//package GUI;
//
//
//
//import org.graphstream.graph.implementations.SingleGraph;
//import org.graphstream.graph.Node;
//import org.graphstream.graph.Edge;
//import org.graphstream.ui.view.Viewer;
//import org.graphstream.ui.swingViewer.ViewPanel;
//import org.graphstream.ui.graphicGraph.GraphicElement;
//
//import javax.swing.*;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//import org.w3c.dom.Document;
//
//import java.awt.*;
//import java.awt.event.*;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.util.*;
//import java.util.List;
//
///**
// * GeneExplorerUI integrează:
// * - Un câmp de input și butonul "Explore" în partea de sus.
// * - Un panou pe stânga cu informații despre genă.
// * - Un panou central cu rețeaua interactivă de gene.
// * - Un panou pe dreapta cu lista de medicamente pentru repurposing.
// * La clic pe un nod, se actualizează informațiile și lista de medicamente.
// */
//public class GeneExplorerUI extends JFrame {
//    // Componente UI
//    private JTextField geneInput;
//    private JButton exploreButton;
//    private JTextArea geneOverviewTextArea;
//    private JPanel networkPanel;
//    private JPanel drugPanel;
//
//    // Componente pentru rețeaua interactivă (GraphStream)
//    private SingleGraph graph;
//    private Viewer viewer;
//    private ViewPanel viewPanel;
//
//    public GeneExplorerUI() {
//        initUI();
//    }
//
//    private void initUI() {
//        setTitle("Gene Explorer");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(new BorderLayout(5, 5));
//
//        // Top Panel: Input pentru genă și butonul "Explore"
//        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
//        geneInput = new JTextField(20);
//        exploreButton = new JButton("Explore");
//        topPanel.add(new JLabel("Gene Name:"));
//        topPanel.add(geneInput);
//        topPanel.add(exploreButton);
//        add(topPanel, BorderLayout.NORTH);
//
//        // Left Panel: Informații despre genă
//        geneOverviewTextArea = new JTextArea(15, 20);
//        geneOverviewTextArea.setEditable(false);
//        geneOverviewTextArea.setLineWrap(true);
//        geneOverviewTextArea.setWrapStyleWord(true);
//        JScrollPane overviewScroll = new JScrollPane(geneOverviewTextArea);
//        JPanel leftPanel = new JPanel(new BorderLayout());
//        leftPanel.setBorder(BorderFactory.createTitledBorder("Gene Overview"));
//        leftPanel.add(overviewScroll, BorderLayout.CENTER);
//        add(leftPanel, BorderLayout.WEST);
//
//        // Center Panel: Vizualizarea rețelei de gene
//        networkPanel = new JPanel(new BorderLayout());
//        networkPanel.setPreferredSize(new Dimension(500, 500));
//        networkPanel.setBorder(BorderFactory.createTitledBorder("Gene Interaction Network"));
//        add(networkPanel, BorderLayout.CENTER);
//
//        // Right Panel: Medicamente pentru repurposing
//        drugPanel = new JPanel();
//        drugPanel.setLayout(new BoxLayout(drugPanel, BoxLayout.Y_AXIS));
//        drugPanel.setBorder(BorderFactory.createTitledBorder("Drug Repurposing"));
//        add(drugPanel, BorderLayout.EAST);
//
//        // Acțiunea pentru butonul "Explore"
//        exploreButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String gene = geneInput.getText().trim();
//                if (!gene.isEmpty()) {
//                    exploreGene(gene);
//                }
//            }
//        });
//
//        pack();
//        setLocationRelativeTo(null);
//        setVisible(true);
//    }
//
//    /**
//     * Execută explorarea pentru gena specificată:
//     * - Obține overview-ul genei.
//     * - Construiește rețeaua de interacțiuni.
//     * - Actualizează panoul cu medicamente pentru repurposing.
//     */
//    private void exploreGene(String gene) {
//        new Thread(() -> {
//            try {
//                // Actualizare UI: mesaj de loading
//                SwingUtilities.invokeLater(() -> {
//                    geneOverviewTextArea.setText("Loading gene overview for " + gene + "...");
//                    drugPanel.removeAll();
//                    drugPanel.revalidate();
//                    drugPanel.repaint();
//                });
//
//                // Obținere informații despre genă (exemplu dummy, integrați codul real din GeneOverview dacă doriți)
//                String overview = fetchGeneOverview(gene);
//                SwingUtilities.invokeLater(() -> geneOverviewTextArea.setText(overview));
//
//                // Construim rețeaua de interacțiuni – folosim metoda statică din clasa GeneInteractionNetwork
//                // Pentru exemplu, se folosește KEGG id "hsa:1956" ca fallback
//                InteractionData data = GeneInteractionNetwork.fetchKEGGInteractions("hsa:1956", gene);
//                buildGraph(data);
//
//                // Obținere informații despre medicamente (date dummy)
//                List<String> drugs = fetchDrugRepurposing(gene);
//                SwingUtilities.invokeLater(() -> {
//                    drugPanel.removeAll();
//                    drugPanel.add(new JLabel("Repurposing Drugs for " + gene + ":"));
//                    for (String drug : drugs) {
//                        drugPanel.add(new JLabel("- " + drug));
//                    }
//                    drugPanel.revalidate();
//                    drugPanel.repaint();
//                });
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                SwingUtilities.invokeLater(() -> geneOverviewTextArea.setText("Error: " + ex.getMessage()));
//            }
//        }).start();
//    }
//
//    // Metodă exemplu pentru obținerea overview-ului genei (puteți integra apeluri reale către NCBI/KEGG)
//    private String fetchGeneOverview(String gene) throws Exception {
//        return "Gene: " + gene +
//                "\nDescription: Sample gene description." +
//                "\nChromosome: 7" +
//                "\nOrganism: Homo sapiens";
//    }
//
//    // Metodă exemplu care returnează o listă dummy de medicamente pentru repurposing
//    private List<String> fetchDrugRepurposing(String gene) {
//        List<String> drugs = new ArrayList<>();
//        drugs.add("Drug A");
//        drugs.add("Drug B");
//        drugs.add("Drug C");
//        return drugs;
//    }
//
//    /**
//     * Construcția și afișarea graficului interactiv folosind GraphStream.
//     */
//    private void buildGraph(InteractionData data) {
//        // Închidem viewer-ul anterior, dacă există
//        if (viewer != null) {
//            viewer.close();
//        }
//        // Creăm un nou graf
//        graph = new SingleGraph("Gene Interaction Network");
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
//        // Adăugare noduri
//        for (GeneNode gene : data.nodes) {
//            Node n = graph.addNode(gene.id);
//            n.setAttribute("ui.label", gene.symbol);
//            n.setAttribute("ui.class", gene.type);
//        }
//        // Adăugare arce
//        for (GeneEdge edge : data.edges) {
//            try {
//                Edge e = graph.addEdge(edge.id, edge.source, edge.target, true);
//                e.setAttribute("ui.label", edge.interaction);
//            } catch (Exception ex) {
//                // Ignore duplicate edges
//            }
//        }
//
//        // Creăm viewer-ul și ViewPanel-ul pentru integrarea în Swing
//        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
//        viewer.enableAutoLayout();
//        viewPanel = (ViewPanel) viewer.addDefaultView(false);
//
//        // Adăugăm listener pentru clic pe nod: actualizează overview-ul și lista de medicamente
//        viewPanel.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                GraphicElement element = viewPanel.findNodeOrSpriteAt(e.getX(), e.getY());
//                if (element != null) {
//                    String nodeId = element.getId();
//                    Node clicked = graph.getNode(nodeId);
//                    String geneSymbol = clicked.getAttribute("ui.label");
//                    updateForGeneClick(geneSymbol);
//                }
//            }
//        });
//
//        // Inserăm ViewPanel-ul în panoul central
//        SwingUtilities.invokeLater(() -> {
//            networkPanel.removeAll();
//            networkPanel.add(viewPanel, BorderLayout.CENTER);
//            networkPanel.revalidate();
//            networkPanel.repaint();
//        });
//    }
//
//    /**
//     * Actualizează overview-ul genei și lista de medicamente când se face clic pe un nod.
//     */
//    private void updateForGeneClick(String geneSymbol) {
//        new Thread(() -> {
//            try {
//                String overview = fetchGeneOverview(geneSymbol);
//                List<String> drugs = fetchDrugRepurposing(geneSymbol);
//                SwingUtilities.invokeLater(() -> {
//                    geneOverviewTextArea.setText(overview);
//                    drugPanel.removeAll();
//                    drugPanel.add(new JLabel("Repurposing Drugs for " + geneSymbol + ":"));
//                    for (String drug : drugs) {
//                        drugPanel.add(new JLabel("- " + drug));
//                    }
//                    drugPanel.revalidate();
//                    drugPanel.repaint();
//                });
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }).start();
//    }
//
//    // Main: inițializarea aplicației
//    public static void main(String[] args) {
//        // Setare Look & Feel nativ
//        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ex) { }
//        SwingUtilities.invokeLater(() -> new GeneExplorerUI());
//    }
//
//    // ============================
//    // Clase pentru datele de interacțiune (preluate din KEGG)
//    // ============================
//    public static class GeneNode {
//        public String id;
//        public String symbol;
//        public String type; // "central", "interactor", "similar"
//
//        public GeneNode(String id, String symbol, String type) {
//            this.id = id;
//            this.symbol = symbol;
//            this.type = type;
//        }
//    }
//
//    public static class GeneEdge {
//        public String id;
//        public String source;
//        public String target;
//        public String interaction;
//
//        public GeneEdge(String id, String source, String target, String interaction) {
//            this.id = id;
//            this.source = source;
//            this.target = target;
//            this.interaction = interaction;
//        }
//    }
//
//    public static class InteractionData {
//        public List<GeneNode> nodes;
//        public List<GeneEdge> edges;
//
//        public InteractionData(List<GeneNode> nodes, List<GeneEdge> edges) {
//            this.nodes = nodes;
//            this.edges = edges;
//        }
//    }
//
//    /**
//     * Clasa statică GeneInteractionNetwork se ocupă de preluarea datelor de interacțiuni din KEGG.
//     * Se folosește metoda fetchKEGGInteractions pentru a construi un grafic de gene.
//     */
//    public static class GeneInteractionNetwork {
//        // Metodă utilitară: preia text simplu de la o adresă URL
//        public static String getTextFromUrl(String urlString) throws Exception {
//            URL url = new URL(urlString);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder response = new StringBuilder();
//            String inputLine;
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine).append("\n");
//            }
//            in.close();
//            return response.toString();
//        }
//
//        /**
//         * Preia datele de interacțiuni din KEGG pentru o genă centrală.
//         * Se folosește primul pathway returnat și se extrag interacțiunile care implică gena centrală.
//         */
//        public static InteractionData fetchKEGGInteractions(String geneKEGGId, String geneSymbol) throws Exception {
//            // Obținerea căilor (pathways) pentru genă
//            String pathwayUrl = "https://rest.kegg.jp/link/pathway/" + geneKEGGId;
//            String pathwayText = getTextFromUrl(pathwayUrl);
//            String[] pathwayLines = pathwayText.trim().split("\n");
//            if (pathwayLines.length == 0) {
//                throw new Exception("No pathways found for gene " + geneSymbol);
//            }
//            String[] parts = pathwayLines[0].split("\t");
//            String pathwayId = parts[1]; // ex: "path:hsa05200"
//            System.out.println("Using pathway: " + pathwayId);
//
//            // Preluăm KGML-ul (XML) pentru pathway-ul ales
//            String kgmlUrl = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
//            String kgmlText = getTextFromUrl(kgmlUrl);
//            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//            Document xmlDoc = dBuilder.parse(new java.io.ByteArrayInputStream(kgmlText.getBytes("UTF-8")));
//            xmlDoc.getDocumentElement().normalize();
//
//            // Construim o hartă de la id-ul entry la simbolul genei
//            Map<String, String> entryMap = new HashMap<>();
//            NodeList entryList = xmlDoc.getElementsByTagName("entry");
//            for (int i = 0; i < entryList.getLength(); i++) {
//                org.w3c.dom.Node node = entryList.item(i);
//                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
//                    Element entryElem = (Element) node;
//                    String type = entryElem.getAttribute("type");
//                    if ("gene".equals(type)) {
//                        String id = entryElem.getAttribute("id");
//                        String nameAttr = entryElem.getAttribute("name"); // ex: "hsa:1956 EGFR"
//                        String[] nameParts = nameAttr.split(" ");
//                        String symbol = (nameParts.length > 1) ? nameParts[1] : nameParts[0];
//                        entryMap.put(id, symbol);
//                    }
//                }
//            }
//
//            // Parcurgem relațiile pentru a extrage interacțiunile care implică gena centrală
//            NodeList relationList = xmlDoc.getElementsByTagName("relation");
//            Map<String, GeneNode> nodeMap = new HashMap<>();
//            List<GeneEdge> edges = new ArrayList<>();
//
//            // Adăugăm nodul central
//            String centralEntryId = null;
//            for (Map.Entry<String, String> entry : entryMap.entrySet()) {
//                if (geneSymbol.equals(entry.getValue())) {
//                    centralEntryId = entry.getKey();
//                    break;
//                }
//            }
//            if (centralEntryId == null) {
//                centralEntryId = geneKEGGId;
//            }
//            nodeMap.put(centralEntryId, new GeneNode(centralEntryId, geneSymbol, "central"));
//
//            // Parcurgem relațiile pentru nodurile care implică gena centrală
//            for (int i = 0; i < relationList.getLength(); i++) {
//                org.w3c.dom.Node relNode = relationList.item(i);
//                if (relNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
//                    Element relElem = (Element) relNode;
//                    String entry1 = relElem.getAttribute("entry1");
//                    String entry2 = relElem.getAttribute("entry2");
//                    String symbol1 = entryMap.get(entry1);
//                    String symbol2 = entryMap.get(entry2);
//                    if (geneSymbol.equals(symbol1) || geneSymbol.equals(symbol2)) {
//                        String interactingEntryId = geneSymbol.equals(symbol1) ? entry2 : entry1;
//                        String interactingSymbol = entryMap.get(interactingEntryId);
//                        if (interactingSymbol == null)
//                            continue;
//                        String interactionType = "";
//                        NodeList subtypes = relElem.getElementsByTagName("subtype");
//                        if (subtypes.getLength() > 0) {
//                            Element subtypeElem = (Element) subtypes.item(0);
//                            interactionType = subtypeElem.getAttribute("name");
//                        }
//                        if (!nodeMap.containsKey(interactingEntryId)) {
//                            nodeMap.put(interactingEntryId, new GeneNode(interactingEntryId, interactingSymbol, "interactor"));
//                        }
//                        edges.add(new GeneEdge("e" + i, centralEntryId, interactingEntryId, interactionType));
//                    }
//                }
//            }
//
//            // Adăugăm până la 10 gene similare (care nu sunt direct conectate)
//            int similarCount = 0;
//            for (Map.Entry<String, String> entry : entryMap.entrySet()) {
//                String entryId = entry.getKey();
//                String symbol = entry.getValue();
//                if (!symbol.equals(geneSymbol) && !nodeMap.containsKey(entryId) && similarCount < 10) {
//                    nodeMap.put(entryId, new GeneNode(entryId, symbol, "similar"));
//                    edges.add(new GeneEdge("e_sim_" + entryId, centralEntryId, entryId, "similar"));
//                    similarCount++;
//                }
//            }
//
//            return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
//        }
//    }
//}
