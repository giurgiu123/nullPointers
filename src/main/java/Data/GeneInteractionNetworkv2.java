package Data;

import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.swingViewer.ViewPanel;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class GeneInteractionNetworkv2 {

    // Data classes pentru gene și relații.
    static class GeneNode {
        String id;
        String symbol;
        String type; // "central", "interactor", "similar"

        GeneNode(String id, String symbol, String type) {
            this.id = id;
            this.symbol = symbol;
            this.type = type;
        }
    }

    static class GeneEdge {
        String id;
        String source;
        String target;
        String interaction;

        GeneEdge(String id, String source, String target, String interaction) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.interaction = interaction;
        }
    }

    static class InteractionData {
        List<GeneNode> nodes;
        List<GeneEdge> edges;

        InteractionData(List<GeneNode> nodes, List<GeneEdge> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    // Clasa pentru informațiile despre medicamente.
    static class DrugInfo {

        String geneSymbol;
        String drugName;
        String indication;
        String mechanism;

        DrugInfo(String geneSymbol, String drugName, String indication, String mechanism) {
            this.geneSymbol = geneSymbol;
            this.drugName = drugName;
            this.indication = indication;
            this.mechanism = mechanism;
        }
    }

    // Metodă utilitară: preia text de la o adresă URL.
    public static String getTextFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine).append("\n");
        }
        in.close();
        return response.toString();
    }

    // Metodă pentru a prelua datele de interacțiune din KEGG pentru o genă dată.
    public static InteractionData fetchKEGGInteractions(String geneKEGGId, String geneSymbol) throws Exception {
        // STEP 1: Preia căile pentru genă.
        String pathwayUrl = "https://rest.kegg.jp/link/pathway/" + geneKEGGId;
        String pathwayText = getTextFromUrl(pathwayUrl);
        String[] pathwayLines = pathwayText.trim().split("\n");
        if (pathwayLines.length == 0) {
            throw new Exception("Nu s-au găsit căi pentru gena " + geneSymbol);
        }
        // Folosește prima cale (ex: "hsa:64285\tpath:hsa05200")
        String[] parts = pathwayLines[0].split("\t");
        if (parts.length < 2) {
            throw new Exception("Format neașteptat pentru calea: " + pathwayLines[0]);
        }
        String pathwayId = parts[1];
        System.out.println("Folosim calea: " + pathwayId);

        // STEP 2: Preia KGML (XML) pentru calea aleasă.
        String kgmlUrl = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
        String kgmlText = getTextFromUrl(kgmlUrl);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document xmlDoc = dBuilder.parse(new java.io.ByteArrayInputStream(kgmlText.getBytes("UTF-8")));
        xmlDoc.getDocumentElement().normalize();

        // STEP 3: Construiește o mapare de la id-ul entry-ului la simbolul genei.
        Map<String, String> entryMap = new HashMap<>();
        NodeList entryList = xmlDoc.getElementsByTagName("entry");
        for (int i = 0; i < entryList.getLength(); i++) {
            org.w3c.dom.Node node = entryList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element entryElem = (Element) node;
                String type = entryElem.getAttribute("type");
                if ("gene".equals(type)) {
                    String id = entryElem.getAttribute("id");
                    String nameAttr = entryElem.getAttribute("name"); // ex: "hsa:64285 EGFR"
                    String[] nameParts = nameAttr.split(" ");
                    String symbol = (nameParts.length > 1) ? nameParts[1] : nameParts[0];
                    entryMap.put(id, symbol);
                }
            }
        }

        // STEP 4: Parcurge relațiile și adaugă doar cele care implică gena centrală.
        NodeList relationList = xmlDoc.getElementsByTagName("relation");
        Map<String, GeneNode> nodeMap = new HashMap<>();
        List<GeneEdge> edges = new ArrayList<>();

        // Adaugă nodul central.
        String centralEntryId = null;
        for (Map.Entry<String, String> entry : entryMap.entrySet()) {
            if (geneSymbol.equals(entry.getValue())) {
                centralEntryId = entry.getKey();
                break;
            }
        }
        if (centralEntryId == null) {
            centralEntryId = geneKEGGId;
        }
        nodeMap.put(centralEntryId, new GeneNode(centralEntryId, geneSymbol, "central"));

        // Pentru fiecare relație ce implică gena centrală, adaugă nodurile interacționare.
        for (int i = 0; i < relationList.getLength(); i++) {
            org.w3c.dom.Node relNode = relationList.item(i);
            if (relNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element relElem = (Element) relNode;
                String entry1 = relElem.getAttribute("entry1");
                String entry2 = relElem.getAttribute("entry2");
                String symbol1 = entryMap.get(entry1);
                String symbol2 = entryMap.get(entry2);
                if (geneSymbol.equals(symbol1) || geneSymbol.equals(symbol2)) {
                    String interactingEntryId = geneSymbol.equals(symbol1) ? entry2 : entry1;
                    String interactingSymbol = entryMap.get(interactingEntryId);
                    if (interactingSymbol == null)
                        continue;
                    String interactionType = "";
                    NodeList subtypes = relElem.getElementsByTagName("subtype");
                    if (subtypes.getLength() > 0) {
                        Element subtypeElem = (Element) subtypes.item(0);
                        interactionType = subtypeElem.getAttribute("name");
                    }
                    if (!nodeMap.containsKey(interactingEntryId)) {
                        nodeMap.put(interactingEntryId, new GeneNode(interactingEntryId, interactingSymbol, "interactor"));
                    }
                    GeneEdge edge = new GeneEdge("e" + i, centralEntryId, interactingEntryId, interactionType);
                    edges.add(edge);
                }
            }
        }

        // STEP 5: Adaugă până la 3 gene similare suplimentare (dacă nu apar deja).
        int similarCount = 0;
        for (Map.Entry<String, String> entry : entryMap.entrySet()) {
            String entryId = entry.getKey();
            String symbol = entry.getValue();
            if (!symbol.equals(geneSymbol) && !nodeMap.containsKey(entryId) && similarCount < 30) {
                nodeMap.put(entryId, new GeneNode(entryId, symbol, "similar"));
                edges.add(new GeneEdge("e_sim_" + entryId, centralEntryId, entryId, "similar"));
                similarCount++;
            }
        }

        return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
    }

    public static Viewer createGraphViewer(InteractionData data) {
        SingleGraph graph = new SingleGraph("Gene-Gene Interaction Network");
        String styleSheet = ""
                + "graph { padding: 50px; }"
                + "node { fill-color: #61bffc; size: 40px; text-size: 14; text-alignment: center; stroke-mode: plain; stroke-color: #555; stroke-width: 2; }"
                + "node.central { fill-color: #aaffaa; shape: diamond; }"
                + "node.similar { fill-color: #ffaaaa; }"
                + "edge { fill-color: #9dbaea; text-size: 12; }";
        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true);
        graph.setStrict(false);

        // Adaugă nodurile și muchiile inițiale din data
        for (GeneNode gene : data.nodes) {
            Node n = graph.addNode(gene.id);
            n.setAttribute("ui.label", gene.symbol);
            n.setAttribute("ui.class", gene.type);
        }
        for (GeneEdge edge : data.edges) {
            try {
                Edge e = graph.addEdge(edge.id, edge.source, edge.target, true);
                e.setAttribute("ui.label", edge.interaction);
            } catch (Exception ex) {
                // Dacă muchia există deja, o ignorăm.
            }
        }

        Viewer viewer = graph.display();
        ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();

        // Elimină orice listener existent pentru a evita conflictele
        for (MouseListener ml : viewPanel.getMouseListeners()) {
            viewPanel.removeMouseListener(ml);
        }

        // Adaugă un singur listener pentru click pe noduri care reface graful
        viewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GraphicElement element = viewPanel.findNodeOrSpriteAt(e.getX(), e.getY());
                // Verificăm dacă elementul este un nod
                if (element != null && element.getClass().getSimpleName().equals("Node")) {
                    Node clickedNode = graph.getNode(element.getId());
                    String clickedGeneKEGGId = clickedNode.getId();
                    String clickedGeneSymbol = (String) clickedNode.getAttribute("ui.label");
                    System.out.println("Clicked on gene: " + clickedGeneSymbol + " (" + clickedGeneKEGGId + ")");

                    // Se preiau interacțiunile pentru gena clicată
                    try {
                        InteractionData newData = fetchKEGGInteractions(clickedGeneKEGGId, clickedGeneSymbol);

                        // Ștergem tot graful curent
                        graph.clear();

                        // Re-adăugăm nodurile și muchiile noi din newData
                        for (GeneNode gene : newData.nodes) {
                            Node n = graph.addNode(gene.id);
                            n.setAttribute("ui.label", gene.symbol);
                            n.setAttribute("ui.class", gene.type);
                        }
                        for (GeneEdge edge : newData.edges) {
                            try {
                                Edge eNew = graph.addEdge(edge.id, edge.source, edge.target, true);
                                eNew.setAttribute("ui.label", edge.interaction);
                            } catch (Exception ex) {
                                // Dacă muchia există deja, o ignorăm.
                            }
                        }

                        // Reaplicăm stilul, dacă este necesar
                        graph.setAttribute("ui.stylesheet", styleSheet);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        return viewer;
    }
//    public static Viewer createGraphViewer(InteractionData data) {
//        SingleGraph graph = new SingleGraph("Gene-Gene Interaction Network");
//        String styleSheet = ""
//                + "graph { padding: 50px; }"
//                + "node { fill-color: #61bffc; size: 40px; text-size: 14; text-alignment: center; stroke-mode: plain; stroke-color: #555; stroke-width: 2; }"
//                + "node.central { fill-color: #aaffaa; shape: diamond; }"
//                + "node.similar { fill-color: #ffaaaa; }"
//                + "edge { fill-color: #9dbaea; text-size: 12; }";
//        graph.setAttribute("ui.stylesheet", styleSheet);
//        graph.setAutoCreate(true);
//        graph.setStrict(false);
//
//        for (GeneNode gene : data.nodes) {
//            Node n = graph.addNode(gene.id);
//            n.setAttribute("ui.label", gene.symbol);
//            n.setAttribute("ui.class", gene.type);
//        }
//        for (GeneEdge edge : data.edges) {
//            try {
//                Edge e = graph.addEdge(edge.id, edge.source, edge.target, true);
//                e.setAttribute("ui.label", edge.interaction);
//            } catch (Exception ex) {
//                // Dacă muchia există deja, o ignorăm.
//            }
//        }
//        Viewer viewer = graph.display();
//        // Adaugă un listener pe noduri (exemplu de interacțiune)
//       ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
//        viewPanel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                GraphicElement element = viewPanel.findNodeOrSpriteAt(e.getX(), e.getY());
//                if (element != null) {
//                    String nodeId = element.getId();
//                    Node clickedNode = graph.getNode(nodeId);
//                    System.out.println("Clicked on gene: " + clickedNode.getAttribute("ui.label")
//                            + " (Type: " + clickedNode.getAttribute("ui.class") + ")");
//
//                    // Ascunde toate nodurile și muchiile
//                    for (Node node : graph) {
//                        node.setAttribute("ui.hide", true);
//                    }
//                    for (Edge edge : graph.getEdgeSet()) {
//                        edge.setAttribute("ui.hide", true);
//                    }
//
//                    // Afișează nodul clicat și setează-l ca central
//                    clickedNode.setAttribute("ui.hide", false);
//                    clickedNode.setAttribute("ui.class", "central");
//
//                    // Afișează muchiile care pleacă din nodul clicat și nodurile țintă
//                    for (Edge edge : clickedNode.getLeavingEdgeSet()) {
//                        edge.setAttribute("ui.hide", false);
//                        Node target = edge.getTargetNode();
//                        target.setAttribute("ui.hide", false);
//                        target.setAttribute("ui.class", "interactor");
//                    }
//
//                    // Forțează reîmprospătarea vizualizării
//                    viewPanel.repaint();
//                }
//            }
//        });
//        return viewer;
//    }

    // Metodă pentru simularea unei baze de date de medicamente (exemplu static sau citit din CSV).
    public static Map<String, List<DrugInfo>> loadDrugMapFromCSV(String filePath) {
        Map<String, List<DrugInfo>> drugMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // skip header
                    continue;
                }

                // Safe CSV parsing (basic)
                String[] parts = line.split(",", -1); // keep empty columns if any
                if (parts.length < 4) continue;

                String gene = parts[0].trim();
                String drug = parts[1].trim();
                String disease = parts[2].trim();
                String mechanism = parts[3].trim();

                DrugInfo info = new DrugInfo(gene, drug, disease, mechanism);
                drugMap.computeIfAbsent(gene, k -> new ArrayList<>()).add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return drugMap;
    }

    // Metodă pentru afișarea sugestiilor de repurposing într-un tabel cu drug names clicabile.
    public static void displayNetworkAndDrugSuggestions(InteractionData data) {
        // Creează viewer-ul grafic.
        Viewer viewer = createGraphViewer(data);
        java.awt.Component graphPanel = viewer.getDefaultView();

        // Construiește datele pentru tabelul de medicamente.
        Map<String,List<DrugInfo>> drugDB =loadDrugMapFromCSV("/Users/paulgiurgiu/Documents/hackathon/nullPointers/src/main/resources/drug_repurposing_map.csv");
        List<Object[]> rows = new ArrayList<>();
        // Pentru fiecare genă din rețea, caută medicamente asociate.
        for (GeneNode gene : data.nodes) {
            for (Map.Entry<String, List<DrugInfo>> entry : drugDB.entrySet()) {
                String geneSymbol = entry.getKey();

                if (geneSymbol.equalsIgnoreCase(gene.symbol)) {
                    int score = 0;
                    if ("similar".equals(gene.type)) {
                        score += 10;
                    } else if ("interactor".equals(gene.type)) {
                        score += 5;
                    } else if ("central".equals(gene.type)) {
                        score += 0;
                    }

//                    for (DrugInfo drug : entry.getValue()) {
//                        System.out.println("Adaugă: " + gene.symbol + " - " + drug.drug + " cu scor: " + score);
//                        rows.add(new Object[]{
//                                gene.symbol,
//                                drug.drug,
//                                drug.disease,
//                                score,
//                                drug.mechanism
//                        });
//                    }
                    for (DrugInfo drug : entry.getValue()) {
                        System.out.println("Adaugă: " + gene.symbol + " - " + drug.drugName + "-"+drug.indication+" cu scor: " + score);

                        rows.add(new Object[]{
                                gene.symbol,
                                drug.drugName,        // numele medicamentului
                                drug.indication,     // indicația terapeutică
                                score,            // scor calculat în funcție de tipul genei
                                drug.mechanism    // mecanismul de acțiune
                        });
                    }
                }
            }
        }
        String[] columnNames = { "Gene", "Drug", "Indication", "Repurposing Score", "Mechanism" };
        Object[][] tableData = rows.toArray(new Object[0][]);
        JTable table = new JTable(tableData, columnNames);

        // Adaugă un listener pentru ca, la clic pe celula "Drug", să se afișeze informații suplimentare.
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 1) { // Dacă se face clic pe coloana "Drug"
                    String drugName = (String) table.getValueAt(row, col);
                    String mechanism = (String) table.getValueAt(row, 4);
                    JOptionPane.showMessageDialog(null,
                            "Drug: " + drugName + "\nMechanism: " + mechanism,
                            "Drug Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);

        // Integrează panoul grafic și tabelul într-un JSplitPane.
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, tableScroll);
        splitPane.setDividerLocation(700);

        JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.setSize(1200, 800);
        frame.setVisible(true);
    }
    public static List<Object[]> filterDrugsForGene(InteractionData data, Map<String, List<DrugInfo>> drugDB) {
        List<Object[]> rows = new ArrayList<>();

        for (GeneNode gene : data.nodes) {
            List<DrugInfo> drugs = drugDB.get(gene.symbol);
            if (drugs != null) {
                int score = switch (gene.type) {
                    case "central" -> 0;
                    case "interactor" -> 5;
                    case "similar" -> 10;
                    default -> 0;
                };

                for (DrugInfo drug : drugs) {
                    rows.add(new Object[]{
                            gene.symbol,
                            drug.drugName,
                            drug.indication,
                            score,
                            drug.mechanism
                    });
                }
            }
        }

        return rows;
    }
