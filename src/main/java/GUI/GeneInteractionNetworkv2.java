////package GUI;
////
////import org.graphstream.ui.graphicGraph.GraphicElement;
////import org.graphstream.ui.swingViewer.ViewPanel;
////
////import java.awt.BorderLayout;
////import java.awt.event.MouseAdapter;
////import java.awt.event.MouseEvent;
////import java.io.BufferedReader;
////import java.io.FileInputStream;
////import java.io.InputStreamReader;
////import java.net.HttpURLConnection;
////import java.net.URL;
////import java.nio.charset.StandardCharsets;
////import java.util.*;
////import javax.swing.*;
////import javax.xml.parsers.DocumentBuilder;
////import javax.xml.parsers.DocumentBuilderFactory;
////
////import org.graphstream.graph.Edge;
////import org.graphstream.graph.Node;
////import org.graphstream.graph.implementations.SingleGraph;
////import org.graphstream.ui.view.Viewer;
////import org.w3c.dom.Document;
////import org.w3c.dom.Element;
////import org.w3c.dom.NodeList;
////
////public class GeneInteractionNetworkv2 {
////
////    // Data classes pentru gene și relații.
////    static class GeneNode {
////        String id;
////        String symbol;
////        String type; // "central", "interactor", "similar"
////
////        GeneNode(String id, String symbol, String type) {
////            this.id = id;
////            this.symbol = symbol;
////            this.type = type;
////        }
////    }
////
////    static class GeneEdge {
////        String id;
////        String source;
////        String target;
////        String interaction;
////
////        GeneEdge(String id, String source, String target, String interaction) {
////            this.id = id;
////            this.source = source;
////            this.target = target;
////            this.interaction = interaction;
////        }
////    }
////
////    static class InteractionData {
////        List<GeneNode> nodes;
////        List<GeneEdge> edges;
////
////        InteractionData(List<GeneNode> nodes, List<GeneEdge> edges) {
////            this.nodes = nodes;
////            this.edges = edges;
////        }
////    }
////
////    // Clasa pentru informațiile despre medicamente.
////    static class DrugInfo {
////
////        String geneSymbol;
////        String drugName;
////        String indication;
////        String mechanism;
////
////        DrugInfo(String geneSymbol, String drugName, String indication, String mechanism) {
////            this.geneSymbol = geneSymbol;
////            this.drugName = drugName;
////            this.indication = indication;
////            this.mechanism = mechanism;
////        }
////    }
////
////    // Metodă utilitară: preia text de la o adresă URL.
////    public static String getTextFromUrl(String urlString) throws Exception {
////        URL url = new URL(urlString);
////        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
////        conn.setRequestMethod("GET");
////        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
////        StringBuilder response = new StringBuilder();
////        String inputLine;
////        while ((inputLine = in.readLine()) != null) {
////            response.append(inputLine).append("\n");
////        }
////        in.close();
////        return response.toString();
////    }
////
////    // Metodă pentru a prelua datele de interacțiune din KEGG pentru o genă dată.
////    public static InteractionData fetchKEGGInteractions(String geneKEGGId, String geneSymbol) throws Exception {
////        // STEP 1: Preia căile pentru genă.
////        String pathwayUrl = "https://rest.kegg.jp/link/pathway/" + geneKEGGId;
////        String pathwayText = getTextFromUrl(pathwayUrl);
////        String[] pathwayLines = pathwayText.trim().split("\n");
////        if (pathwayLines.length == 0) {
////            throw new Exception("Nu s-au găsit căi pentru gena " + geneSymbol);
////        }
////        // Folosește prima cale (ex: "hsa:64285\tpath:hsa05200")
////        String[] parts = pathwayLines[0].split("\t");
////        if (parts.length < 2) {
////            throw new Exception("Format neașteptat pentru calea: " + pathwayLines[0]);
////        }
////        String pathwayId = parts[1];
////        System.out.println("Folosim calea: " + pathwayId);
////
////        // STEP 2: Preia KGML (XML) pentru calea aleasă.
////        String kgmlUrl = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
////        String kgmlText = getTextFromUrl(kgmlUrl);
////        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
////        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
////        Document xmlDoc = dBuilder.parse(new java.io.ByteArrayInputStream(kgmlText.getBytes("UTF-8")));
////        xmlDoc.getDocumentElement().normalize();
////
////        // STEP 3: Construiește o mapare de la id-ul entry-ului la simbolul genei.
////        Map<String, String> entryMap = new HashMap<>();
////        NodeList entryList = xmlDoc.getElementsByTagName("entry");
////        for (int i = 0; i < entryList.getLength(); i++) {
////            org.w3c.dom.Node node = entryList.item(i);
////            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
////                Element entryElem = (Element) node;
////                String type = entryElem.getAttribute("type");
////                if ("gene".equals(type)) {
////                    String id = entryElem.getAttribute("id");
////                    String nameAttr = entryElem.getAttribute("name"); // ex: "hsa:64285 EGFR"
////                    String[] nameParts = nameAttr.split(" ");
////                    String symbol = (nameParts.length > 1) ? nameParts[1] : nameParts[0];
////                    entryMap.put(id, symbol);
////                }
////            }
////        }
////
////        // STEP 4: Parcurge relațiile și adaugă doar cele care implică gena centrală.
////        NodeList relationList = xmlDoc.getElementsByTagName("relation");
////        Map<String, GeneNode> nodeMap = new HashMap<>();
////        List<GeneEdge> edges = new ArrayList<>();
////
////        // Adaugă nodul central.
////        String centralEntryId = null;
////        for (Map.Entry<String, String> entry : entryMap.entrySet()) {
////            if (geneSymbol.equals(entry.getValue())) {
////                centralEntryId = entry.getKey();
////                break;
////            }
////        }
////        if (centralEntryId == null) {
////            centralEntryId = geneKEGGId;
////        }
////        nodeMap.put(centralEntryId, new GeneNode(centralEntryId, geneSymbol, "central"));
////
////        // Pentru fiecare relație ce implică gena centrală, adaugă nodurile interacționare.
////        for (int i = 0; i < relationList.getLength(); i++) {
////            org.w3c.dom.Node relNode = relationList.item(i);
////            if (relNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
////                Element relElem = (Element) relNode;
////                String entry1 = relElem.getAttribute("entry1");
////                String entry2 = relElem.getAttribute("entry2");
////                String symbol1 = entryMap.get(entry1);
////                String symbol2 = entryMap.get(entry2);
////                if (geneSymbol.equals(symbol1) || geneSymbol.equals(symbol2)) {
////                    String interactingEntryId = geneSymbol.equals(symbol1) ? entry2 : entry1;
////                    String interactingSymbol = entryMap.get(interactingEntryId);
////                    if (interactingSymbol == null)
////                        continue;
////                    String interactionType = "";
////                    NodeList subtypes = relElem.getElementsByTagName("subtype");
////                    if (subtypes.getLength() > 0) {
////                        Element subtypeElem = (Element) subtypes.item(0);
////                        interactionType = subtypeElem.getAttribute("name");
////                    }
////                    if (!nodeMap.containsKey(interactingEntryId)) {
////                        nodeMap.put(interactingEntryId, new GeneNode(interactingEntryId, interactingSymbol, "interactor"));
////                    }
////                    GeneEdge edge = new GeneEdge("e" + i, centralEntryId, interactingEntryId, interactionType);
////                    edges.add(edge);
////                }
////            }
////        }
////
////        // STEP 5: Adaugă până la 3 gene similare suplimentare (dacă nu apar deja).
////        int similarCount = 0;
////        for (Map.Entry<String, String> entry : entryMap.entrySet()) {
////            String entryId = entry.getKey();
////            String symbol = entry.getValue();
////            if (!symbol.equals(geneSymbol) && !nodeMap.containsKey(entryId) && similarCount < 3000) {
////                nodeMap.put(entryId, new GeneNode(entryId, symbol, "similar"));
////                edges.add(new GeneEdge("e_sim_" + entryId, centralEntryId, entryId, "similar"));
////                similarCount++;
////            }
////        }
////
////        return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
////    }
////
////    // Metodă pentru a crea viewer-ul grafic al rețelei.
////    public static Viewer createGraphViewer(InteractionData data) {
////        SingleGraph graph = new SingleGraph("Gene-Gene Interaction Network");
////        String styleSheet = ""
////                + "graph { padding: 50px; }"
////                + "node { fill-color: #61bffc; size: 40px; text-size: 14; text-alignment: center; stroke-mode: plain; stroke-color: #555; stroke-width: 2; }"
////                + "node.central { fill-color: #aaffaa; shape: diamond; }"
////                + "node.similar { fill-color: #ffaaaa; }"
////                + "edge { fill-color: #9dbaea; text-size: 12; }";
////        graph.setAttribute("ui.stylesheet", styleSheet);
////        graph.setAutoCreate(true);
////        graph.setStrict(false);
////
////        for (GeneNode gene : data.nodes) {
////            Node n = graph.addNode(gene.id);
////            n.setAttribute("ui.label", gene.symbol);
////            n.setAttribute("ui.class", gene.type);
////        }
////        for (GeneEdge edge : data.edges) {
////            try {
////                Edge e = graph.addEdge(edge.id, edge.source, edge.target, true);
////                e.setAttribute("ui.label", edge.interaction);
////            } catch (Exception ex) {
////                // Dacă muchia există deja, o ignorăm.
////            }
////        }
////        Viewer viewer = graph.display();
////        // Adaugă un listener pe noduri (exemplu de interacțiune)
////        ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
////        viewPanel.addMouseListener(new MouseAdapter() {
////            @Override
////            public void mouseClicked(MouseEvent e) {
////                GraphicElement element = viewPanel.findNodeOrSpriteAt(e.getX(), e.getY());
////                if (element != null) {
////                    String nodeId = element.getId();
////                    Node clicked = graph.getNode(nodeId);
////                    System.out.println("Clicked on gene: " + clicked.getAttribute("ui.label")
////                            + " (Type: " + clicked.getAttribute("ui.class") + ")");
////                }
////            }
////        });
////        return viewer;
////    }
////
////    // Metodă pentru simularea unei baze de date de medicamente (exemplu static sau citit din CSV).
////    public static Map<String, List<DrugInfo>> loadDrugMapFromCSV(String filePath) {
////        Map<String, List<DrugInfo>> drugMap = new HashMap<>();
////
////        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
////            String line;
////            boolean isHeader = true;
////
////            while ((line = br.readLine()) != null) {
////                if (isHeader) {
////                    isHeader = false; // skip header
////                    continue;
////                }
////
////                // Safe CSV parsing (basic)
////                String[] parts = line.split(",", -1); // keep empty columns if any
////                if (parts.length < 4) continue;
////
////                String gene = parts[0].trim();
////                String drug = parts[1].trim();
////                String disease = parts[2].trim();
////                String mechanism = parts[3].trim();
////
////                DrugInfo info = new DrugInfo(gene, drug, disease, mechanism);
////                drugMap.computeIfAbsent(gene, k -> new ArrayList<>()).add(info);
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////
////        return drugMap;
////    }
////
////    // Metodă pentru afișarea sugestiilor de repurposing într-un tabel cu drug names clicabile.
////    public static void displayNetworkAndDrugSuggestions(InteractionData data) {
////        // Creează viewer-ul grafic.
////        Viewer viewer = createGraphViewer(data);
////        java.awt.Component graphPanel = viewer.getDefaultView();
////
////        // Construiește datele pentru tabelul de medicamente.
////        Map<String,List<DrugInfo>> drugDB =loadDrugMapFromCSV("/Users/paulgiurgiu/Documents/hackathon/nullPointers/src/main/resources/drug_repurposing_map.csv");
////        List<Object[]> rows = new ArrayList<>();
////        // Pentru fiecare genă din rețea, caută medicamente asociate.
////        for (GeneNode gene : data.nodes) {
////            for (Map.Entry<String, List<DrugInfo>> entry : drugDB.entrySet()) {
////                String geneSymbol = entry.getKey();
////
////                if (geneSymbol.equalsIgnoreCase(gene.symbol)) {
////                    int score = 0;
////                    if ("similar".equals(gene.type)) {
////                        score += 10;
////                    } else if ("interactor".equals(gene.type)) {
////                        score += 5;
////                    } else if ("central".equals(gene.type)) {
////                        score += 0;
////                    }
////
//////                    for (DrugInfo drug : entry.getValue()) {
//////                        System.out.println("Adaugă: " + gene.symbol + " - " + drug.drug + " cu scor: " + score);
//////                        rows.add(new Object[]{
//////                                gene.symbol,
//////                                drug.drug,
//////                                drug.disease,
//////                                score,
//////                                drug.mechanism
//////                        });
//////                    }
////                    for (DrugInfo drug : entry.getValue()) {
////                        System.out.println("Adaugă: " + gene.symbol + " - " + drug.drugName + "-"+drug.indication+" cu scor: " + score);
////
////                        rows.add(new Object[]{
////                                gene.symbol,
////                                drug.drugName,        // numele medicamentului
////                                drug.indication,     // indicația terapeutică
////                                (Object) score,            // scor calculat în funcție de tipul genei
////                                drug.mechanism    // mecanismul de acțiune
////                        });
////                    }
////                }
////            }
////        }
////        String[] columnNames = { "Gene", "Drug", "Indication", "Repurposing Score", "Mechanism" };
////        Object[][] tableData = rows.toArray(new Object[0][]);
////        JTable table = new JTable(tableData, columnNames);
////
////        // Adaugă un listener pentru ca, la clic pe celula "Drug", să se afișeze informații suplimentare.
////        table.addMouseListener(new MouseAdapter() {
////            @Override
////            public void mouseClicked(MouseEvent e) {
////                int row = table.rowAtPoint(e.getPoint());
////                int col = table.columnAtPoint(e.getPoint());
////                if (row >= 0 && col == 1) { // Dacă se face clic pe coloana "Drug"
////                    String drugName = (String) table.getValueAt(row, col);
////                    String mechanism = (String) table.getValueAt(row, 4);
////                    JOptionPane.showMessageDialog(null,
////                            "Drug: " + drugName + "\nMechanism: " + mechanism,
////                            "Drug Info", JOptionPane.INFORMATION_MESSAGE);
////                }
////            }
////        });
////
////        JScrollPane tableScroll = new JScrollPane(table);
////
////        // Integrează panoul grafic și tabelul într-un JSplitPane.
////        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, tableScroll);
////        splitPane.setDividerLocation(700);
////
////        JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
////        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        frame.add(splitPane, BorderLayout.CENTER);
////        frame.setSize(1200, 800);
////        frame.setVisible(true);
////    }
////    public static List<Object[]> filterDrugsForGene(InteractionData data, Map<String, List<DrugInfo>> drugDB) {
////        List<Object[]> rows = new ArrayList<>();
////
////        for (GeneNode gene : data.nodes) {
////            List<DrugInfo> drugs = drugDB.get(gene.symbol);
////            if (drugs != null) {
////                int score = switch (gene.type) {
////                    case "central" -> 0;
////                    case "interactor" -> 5;
////                    case "similar" -> 10;
////                    default -> 0;
////                };
////
////                for (DrugInfo drug : drugs) {
////                    rows.add(new Object[]{
////                            gene.symbol,
////                            drug.drugName,
////                            drug.indication,
////                            (Object) score,
////                            drug.mechanism
////                    });
////                }
////            }
////        }
////
////        return rows;
////    }
////
////    public static void main(String[] args) {
////        try {
////            // Exemplu: folosim EGFR cu KEGG id "hsa:4210" și simbol "EGFR"
////            String geneKEGGId = "hsa:4210";
////            String geneSymbol = "TP53";
////
////            // Preia datele de interacțiune din KEGG.
////            InteractionData data = fetchKEGGInteractions(geneKEGGId, geneSymbol);
////
////            // Afișează în consolă nodurile și muchiile.
////            System.out.println("Nodes:");
////            for (GeneNode node : data.nodes) {
////                System.out.println(" - " + node.symbol + " (" + node.type + ")");
////            }
////            System.out.println("Edges:");
////            for (GeneEdge edge : data.edges) {
////                System.out.println(" - " + edge.source + " -> " + edge.target + " [" + edge.interaction + "]");
////            }
////
////    // Alege una dintre metode:
////            // 1. Afișează doar rețeaua:
////            // createGraphViewer(data);
////            // 2. Afișează doar tabelul cu medicamente:
////            //displayDrugRepurposingSuggestions(data);
////            // 3. Integrează rețeaua și tabelul într-un singur frame:
////            // Afișează rețeaua și sugestiile de reorientare a medicamentelor într-o interfață combinată.
////            displayNetworkAndDrugSuggestions(data);
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////    }
////}
//package GUI;
//
//import org.graphstream.ui.graphicGraph.GraphicElement;
//import org.graphstream.ui.swingViewer.ViewPanel;
//import org.graphstream.graph.Edge;
//import org.graphstream.graph.Node;
//import org.graphstream.graph.implementations.SingleGraph;
//import org.graphstream.ui.view.Viewer;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.io.BufferedReader;
//import java.io.ByteArrayInputStream;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.List;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//
//public class GeneInteractionNetworkv2 {
//
//    // Data classes pentru gene și relații.
//    public static class GeneNode {
//        String id;
//        String symbol;
//        String type; // "central", "interactor", "similar", "related"
//        public GeneNode(String id, String symbol, String type) {
//            this.id = id;
//            this.symbol = symbol;
//            this.type = type;
//        }
//    }
//
//    public static class GeneEdge {
//        String id;
//        String source;
//        String target;
//        String interaction;
//        public GeneEdge(String id, String source, String target, String interaction) {
//            this.id = id;
//            this.source = source;
//            this.target = target;
//            this.interaction = interaction;
//        }
//    }
//
//    public static class InteractionData {
//        List<GeneNode> nodes;
//        List<GeneEdge> edges;
//        public InteractionData(List<GeneNode> nodes, List<GeneEdge> edges) {
//            this.nodes = nodes;
//            this.edges = edges;
//        }
//    }
//
//    // Clasa pentru informațiile despre medicamente.
//    public static class DrugInfo {
//        String geneSymbol;
//        String drugName;
//        String indication;
//        String mechanism;
//        public DrugInfo(String geneSymbol, String drugName, String indication, String mechanism) {
//            this.geneSymbol = geneSymbol;
//            this.drugName = drugName;
//            this.indication = indication;
//            this.mechanism = mechanism;
//        }
//    }
//
//    // Metodă utilitară: preia textul de la o adresă URL.
//    public static String getTextFromUrl(String urlString) throws Exception {
//        URL url = new URL(urlString);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String inputLine;
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine).append("\n");
//        }
//        in.close();
//        return response.toString();
//    }
//
//    // Metoda de preluare a interacțiunilor din KEGG (vechea abordare)
//    public static InteractionData fetchKEGGInteractions(String geneKEGGId, String geneSymbol) throws Exception {
//        String pathwayUrl = "https://rest.kegg.jp/link/pathway/" + geneKEGGId;
//        String pathwayText = getTextFromUrl(pathwayUrl);
//        String[] pathwayLines = pathwayText.trim().split("\n");
//        if (pathwayLines.length == 0) {
//            throw new Exception("Nu s-au găsit căi pentru gena " + geneSymbol);
//        }
//        String[] parts = pathwayLines[0].split("\t");
//        if (parts.length < 2) {
//            throw new Exception("Format neașteptat pentru calea: " + pathwayLines[0]);
//        }
//        String pathwayId = parts[1];
//        System.out.println("Folosim calea: " + pathwayId);
//
//        String kgmlUrl = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
//        String kgmlText = getTextFromUrl(kgmlUrl);
//        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//        Document xmlDoc = dBuilder.parse(new ByteArrayInputStream(kgmlText.getBytes("UTF-8")));
//        xmlDoc.getDocumentElement().normalize();
//
//        Map<String, String> entryMap = new HashMap<>();
//        NodeList entryList = xmlDoc.getElementsByTagName("entry");
//        for (int i = 0; i < entryList.getLength(); i++) {
//            org.w3c.dom.Node node = entryList.item(i);
//            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
//                Element entryElem = (Element) node;
//                String type = entryElem.getAttribute("type");
//                if ("gene".equals(type)) {
//                    String id = entryElem.getAttribute("id");
//                    String nameAttr = entryElem.getAttribute("name");
//                    String[] nameParts = nameAttr.split(" ");
//                    String symbol = (nameParts.length > 1) ? nameParts[1] : nameParts[0];
//                    entryMap.put(id, symbol);
//                }
//            }
//        }
//
//        NodeList relationList = xmlDoc.getElementsByTagName("relation");
//        Map<String, GeneNode> nodeMap = new HashMap<>();
//        List<GeneEdge> edges = new ArrayList<>();
//
//        String centralEntryId = null;
//        for (Map.Entry<String, String> entry : entryMap.entrySet()) {
//            if (geneSymbol.equals(entry.getValue())) {
//                centralEntryId = entry.getKey();
//                break;
//            }
//        }
//        if (centralEntryId == null) {
//            centralEntryId = geneKEGGId;
//        }
//        nodeMap.put(centralEntryId, new GeneNode(centralEntryId, geneSymbol, "central"));
//
//        for (int i = 0; i < relationList.getLength(); i++) {
//            org.w3c.dom.Node relNode = relationList.item(i);
//            if (relNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
//                Element relElem = (Element) relNode;
//                String entry1 = relElem.getAttribute("entry1");
//                String entry2 = relElem.getAttribute("entry2");
//                String symbol1 = entryMap.get(entry1);
//                String symbol2 = entryMap.get(entry2);
//                if (geneSymbol.equals(symbol1) || geneSymbol.equals(symbol2)) {
//                    String interactingEntryId = geneSymbol.equals(symbol1) ? entry2 : entry1;
//                    String interactingSymbol = entryMap.get(interactingEntryId);
//                    if (interactingSymbol == null) continue;
//                    String interactionType = "";
//                    NodeList subtypes = relElem.getElementsByTagName("subtype");
//                    if (subtypes.getLength() > 0) {
//                        Element subtypeElem = (Element) subtypes.item(0);
//                        interactionType = subtypeElem.getAttribute("name");
//                    }
//                    if (!nodeMap.containsKey(interactingEntryId)) {
//                        nodeMap.put(interactingEntryId, new GeneNode(interactingEntryId, interactingSymbol, "interactor"));
//                    }
//                    GeneEdge edge = new GeneEdge("e" + i, centralEntryId, interactingEntryId, interactionType);
//                    edges.add(edge);
//                }
//            }
//        }
//
//        int similarCount = 0;
//        for (Map.Entry<String, String> entry : entryMap.entrySet()) {
//            String entryId = entry.getKey();
//            String symbol = entry.getValue();
//            if (!symbol.equals(geneSymbol) && !nodeMap.containsKey(entryId) && similarCount < 30) {
//                nodeMap.put(entryId, new GeneNode(entryId, symbol, "similar"));
//                edges.add(new GeneEdge("e_sim_" + entryId, centralEntryId, entryId, "similar"));
//                similarCount++;
//            }
//        }
//
//        return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
//    }
//
//    // Metoda standard de creare a graficului (folosită în exemplul de test)
//    public static Viewer createGraphViewer(InteractionData data) {
//        SingleGraph graph = new SingleGraph("Gene-Gene Interaction Network");
//        String styleSheet = ""
//                + "graph { padding: 50px; }"
//                + "node { fill-color: #61bffc; size: 40px; text-size: 14; text-alignment: center; stroke-mode: plain; stroke-color: #555; stroke-width: 2; }"
//                + "node.central { fill-color: #aaffaa; shape: diamond; }"
//                + "node.similar { fill-color: #ffaaaa; }"
//                + "node.related { fill-color: #ffc107; }"
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
//
//        Viewer viewer = graph.display();
//        ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
//
//        for (MouseListener ml : viewPanel.getMouseListeners()) {
//            viewPanel.removeMouseListener(ml);
//        }
//
//        viewPanel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                GraphicElement element = viewPanel.findNodeOrSpriteAt(e.getX(), e.getY());
//                if (element != null && element.getClass().getSimpleName().equals("Node")) {
//                    Node clickedNode = graph.getNode(element.getId());
//                    String clickedGeneId = clickedNode.getId();
//                    String clickedGeneSymbol = (String) clickedNode.getAttribute("ui.label");
//                    System.out.println("Clicked on gene: " + clickedGeneSymbol + " (" + clickedGeneId + ")");
//                    try {
//                        InteractionData newData = fetchKEGGInteractions(clickedGeneId, clickedGeneSymbol);
//                        graph.clear();
//                        for (GeneNode gene : newData.nodes) {
//                            Node n = graph.addNode(gene.id);
//                            n.setAttribute("ui.label", gene.symbol);
//                            n.setAttribute("ui.class", gene.type);
//                        }
//                        for (GeneEdge edge : newData.edges) {
//                            try {
//                                Edge eNew = graph.addEdge(edge.id, edge.source, edge.target, true);
//                                eNew.setAttribute("ui.label", edge.interaction);
//                            } catch (Exception ex) {
//                                // Dacă muchia există deja, o ignorăm.
//                            }
//                        }
//                        graph.setAttribute("ui.stylesheet", styleSheet);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//        return viewer;
//    }
//
//    // Noua metodă: construiește un graf bazat pe genele din baza de date,
//    // conectând genele care împărtășesc cel puțin un pathway comun.
//    // Această metodă folosește clasele din pachetul DataModel (Gene și PathWay).
//    public static InteractionData createGraphFromGenes(List<DataModel.Gene> allGenes, DataModel.Gene selectedGene) {
//        Map<String, GeneNode> nodeMap = new HashMap<>();
//        List<GeneEdge> edges = new ArrayList<>();
//
//        // Nodul central – gena selectată
//        nodeMap.put(selectedGene.getName(), new GeneNode(selectedGene.getName(), selectedGene.getName(), "central"));
//
//        // Adaugă noduri pentru genele care au cel puțin un pathway comun cu gena selectată.
//        for (DataModel.Gene gene : allGenes) {
//            if (!gene.getName().equalsIgnoreCase(selectedGene.getName())) {
//                boolean hasCommonPathway = false;
//                for (DataModel.PathWay p1 : selectedGene.getPathWays()) {
//                    for (DataModel.PathWay p2 : gene.getPathWays()) {
//                        if (p1.getPath().equals(p2.getPath())) {
//                            hasCommonPathway = true;
//                            break;
//                        }
//                    }
//                    if (hasCommonPathway) break;
//                }
//                if (hasCommonPathway) {
//                    nodeMap.put(gene.getName(), new GeneNode(gene.getName(), gene.getName(), "related"));
//                }
//            }
//        }
//
//        // Creează muchii între genele care împărtășesc cel puțin un pathway comun.
//        List<String> geneNames = new ArrayList<>(nodeMap.keySet());
//        for (int i = 0; i < geneNames.size(); i++) {
//            for (int j = i + 1; j < geneNames.size(); j++) {
//                DataModel.Gene gene1 = findGeneByName(allGenes, geneNames.get(i));
//                DataModel.Gene gene2 = findGeneByName(allGenes, geneNames.get(j));
//                if (gene1 != null && gene2 != null && shareCommonPathway(gene1, gene2)) {
//                    String edgeId = "e_" + gene1.getName() + "_" + gene2.getName();
//                    edges.add(new GeneEdge(edgeId, gene1.getName(), gene2.getName(), "common_pathway"));
//                }
//            }
//        }
//        return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
//    }
//
//    // Metodă utilitară pentru a găsi o genă după nume în lista de gene.
//    private static DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String name) {
//        for (DataModel.Gene gene : genes) {
//            if (gene.getName().equalsIgnoreCase(name)) {
//                return gene;
//            }
//        }
//        return null;
//    }
//
//    // Verifică dacă două gene au cel puțin un pathway comun.
//    private static boolean shareCommonPathway(DataModel.Gene gene1, DataModel.Gene gene2) {
//        for (DataModel.PathWay p1 : gene1.getPathWays()) {
//            for (DataModel.PathWay p2 : gene2.getPathWays()) {
//                if (p1.getPath().equals(p2.getPath())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//    // Metode suplimentare pentru încărcarea și filtrarea datelor despre medicamente:
//    public static Map<String, List<DrugInfo>> loadDrugMapFromCSV(String filePath) {
//        Map<String, List<DrugInfo>> drugMap = new HashMap<>();
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
//            String line;
//            boolean isHeader = true;
//            while ((line = br.readLine()) != null) {
//                if (isHeader) {
//                    isHeader = false;
//                    continue;
//                }
//                String[] parts = line.split(",", -1);
//                if (parts.length < 4) continue;
//                String gene = parts[0].trim();
//                String drug = parts[1].trim();
//                String disease = parts[2].trim();
//                String mechanism = parts[3].trim();
//                DrugInfo info = new DrugInfo(gene, drug, disease, mechanism);
//                drugMap.computeIfAbsent(gene, k -> new ArrayList<>()).add(info);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return drugMap;
//    }
//
//    public static List<Object[]> filterDrugsForGene(InteractionData data, Map<String, List<DrugInfo>> drugDB) {
//        List<Object[]> rows = new ArrayList<>();
//        for (GeneNode gene : data.nodes) {
//            List<DrugInfo> drugs = drugDB.get(gene.symbol);
//            if (drugs != null) {
//                int score = 0;
//                switch (gene.type) {
//                    case "central": score = 0; break;
//                    case "interactor": score = 5; break;
//                    case "similar": score = 10; break;
//                    case "related": score = 7; break;
//                    default: score = 0;
//                }
//                for (DrugInfo drug : drugs) {
//                    rows.add(new Object[]{gene.symbol, drug.drugName, drug.indication, score, drug.mechanism});
//                }
//            }
//        }
//        return rows;
//    }
//
//    // Metoda de afișare a rețelei și sugestiilor de repurposing într-o interfață integrată.
//    public static void displayNetworkAndDrugSuggestions(InteractionData data) {
//        Viewer viewer = createGraphViewer(data);
//        Component graphPanel = viewer.getDefaultView();
//        Map<String, List<DrugInfo>> drugDB = loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
//        List<Object[]> rows = new ArrayList<>();
//        for (GeneNode gene : data.nodes) {
//            for (Map.Entry<String, List<DrugInfo>> entry : drugDB.entrySet()) {
//                String geneSymbol = entry.getKey();
//                if (geneSymbol.equalsIgnoreCase(gene.symbol)) {
//                    int score = 0;
//                    if ("similar".equals(gene.type)) {
//                        score += 10;
//                    } else if ("interactor".equals(gene.type)) {
//                        score += 5;
//                    } else if ("central".equals(gene.type)) {
//                        score += 0;
//                    }
//                    for (DrugInfo drug : entry.getValue()) {
//                        rows.add(new Object[]{gene.symbol, drug.drugName, drug.indication, score, drug.mechanism});
//                    }
//                }
//            }
//        }
//        String[] columnNames = { "Gene", "Drug", "Indication", "Repurposing Score", "Mechanism" };
//        Object[][] tableData = rows.toArray(new Object[0][]);
//        JTable table = new JTable(tableData, columnNames);
//        table.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                int row = table.rowAtPoint(e.getPoint());
//                int col = table.columnAtPoint(e.getPoint());
//                if (row >= 0 && col == 1) {
//                    String drugName = (String) table.getValueAt(row, col);
//                    String mechanism = (String) table.getValueAt(row, 4);
//                    JOptionPane.showMessageDialog(null, "Drug: " + drugName + "\nMechanism: " + mechanism, "Drug Info", JOptionPane.INFORMATION_MESSAGE);
//                }
//            }
//        });
//        JScrollPane tableScroll = new JScrollPane(table);
//        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, tableScroll);
//        splitPane.setDividerLocation(700);
//        JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.add(splitPane, BorderLayout.CENTER);
//        frame.setSize(1200, 800);
//        frame.setVisible(true);
//    }
//
//    // Metoda main de test pentru GeneInteractionNetworkv2 (opțională)
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> {
//            JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            frame.setSize(1200, 800);
//            frame.setLayout(new BorderLayout());
//            JPanel inputPanel = new JPanel();
//            JLabel keggLabel = new JLabel("KEGG ID:");
//            JTextField keggField = new JTextField("hsa:4210", 10);
//            JLabel geneLabel = new JLabel("Gene Symbol:");
//            JTextField geneField = new JTextField("TP53", 10);
//            JButton loadButton = new JButton("Load");
//            inputPanel.add(keggLabel);
//            inputPanel.add(keggField);
//            inputPanel.add(geneLabel);
//            inputPanel.add(geneField);
//            inputPanel.add(loadButton);
//            frame.add(inputPanel, BorderLayout.NORTH);
//            JTabbedPane tabbedPane = new JTabbedPane();
//            frame.add(tabbedPane, BorderLayout.CENTER);
//            JPanel networkPanel = new JPanel(new BorderLayout());
//            JPanel drugPanel = new JPanel(new BorderLayout());
//            tabbedPane.addTab("Network", networkPanel);
//            tabbedPane.addTab("Drug Repurposing", drugPanel);
//            Runnable loadData = () -> {
//                String geneKEGGId = keggField.getText().trim();
//                String geneSymbol = geneField.getText().trim();
//                try {
//                    InteractionData data = fetchKEGGInteractions(geneKEGGId, geneSymbol);
//                    networkPanel.removeAll();
//                    Viewer viewer = createGraphViewer(data);
//                    networkPanel.add(viewer.getDefaultView(), BorderLayout.CENTER);
//                    networkPanel.revalidate();
//                    networkPanel.repaint();
//                    drugPanel.removeAll();
//                    Map<String, List<DrugInfo>> drugDB = loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
//                    List<Object[]> rows = filterDrugsForGene(data, drugDB);
//                    String[] columnNames = { "Gene", "Drug", "Indication", "Repurposing Score", "Mechanism" };
//                    JTable table = new JTable(rows.toArray(new Object[0][]), columnNames);
//                    table.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            int row = table.rowAtPoint(e.getPoint());
//                            int col = table.columnAtPoint(e.getPoint());
//                            if (row >= 0 && col == 1) {
//                                String drugName = (String) table.getValueAt(row, col);
//                                String mechanism = (String) table.getValueAt(row, 4);
//                                JOptionPane.showMessageDialog(frame, "Drug: " + drugName + "\nMechanism: " + mechanism, "Drug Info", JOptionPane.INFORMATION_MESSAGE);
//                            }
//                        }
//                    });
//                    drugPanel.add(new JScrollPane(table), BorderLayout.CENTER);
//                    drugPanel.revalidate();
//                    drugPanel.repaint();
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
//                }
//            };
//            loadData.run();
//            loadButton.addActionListener(e -> loadData.run());
//            frame.setVisible(true);
//        });
//    }
//}
package GUI;

import Data.PathwayParser;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
public class GeneInteractionNetworkv2 {

    // Data classes pentru gene și relații.
    public static class GeneNode {
        String id;
        String symbol;
        String type; // "central", "interactor", "similar", "related"
        public GeneNode(String id, String symbol, String type) {
            this.id = id;
            this.symbol = symbol;
            this.type = type;
        }
    }

    public static class GeneEdge {
        String id;
        String source;
        String target;
        String interaction;
        public GeneEdge(String id, String source, String target, String interaction) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.interaction = interaction;
        }
    }

    public static class InteractionData {
        List<GeneNode> nodes;
        List<GeneEdge> edges;
        public InteractionData(List<GeneNode> nodes, List<GeneEdge> edges) {
            this.nodes = nodes;
            this.edges = edges;
        }
    }

    // Clasa pentru informațiile despre medicamente.
    public static class DrugInfo {
        String geneSymbol;
        String drugName;
        String indication;
        String mechanism;
        public DrugInfo(String geneSymbol, String drugName, String indication, String mechanism) {
            this.geneSymbol = geneSymbol;
            this.drugName = drugName;
            this.indication = indication;
            this.mechanism = mechanism;
        }
    }

    // Metodă utilitară: preia textul de la o adresă URL.
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

    // Metoda de preluare a interacțiunilor din KEGG (vechea abordare)
    public static InteractionData fetchKEGGInteractions(String geneKEGGId, String geneSymbol) throws Exception {
        String pathwayUrl = "https://rest.kegg.jp/link/pathway/" + geneKEGGId;
        String pathwayText = getTextFromUrl(pathwayUrl);
        String[] pathwayLines = pathwayText.trim().split("\n");
        if (pathwayLines.length == 0) {
            throw new Exception("Nu s-au găsit căi pentru gena " + geneSymbol);
        }
        String[] parts = pathwayLines[0].split("\t");
        if (parts.length < 2) {
            throw new Exception("Format neașteptat pentru calea: " + pathwayLines[0]);
        }
        String pathwayId = parts[1];
        System.out.println("Folosim calea: " + pathwayId);

        String kgmlUrl = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
        String kgmlText = getTextFromUrl(kgmlUrl);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document xmlDoc = dBuilder.parse(new ByteArrayInputStream(kgmlText.getBytes("UTF-8")));
        xmlDoc.getDocumentElement().normalize();

        Map<String, String> entryMap = new HashMap<>();
        NodeList entryList = xmlDoc.getElementsByTagName("entry");
        for (int i = 0; i < entryList.getLength(); i++) {
            org.w3c.dom.Node node = entryList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element entryElem = (Element) node;
                String type = entryElem.getAttribute("type");
                if ("gene".equals(type)) {
                    String id = entryElem.getAttribute("id");
                    String nameAttr = entryElem.getAttribute("name");
                    String[] nameParts = nameAttr.split(" ");
                    String symbol = (nameParts.length > 1) ? nameParts[1] : nameParts[0];
                    entryMap.put(id, symbol);
                }
            }
        }

        NodeList relationList = xmlDoc.getElementsByTagName("relation");
        Map<String, GeneNode> nodeMap = new HashMap<>();
        List<GeneEdge> edges = new ArrayList<>();

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
                    if (interactingSymbol == null) continue;
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

    // Metoda standard de creare a graficului (folosită și în noua abordare)
//    public static Viewer createGraphViewer(InteractionData data) {
//        SingleGraph graph = new SingleGraph("Gene-Gene Interaction Network");
//        String styleSheet = ""
//                + "graph { padding: 50px; }"
//                + "node { fill-color: #61bffc; size: 40px; text-size: 14; text-alignment: center; stroke-mode: plain; stroke-color: #555; stroke-width: 2; }"
//                + "node.central { fill-color: #aaffaa; shape: diamond; }"
//                + "node.similar { fill-color: #ffaaaa; }"
//                + "node.related { fill-color: #ffc107; }"
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
//
//        Viewer viewer = graph.display();
//        viewer.enableAutoLayout();
//        viewer.enableAutoLayout(new SpringBox(false));
//        ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
//
//        for (MouseListener ml : viewPanel.getMouseListeners()) {
//            viewPanel.removeMouseListener(ml);
//        }
//
//        viewPanel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                GraphicElement element = viewPanel.findNodeOrSpriteAt(e.getX(), e.getY());
//                if (element != null && element.getClass().getSimpleName().equals("Node")) {
//                    Node clickedNode = graph.getNode(element.getId());
//                    String clickedGeneId = clickedNode.getId();
//                    String clickedGeneSymbol = (String) clickedNode.getAttribute("ui.label");
//                    System.out.println("Clicked on gene: " + clickedGeneSymbol + " (" + clickedGeneId + ")");
//                    try {
//                        InteractionData newData = fetchKEGGInteractions(clickedGeneId, clickedGeneSymbol);
//                        graph.clear();
//                        for (GeneNode gene : newData.nodes) {
//                            Node n = graph.addNode(gene.id);
//                            n.setAttribute("ui.label", gene.symbol);
//                            n.setAttribute("ui.class", gene.type);
//                        }
//                        for (GeneEdge edge : newData.edges) {
//                            try {
//                                Edge eNew = graph.addEdge(edge.id, edge.source, edge.target, true);
//                                eNew.setAttribute("ui.label", edge.interaction);
//                            } catch (Exception ex) {
//                                // Dacă muchia există deja, o ignorăm.
//                            }
//                        }
//                        graph.setAttribute("ui.stylesheet", styleSheet);
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//        });
//        return viewer;
//    }




    public static Viewer createGraphViewer(InteractionData data) {
        SingleGraph graph = new SingleGraph("Gene-Gene Interaction Network");

        // ✔ Stil grafic pentru noduri și muchii
        String styleSheet = ""
                + "graph { padding: 50px; }"
                + "node { fill-color: #61bffc; size: 30px; text-size: 12; text-alignment: under; stroke-mode: plain; stroke-color: #444; stroke-width: 1.5px; }"
                + "node.central { fill-color: #aaffaa; shape: diamond; }"
                + "node.similar { fill-color: #ffaaaa; }"
                + "node.related { fill-color: #ffc107; }"
                + "edge { fill-color: #9dbaea; text-size: 10; }";

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true);
        graph.setStrict(false);

//       // ✔ Setări pentru îmbunătățirea layout-ului
//        graph.setAttribute("layout.quality", true);
//        graph.setAttribute("layout.stabilization-limit", 0.000005);

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
                // muchia există deja, o ignorăm
            }
        }
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        SpringBox layout = new SpringBox(false);
        layout.setStabilizationLimit(0.000005);  // cât mai lent = noduri cât mai împrăștiate
        layout.setForce(1.2);                    // forță de respingere mai mare
        viewer.enableAutoLayout(layout);
        // ✔ Viewer + SpringBox layout

        //viewer.enableAutoLayout(new SpringBox(false)); // layout fizic dinamic

        ViewPanel viewPanel = (ViewPanel) viewer.addDefaultView(false); // false = fără fereastră proprie

        // ✔ MouseListener curat
        for (MouseListener ml : viewPanel.getMouseListeners()) {
            viewPanel.removeMouseListener(ml);
        }

        viewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GraphicElement element = viewPanel.findNodeOrSpriteAt(e.getX(), e.getY());
                if (element != null && element.getClass().getSimpleName().equals("Node")) {
                    Node clickedNode = graph.getNode(element.getId());
                    String clickedGeneId = clickedNode.getId();
                    String clickedGeneSymbol = (String) clickedNode.getAttribute("ui.label");
                    System.out.println("Clicked on gene: " + clickedGeneSymbol + " (" + clickedGeneId + ")");
                    try {
                        InteractionData newData = fetchKEGGInteractions(clickedGeneId, clickedGeneSymbol);
                        graph.clear();
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
                                // muchia deja există
                            }
                        }
                        graph.setAttribute("ui.stylesheet", styleSheet);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        return viewer;
    }
    public static Component createKGMLGraph(String pathwayId) throws Exception {
        PathwayParser parser = new PathwayParser("dummyGene");
        List<PathwayParser.Entry> entries = parser.parseKGML(pathwayId);
        List<PathwayParser.Relation> relations = parser.parseRelations(pathwayId);

        List<GeneNode> nodes = new ArrayList<>();
        Map<String, GeneNode> nodeMap = new HashMap<>();
        for (PathwayParser.Entry e : entries) {
            GeneNode gn = new GeneNode(e.getId(), e.getName(), e.getType());
            nodes.add(gn);
            nodeMap.put(e.getId(), gn);
        }

        List<GeneEdge> edges = new ArrayList<>();
        for (PathwayParser.Relation r : relations) {
            String id1 = r.getEntry1().getId();
            String id2 = r.getEntry2().getId();
            if (nodeMap.containsKey(id1) && nodeMap.containsKey(id2)) {
                GeneEdge ge = new GeneEdge("r" + id1 + "_" + id2, id1, id2, r.getRelationType());
                edges.add(ge);
            }
        }
        InteractionData data = new InteractionData(nodes, edges);
        return createGraphViewer(data).getDefaultView();
    }

    // Noua metodă: construiește un graf pe baza genele din baza de date (din JSON),
    // conectând genele care împărtășesc cel puțin un pathway comun.
    public static InteractionData createGraphFromGenes(List<DataModel.Gene> allGenes, DataModel.Gene selectedGene) {
        Map<String, GeneNode> nodeMap = new HashMap<>();
        List<GeneEdge> edges = new ArrayList<>();

        // Nodul central – gena selectată
        nodeMap.put(selectedGene.getName(), new GeneNode(selectedGene.getName(), selectedGene.getName(), "central"));

        // Adaugă noduri pentru genele care au cel puțin un pathway comun cu gena selectată.
        for (DataModel.Gene gene : allGenes) {
            if (!gene.getName().equalsIgnoreCase(selectedGene.getName())) {
                boolean hasCommonPathway = false;
                for (DataModel.PathWay p1 : selectedGene.getPathWays()) {
                    for (DataModel.PathWay p2 : gene.getPathWays()) {
                        if (p1.getPath().equals(p2.getPath())) {
                            hasCommonPathway = true;
                            break;
                        }
                    }
                    if (hasCommonPathway) break;
                }
                if (hasCommonPathway) {
                    nodeMap.put(gene.getName(), new GeneNode(gene.getName(), gene.getName(), "related"));
                }
            }
        }

        // Creează muchii între genele care împărtășesc cel puțin un pathway comun.
        List<String> geneNames = new ArrayList<>(nodeMap.keySet());
        for (int i = 0; i < geneNames.size(); i++) {
            for (int j = i + 1; j < geneNames.size(); j++) {
                DataModel.Gene gene1 = findGeneByName(allGenes, geneNames.get(i));
                DataModel.Gene gene2 = findGeneByName(allGenes, geneNames.get(j));
                if (gene1 != null && gene2 != null && shareCommonPathway(gene1, gene2)) {
                    String edgeId = "e_" + gene1.getName() + "_" + gene2.getName();
                    edges.add(new GeneEdge(edgeId, gene1.getName(), gene2.getName(), "common_pathway"));
                }
            }
        }
        return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
    }

    // Metodă utilitară pentru a găsi o genă după nume în lista de gene.
    private static DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String name) {
        for (DataModel.Gene gene : genes) {
            if (gene.getName().equalsIgnoreCase(name)) {
                return gene;
            }
        }
        return null;
    }

    // Verifică dacă două gene au cel puțin un pathway comun.
    private static boolean shareCommonPathway(DataModel.Gene gene1, DataModel.Gene gene2) {
        for (DataModel.PathWay p1 : gene1.getPathWays()) {
            for (DataModel.PathWay p2 : gene2.getPathWays()) {
                if (p1.getPath().equals(p2.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Metoda nouă: încarcă harta medicamentelor din fișierul JSON "generated_gene_drug_summary.json"
    public static Map<String, List<DrugInfo>> loadDrugMapFromJSON(String filePath) {
        Map<String, List<DrugInfo>> drugMap = new HashMap<>();
        try {
            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }
            br.close();
            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String drugName = obj.getString("Drug");
                JSONArray geneArray = obj.getJSONArray("Gene");
                JSONArray diseaseArray = obj.getJSONArray("Disease");
                JSONArray mechanismArray = obj.getJSONArray("Mechanism");
                // Convertește array-urile de stringuri în câte un string concatenat
                String indication = joinJSONArray(diseaseArray, ", ");
                String mechanism = joinJSONArray(mechanismArray, ", ");
                // Pentru fiecare genă din array, adaugă o intrare în mapă
                for (int j = 0; j < geneArray.length(); j++) {
                    String gene = geneArray.getString(j).trim();
                    DrugInfo info = new DrugInfo(gene, drugName, indication, mechanism);
                    drugMap.computeIfAbsent(gene, k -> new ArrayList<>()).add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drugMap;
    }

    // Metodă utilitară pentru a uni elementele unui JSONArray într-un singur string
    private static String joinJSONArray(JSONArray array, String delimiter) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        return String.join(delimiter, list);
    }
    public static List<Object[]> filterDrugsForGene(InteractionData data, Map<String, List<DrugInfo>> drugDB) {
        List<Object[]> rows = new ArrayList<>();
        for (GeneNode gene : data.nodes) {
            List<DrugInfo> drugs = drugDB.get(gene.symbol);
            if (drugs != null) {
                for (DrugInfo drug : drugs) {
                    rows.add(new Object[]{gene.symbol, drug.drugName, drug.indication, drug.mechanism});
                }
            }
        }
        return rows;
    }

    // Metoda de afișare a rețelei și sugestiilor de repurposing într-o interfață integrată.
    public static void displayNetworkAndDrugSuggestions(InteractionData data) {
        Viewer viewer = createGraphViewer(data);
        Component graphPanel = viewer.getDefaultView();
        // Folosește noua metodă pentru a încărca harta medicamentelor din JSON
        Map<String, List<DrugInfo>> drugDB = loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
        List<Object[]> rows = new ArrayList<>();
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
                    for (DrugInfo drug : entry.getValue()) {
                        rows.add(new Object[]{gene.symbol, drug.drugName, drug.indication, score, drug.mechanism});
                    }
                }
            }
        }
        String[] columnNames = { "Gene", "Drug", "Indication", "Repurposing Score", "Mechanism" };
        Object[][] tableData = rows.toArray(new Object[0][]);
        JTable table = new JTable(tableData, columnNames);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 1) {
                    String drugName = (String) table.getValueAt(row, col);
                    String mechanism = (String) table.getValueAt(row, 4);
                    JOptionPane.showMessageDialog(null, "Drug: " + drugName + "\nMechanism: " + mechanism, "Drug Info", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        JScrollPane tableScroll = new JScrollPane(table);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, tableScroll);
        splitPane.setDividerLocation(700);
        JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.setSize(1200, 800);
        frame.setVisible(true);
    }

    // Metoda main de test pentru GeneInteractionNetworkv2 (opțională)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLayout(new BorderLayout());
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
            JTabbedPane tabbedPane = new JTabbedPane();
            frame.add(tabbedPane, BorderLayout.CENTER);
            JPanel networkPanel = new JPanel(new BorderLayout());
            JPanel drugPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab("Network", networkPanel);
            tabbedPane.addTab("Drug Repurposing", drugPanel);
            Runnable loadData = () -> {
                String geneKEGGId = keggField.getText().trim();
                String geneSymbol = geneField.getText().trim();
                try {
                    InteractionData data = fetchKEGGInteractions(geneKEGGId, geneSymbol);
                    networkPanel.removeAll();
                    Viewer viewer = createGraphViewer(data);
                    networkPanel.add(viewer.getDefaultView(), BorderLayout.CENTER);
                    networkPanel.revalidate();
                    networkPanel.repaint();
                    drugPanel.removeAll();
                    Map<String, List<DrugInfo>> drugDB = loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
                    List<Object[]> rows = filterDrugsForGene(data, drugDB);
                    String[] columnNames = { "Gene", "Drug", "Indication", "Repurposing Score", "Mechanism" };
                    JTable table = new JTable(rows.toArray(new Object[0][]), columnNames);
                    table.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int row = table.rowAtPoint(e.getPoint());
                            int col = table.columnAtPoint(e.getPoint());
                            if (row >= 0 && col == 1) {
                                String drugName = (String) table.getValueAt(row, col);
                                String mechanism = (String) table.getValueAt(row, 4);
                                JOptionPane.showMessageDialog(frame, "Drug: " + drugName + "\nMechanism: " + mechanism, "Drug Info", JOptionPane.INFORMATION_MESSAGE);
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
            loadData.run();
            loadButton.addActionListener(e -> loadData.run());
            frame.setVisible(true);
        });
    }
}