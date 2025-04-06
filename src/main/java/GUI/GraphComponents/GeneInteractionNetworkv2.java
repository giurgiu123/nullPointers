
package GUI.GraphComponents;

import BusinessLogic.DrugManipulator;
import BusinessLogic.PathwayParser;
import DataModel.Entry;
import DataModel.GraphicsEntry;
import DataModel.Relation;
import GUI.DrugInfo;
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
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GeneInteractionNetworkv2 implements DrugManipulator {

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

    public static InteractionData fetchKEGGInteractions(String geneKEGGId, String geneSymbol) throws Exception {
        String pathwayUrl = "https://rest.kegg.jp/link/pathway/" + geneKEGGId;
        String pathwayText = getTextFromUrl(pathwayUrl);
        String[] pathwayLines = pathwayText.trim().split("\n");
        if (pathwayLines.length == 0) {
            throw new Exception("Nu s-au găsit căi pentru gena " + geneSymbol);
        }
        String[] parts = pathwayLines[0].split("\t"); // pentru tab
        if (parts.length < 2) {
            throw new Exception("Format neașteptat pentru calea: " + pathwayLines[0]);
        }
        String pathwayId = parts[1];
        System.out.println("Folosim calea: " + pathwayId);

        String kgmlUrl = "https://rest.kegg.jp/get/" + pathwayId + "/kgml"; //aici se face efectiv calea
        String kgmlText = getTextFromUrl(kgmlUrl);//folosim metoda sa luam tot
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document xmlDoc = dBuilder.parse(new ByteArrayInputStream(kgmlText.getBytes("UTF-8"))); // converteste intr un array de bytes cu codul UTF-8
        xmlDoc.getDocumentElement().normalize();// textul e posibil sa fie fragmentat in mai multe bucati ,
        // noi vrem sa fie unit intr un singur nod sa ne fie mai usor sa lucram cu el

        Map<String, String> entryMap = new HashMap<>();
        NodeList entryList = xmlDoc.getElementsByTagName("entry");
        for (int i = 0; i < entryList.getLength(); i++) {
            org.w3c.dom.Node node = entryList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element entryElem = (Element) node;
                String type = entryElem.getAttribute("type");
                if ("gene".equals(type)) {// pentru genele cu type ul gene (asta am observat din fisierul XML) - am incercat sa identificam un pattern
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
            if (!symbol.equals(geneSymbol) && !nodeMap.containsKey(entryId) && similarCount < 30) {//sa oprim cand ajungem la 30 de gene simil
                nodeMap.put(entryId, new GeneNode(entryId, symbol, "similar"));//face un nou nod
                edges.add(new GeneEdge("e_sim_" + entryId, centralEntryId, entryId, "similar"));// il adauga in muchii ca similar
                similarCount++;
            }
        }

        return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
    }


    public static Viewer createGraphViewer(InteractionData data) {
        SingleGraph graph = new SingleGraph("Gene-Gene Interaction Network");

        String styleSheet = ""
                + "graph { padding: 50px; }"
                + "node { fill-color: #61bffc; size: 30px; text-size: 20; text-alignment: under; stroke-mode: plain; stroke-color: #444; stroke-width: 1.5px; }"
                + "node.central { fill-color: #aaffaa; shape: diamond; }"
                + "node.similar { fill-color: #ffaaaa; }"
                + "node.related { fill-color: #ffc107; }"
                + "edge { fill-color: #9dbaea; text-size: 10; }";

        graph.setAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true);
        graph.setStrict(false);


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
                ex.printStackTrace();
            }
        }
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        SpringBox layout = new SpringBox(false);
        layout.setStabilizationLimit(0.000005);
        layout.setForce(1.2);
        viewer.enableAutoLayout(layout);
        ViewPanel viewPanel = (ViewPanel) viewer.addDefaultView(false);
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