//
//    public static void main(String[] args) {
//        try {
//            // Exemplu: folosim EGFR cu KEGG id "hsa:4210" și simbol "EGFR"
//            String geneKEGGId = "hsa:4210";
//            String geneSymbol = "TP53";
//
//            // Preia datele de interacțiune din KEGG.
//            InteractionData data = fetchKEGGInteractions(geneKEGGId, geneSymbol);
//
//            // Afișează în consolă nodurile și muchiile.
//            System.out.println("Nodes:");
//            for (GeneNode node : data.nodes) {
//                System.out.println(" - " + node.symbol + " (" + node.type + ")");
//            }
//            System.out.println("Edges:");
//            for (GeneEdge edge : data.edges) {
//                System.out.println(" - " + edge.source + " -> " + edge.target + " [" + edge.interaction + "]");
//            }
//
//    // Alege una dintre metode:
//            // 1. Afișează doar rețeaua:
//            // createGraphViewer(data);
//            // 2. Afișează doar tabelul cu medicamente:
//            //displayDrugRepurposingSuggestions(data);
//            // 3. Integrează rețeaua și tabelul într-un singur frame:
//            // Afișează rețeaua și sugestiile de reorientare a medicamentelor într-o interfață combinată.
//            displayNetworkAndDrugSuggestions(data);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        // Creează fereastra principală
        JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLayout(new BorderLayout());

        // Panoul de input persistent cu câmpurile de text și butonul "Load"
        JPanel inputPanel = new JPanel();
        JLabel keggLabel = new JLabel("KEGG ID:");
        JTextField keggField = new JTextField("hsa:4210", 10);
        JLabel geneLabel = new JLabel("Gene Symbol:");
        JTextField geneField = new JTextField("TP53", 10);
        JButton loadButton = new JButton("Load");
        inputPanel.add(keggLabel);
        inputPanel.add(keggField);
        inputPanel.add(geneLabel);
        inputPanel.add(geneField);
        inputPanel.add(loadButton);
        frame.add(inputPanel, BorderLayout.NORTH);

        // JTabbedPane pentru cele două tab-uri: Network și Drug Repurposing
        JTabbedPane tabbedPane = new JTabbedPane();
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Creează panourile pentru cele două tab-uri
        JPanel networkPanel = new JPanel(new BorderLayout());
        JPanel drugPanel = new JPanel(new BorderLayout());
        tabbedPane.addTab("Network", networkPanel);
        tabbedPane.addTab("Drug Repurposing", drugPanel);

        // Funcție de încărcare și actualizare a datelor
        Runnable loadData = () -> {
            String geneKEGGId = keggField.getText().trim();
            String geneSymbol = geneField.getText().trim();
            try {
                // Preia datele de interacțiune din KEGG
                InteractionData data = fetchKEGGInteractions(geneKEGGId, geneSymbol);

                // Afișează în consolă nodurile și muchiile
                System.out.println("Nodes:");
                for (GeneNode node : data.nodes) {
                    System.out.println(" - " + node.symbol + " (" + node.type + ")");
                }
                System.out.println("Edges:");
                for (GeneEdge edge : data.edges) {
                    System.out.println(" - " + edge.source + " -> " + edge.target + " [" + edge.interaction + "]");
                }

                // Actualizează tab-ul Network: creează un nou Viewer și adaugă componenta grafică
                networkPanel.removeAll();
                Viewer viewer = createGraphViewer(data);
                networkPanel.add(viewer.getDefaultView(), BorderLayout.CENTER);
                networkPanel.revalidate();
                networkPanel.repaint();

                // Actualizează tab-ul Drug Repurposing: creează un nou JTable cu datele din CSV
                drugPanel.removeAll();
                // Înlocuiește calea cu cea corectă pentru fișierul tău CSV
                Map<String, List<DrugInfo>> drugDB = loadDrugMapFromCSV(
                        "/Users/paulgiurgiu/Documents/hackathon/nullPointers/src/main/resources/drug_repurposing_map.csv"
                );
                List<Object[]> rows = filterDrugsForGene(data, drugDB);
                String[] columnNames = { "Gene", "Drug", "Indication", "Repurposing Score", "Mechanism" };
                JTable table = new JTable(rows.toArray(new Object[0][]), columnNames);
                table.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = table.rowAtPoint(e.getPoint());
                        int col = table.columnAtPoint(e.getPoint());
                        if (row >= 0 && col == 1) { // dacă se face clic pe coloana "Drug"
                            String drugName = (String) table.getValueAt(row, col);
                            String mechanism = (String) table.getValueAt(row, 4);
                            JOptionPane.showMessageDialog(frame,
                                    "Drug: " + drugName + "\nMechanism: " + mechanism,
                                    "Drug Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                drugPanel.add(new JScrollPane(table), BorderLayout.CENTER);
                drugPanel.revalidate();
                drugPanel.repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        };

        // Încărcare inițială cu valorile implicite
        loadData.run();

        // Butonul "Load" actualizează datele conform valorilor din câmpurile de text
        loadButton.addActionListener(e -> loadData.run());

        frame.setVisible(true);
    });
}
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            // Creează fereastra principală
//            JFrame frame = new JFrame("Gene Interaction Network");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(1200, 800);
//            frame.setLayout(new BorderLayout());
//
//            // Panou pentru input-ul utilizatorului
//            JPanel inputPanel = new JPanel();
//            JLabel keggLabel = new JLabel("KEGG ID:");
//            JTextField keggField = new JTextField(10);
//            JLabel symbolLabel = new JLabel("Gene Symbol:");
//            JTextField symbolField = new JTextField(10);
//            JButton loadButton = new JButton("Load");
//
//            inputPanel.add(keggLabel);
//            inputPanel.add(keggField);
//            inputPanel.add(symbolLabel);
//            inputPanel.add(symbolField);
//            inputPanel.add(loadButton);
//
//            // Panou pentru afișarea rețelei și a sugestiilor (aici folosim doar rețeaua)
//            JPanel networkPanel = new JPanel(new BorderLayout());
//
//            // Adaugă panourile în fereastră
//            frame.add(inputPanel, BorderLayout.NORTH);
//            frame.add(networkPanel, BorderLayout.CENTER);
//            frame.setVisible(true);
//
//            // La apăsarea butonului, se încarcă rețeaua pentru datele introduse
//            loadButton.addActionListener(e -> {
//                String geneKEGGId = keggField.getText().trim();
//                String geneSymbol = symbolField.getText().trim();
//
//                if(geneKEGGId.isEmpty() || geneSymbol.isEmpty()){
//                    JOptionPane.showMessageDialog(frame, "Vă rugăm să introduceți atât KEGG ID cât și Gene Symbol.");
//                    return;
//                }
//
//                try {
//                    // Obține datele de interacțiune de la KEGG
//                    InteractionData data = fetchKEGGInteractions(geneKEGGId, geneSymbol);
//
//                    // Șterge conținutul anterior al panoului (dacă există)
//                    networkPanel.removeAll();
//
//                    // Creează vizualizarea grafică a rețelei
//                    Viewer viewer = createGraphViewer(data);
//                    // Obține componenta care reprezintă vizualizarea rețelei
//                    java.awt.Component graphComponent = viewer.getDefaultView();
//                    networkPanel.add(graphComponent, BorderLayout.CENTER);
//
//                    // Reafișează panoul
//                    networkPanel.revalidate();
//                    networkPanel.repaint();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(frame, "Eroare: " + ex.getMessage());
//                }
//            });
//        });
//    }
}