private static GeneNode findNodeAt(Point p, List<GeneNode> nodes) {
    for (GeneNode node : nodes) {
        double dx = p.x - node.getX();
        double dy = p.y - node.getY();
        if (Math.sqrt(dx * dx + dy * dy) <= 12) {
            return node;
        }
    }
    return null;
}

    private static GeneNode getNodeById(List<GeneNode> nodes, String id) {
        for (GeneNode node : nodes) {
            if (node.getId().equals(id)) return node;
        }
        return null;
    }
    public static JComponent createHoverableGraphViewer(InteractionData data) {
        List<GeneNode> nodes = data.getNodes();
        List<GeneEdge> edges = data.getEdges();

        JPanel panel = new JPanel() {
            GeneNode hoveredNode = null;

            {
                setToolTipText("");
                addMouseMotionListener(new MouseMotionAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        Point p = e.getPoint();
                        GeneNode newHovered = findNodeAt(p, nodes);
                        if (newHovered != hoveredNode) {
                            hoveredNode = newHovered;
                            if (hoveredNode != null) {
                                setToolTipText("Type: " + hoveredNode.getName());
                            } else {
                                setToolTipText(null);
                            }
                            repaint();//reconstruieste vizual dar nu schimba nimic in memorie
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Desenează muchiile
                for (GeneEdge edge : edges) {
                    GeneNode src = getNodeById(nodes, edge.getFrom());
                    GeneNode tgt = getNodeById(nodes, edge.getTo());
                    if (src != null && tgt != null) {
                        g2.setColor(Color.LIGHT_GRAY);
                        g2.drawLine(src.getX(), src.getY(), tgt.getX(), tgt.getY());
                    }
                }

                // Desenează nodurile
                for (GeneNode node : nodes) {
                    Color nodeColor = node.getColor();
                    if (node.equals(hoveredNode)) {
                        g2.setColor(nodeColor.darker());
                        g2.fillOval(node.getX() - 12, node.getY() - 12, 24, 24);
                    } else {
                        g2.setColor(nodeColor);
                        g2.fillOval(node.getX() - 10, node.getY() - 10, 20, 20);
                    }
                }

                // Deseneaza legenda in colțul din stanga jos
                int legendX = 20;
                int legendY = getHeight() - 90;

                g2.setColor(Color.BLACK);
                g2.drawString("Legend:", legendX, legendY);

                drawLegendItem(g2, legendX, legendY + 20, new Color(128, 0, 128), "Inhibition");
                drawLegendItem(g2, legendX, legendY + 40, new Color(0, 180, 0), "Activation");
                drawLegendItem(g2, legendX, legendY + 60, Color.BLUE, "Isolated");
            }

            private void drawLegendItem(Graphics2D g2, int x, int y, Color color, String label) {
                g2.setColor(color);
                g2.fillOval(x, y - 8, 12, 12);
                g2.setColor(Color.BLACK);
                g2.drawString(label, x + 18, y + 2);
            }
        };

        panel.setPreferredSize(new Dimension(1000, 800));
        return panel;
    }
    public static Component createKGMLGraph(String pathwayId) throws Exception {
        PathwayParser parser = new PathwayParser("dummyGene");
        List<Entry> entries = parser.parseKGML(pathwayId);
        List<Relation> relations = parser.parseRelations(pathwayId);

        List<GeneNode> nodes = new ArrayList<>();
        Map<String, GeneNode> nodeMap = new HashMap<>();

        // Creăm nodurile și atribuim coordonate (sau random dacă lipsesc)
        for (Entry e : entries) {
            GeneNode gn = new GeneNode(e.getId(), e.getName(), e.getType());
            GraphicsEntry graphics = e.getGraphics();
            if (graphics != null) {
                gn.setX(graphics.getX());
                gn.setY(graphics.getY());
            } else {
                // fallback: coordonate random
                gn.setX(e.getGraphics().getX());
                gn.setY(e.getGraphics().getY());
            }
            nodes.add(gn);
            nodeMap.put(e.getId(), gn);
        }
// Calculam zona originală (bounding box)
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (GeneNode node : nodes) {
            minX = Math.min(minX, node.getX());
            maxX = Math.max(maxX, node.getX());
            minY = Math.min(minY, node.getY());
            maxY = Math.max(maxY, node.getY());
        }

// spatiul final dorit
        int targetWidth = 1500;
        int targetHeight = 1000;
        int padding = 50;

// calculez scale separat pe X și Y pentru a folosi întreg spatiul
        double scaleX = (targetWidth - 2.0 * padding) / (maxX - minX);
        double scaleY = (targetHeight - 2.0 * padding) / (maxY - minY);

// rescalez coordonatele fara pastrarea proportiei(stretch full screen)
        for (GeneNode node : nodes) {
            int originalX = node.getX();
            int originalY = node.getY();

            int newX = (int) ((originalX - minX) * scaleX + padding);
            int newY = (int) ((originalY - minY) * scaleY + padding);

            node.setX(newX);
            node.setY(newY);
        }

        List<GeneEdge> edges = new ArrayList<>();
        Map<String, String> nodeRelationTypes = new HashMap<>();

        // Construim muchiile si mapam tipurile de relatii pe noduri
        for (Relation r : relations) {
            String id1 = r.getEntry1().getId();
            String id2 = r.getEntry2().getId();
            String type = r.getRelationType();

            // salvam primul tip de relatie pentru fiecare nod
            nodeRelationTypes.putIfAbsent(id1, type);
            nodeRelationTypes.putIfAbsent(id2, type);

            if (nodeMap.containsKey(id1) && nodeMap.containsKey(id2)) {
                GeneEdge ge = new GeneEdge("r" + id1 + "_" + id2, id1, id2, type);
                edges.add(ge);
            }
        }

        // setam culoarea nodurilor in functie de tipul de relatie
        for (GeneNode node : nodes) {
            String relationType = nodeRelationTypes.get(node.getId());
            if ("inhibition".equalsIgnoreCase(relationType)) {
                node.setColor(new Color(128, 0, 128)); // mov
            } else if ("activation".equalsIgnoreCase(relationType)) {
                node.setColor(new Color(0, 180, 0)); // verde
            } else {
                node.setColor(Color.BLUE); // izolat sau alt tip
            }
        }

        InteractionData data = new InteractionData(nodes, edges);
        return createHoverableGraphViewer(data);
    }
    //  construieste un graf pe baza genele din baza de date json,
    // conectand genele care impartasesc cel puțin un pathway comun.
    public static InteractionData createGraphFromGenes(List<DataModel.Gene> allGenes, DataModel.Gene selectedGene) {
        Map<String, GeneNode> nodeMap = new HashMap<>();
        List<GeneEdge> edges = new ArrayList<>();

        // Nodul central – gena selectata
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

        // creeaza muchii intre genele care impartasesc cel putin un pathway comun.
        List<String> geneNames = new ArrayList<>(nodeMap.keySet());
        for (int i = 0; i < geneNames.size(); i++) {
            for (int j = i + 1; j < geneNames.size(); j++) {
                DataModel.Gene gene1 = findGeneByName(allGenes, geneNames.get(i));
                DataModel.Gene gene2 = findGeneByName(allGenes, geneNames.get(j));
                if (gene1 != null && gene2 != null && shareCommonPathway(gene1, gene2)) {
                    String edgeId = "e_" + gene1.getName() + "_" + gene2.getName();
                    edges.add(new GeneEdge(edgeId, gene1.getName(), gene2.getName(), ""));
                }
            }
        }
        return new InteractionData(new ArrayList<>(nodeMap.values()), edges);
    }

    // metoda utilitara pentru a gasi o gena dupa nume în lista de gene.
    private static DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String name) {
        for (DataModel.Gene gene : genes) {
            if (gene.getName().equalsIgnoreCase(name)) {
                return gene;
            }
        }
        return null;
    }

    // verifica dacă doua gene au cel puțin un pathway comun.
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

    public Map<String, List<DrugInfo>> loadDrugMapFromJSON(String filePath) {
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
                // converteste array-urile de stringuri in cate un string concatenat
                String indication = joinJSONArray(diseaseArray, ", ");
                String mechanism = joinJSONArray(mechanismArray, ", ");
                // pentru fiecare gena din array, adauga o intrare în mapa
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
    private static void stylizeTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(0, 120, 215));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
    }

    private static String joinJSONArray(JSONArray array, String delimiter) { //ca la indication si machanism pot fi mai multe separate prin virgula
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
                    rows.add(new Object[]{gene.symbol, drug.getDrugName(), drug.getIndication(), drug.getMechanism()});
                }
            }
        }
        return rows;
    }

    public void displayNetworkAndDrugSuggestions(InteractionData data) {
        Viewer viewer = createGraphViewer(data);
        Component graphPanel = viewer.getDefaultView();

        //harta med
        Map<String, List<DrugInfo>> drugDB = this.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
        List<Object[]> rows = new ArrayList<>();

        for (GeneNode gene : data.nodes) {
            for (Map.Entry<String, List<DrugInfo>> entry : drugDB.entrySet()) {
                String geneSymbol = entry.getKey();
                if (geneSymbol.equalsIgnoreCase(gene.symbol)) {
                    for (DrugInfo drug : entry.getValue()) {
                        rows.add(new Object[]{
                                gene.symbol,
                                drug.getDrugName(),
                                drug.getIndication(),
                                drug.getMechanism()
                        });
                    }
                }
            }
        }

        String[] columnNames = { "Gene", "Drug", "Indication", "Mechanism" };
        Object[][] tableData = rows.toArray(new Object[0][]);
        JTable table = new JTable(new DefaultTableModel(tableData, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // toate celulele devin non-editabile
            }
        });
        stylizeTable(table);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 1) {
                    String drugName = (String) table.getValueAt(row, col);
                    String mechanism = (String) table.getValueAt(row, 3); // index ajustat
                    JOptionPane.showMessageDialog(null,
                            "Drug: " + drugName + "\nMechanism: " + mechanism,
                            "Drug Info",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, tableScroll);
        splitPane.setDividerLocation(700);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            Window window = SwingUtilities.getWindowAncestor(backButton);
            if (window != null) window.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(backButton);

        JFrame frame = new JFrame("Gene Interaction Network & Drug Repurposing Suggestions");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }}