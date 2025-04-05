//package Data;
//
//import javax.swing.*;
//import javax.swing.table.DefaultTableModel;
//import java.awt.*;
//import java.awt.event.*;
//import java.io.*;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.List;
//
//import org.graphstream.ui.view.Viewer;
//
//public class graphv3 {
//
//    public static void launchInteractiveUI() {
//        JFrame frame = new JFrame("Gene Explorer");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLayout(new BorderLayout());
//
//        // Top Panel: Search bar + Explore button
//        JPanel topPanel = new JPanel();
//        JTextField geneInput = new JTextField(20);
//        JButton exploreButton = new JButton("Explore");
//        topPanel.add(new JLabel("Enter Gene:"));
//        topPanel.add(geneInput);
//        topPanel.add(exploreButton);
//        frame.add(topPanel, BorderLayout.NORTH);
//
//        // Placeholder panel for split content
//        JPanel contentPanel = new JPanel(new BorderLayout());
//        frame.add(contentPanel, BorderLayout.CENTER);
//
//        exploreButton.addActionListener(e -> {
//            String geneSymbol = geneInput.getText().trim().toUpperCase();
//            if (geneSymbol.isEmpty()) {
//                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.");
//                return;
//            }
//            try {
//                // Guess KEGG ID from symbol (for demo purposes, replace with real mapping if needed)
//                String geneKEGGId = "hsa:" + fetchNCBIGeneId(geneSymbol); // You can cache this
//
//                InteractionData data = fetchKEGGInteractions(geneKEGGId, geneSymbol);
//                contentPanel.removeAll();
//
//                // Viewer
//                Viewer viewer = createGraphViewer(data);
//                Component graphPanel = viewer.getDefaultView();
//
//                // Drug Table
//                Map<String, List<DrugInfo>> drugDB = loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
//                List<Object[]> rows = filterDrugsForGene(data, drugDB);
//
//                String[] columnNames = {"Gene", "Drug", "Indication", "Repurposing Score", "Mechanism"};
//                JTable table = new JTable(rows.toArray(new Object[0][]), columnNames);
//
//                // Click listener
//                table.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        int row = table.rowAtPoint(e.getPoint());
//                        int col = table.columnAtPoint(e.getPoint());
//                        if (row >= 0 && col == 1) {
//                            String drugName = (String) table.getValueAt(row, col);
//                            String mechanism = (String) table.getValueAt(row, 4);
//                            JOptionPane.showMessageDialog(null,
//                                    "Drug: " + drugName + "\nMechanism: " + mechanism,
//                                    "Drug Info", JOptionPane.INFORMATION_MESSAGE);
//                        }
//                    }
//                });
//
//                JScrollPane tableScroll = new JScrollPane(table);
//                JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, tableScroll);
//                splitPane.setDividerLocation(700);
//
//                contentPanel.add(splitPane, BorderLayout.CENTER);
//                contentPanel.revalidate();
//                contentPanel.repaint();
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(frame, "Error fetching data for gene: " + geneSymbol);
//            }
//        });
//
//        frame.setSize(1200, 800);
//        frame.setLocationRelativeTo(null);
//        frame.setVisible(true);
//    }
//
//    // NCBI Gene ID lookup
//    public static String fetchNCBIGeneId(String geneSymbol) throws Exception {
//        String baseUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
//        String esearchUrl = baseUrl + "esearch.fcgi?db=gene&term=" + geneSymbol + "[gene]+AND+Homo+sapiens[orgn]&retmode=json";
//        URL url = new URL(esearchUrl);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("GET");
//
//        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = in.readLine()) != null) {
//            response.append(line);
//        }
//        in.close();
//
//        String json = response.toString();
//        int start = json.indexOf("\"IdList\":[\"") + 13;
//        int end = json.indexOf("\"]", start);
//        if (start < 0 || end < 0 || end <= start) {
//            throw new Exception("Gene ID not found in response");
//        }
//        return json.substring(start, end).split("\\\"")[0];
//    }
//
//    // Stub methods to be implemented or connected from existing codebase
//    public static Viewer createGraphViewer(InteractionData data) {
//        // Replace with real GraphStream viewer code
//        return null;
//    }
//
//    public static InteractionData fetchKEGGInteractions(String geneKEGGId, String geneSymbol) throws Exception {
//        // Replace with real KEGG interaction fetching logic
//        return null;
//    }
//
//    public static Map<String, List<DrugInfo>> loadDrugMapFromCSV(String filePath) {
//        // Replace with actual CSV loader that returns drugDB
//        return new HashMap<>();
//    }
//
//    public static List<Object[]> filterDrugsForGene(InteractionData data, Map<String, List<DrugInfo>> drugDB) {
//        List<Object[]> rows = new ArrayList<>();
//        for (GeneNode gene : data.nodes) {
//            List<DrugInfo> matched = drugDB.get(gene.symbol);
//            if (matched != null) {
//                int score = switch (gene.type) {
//                    case "similar" -> 10;
//                    case "interactor" -> 5;
//                    case "central" -> 0;
//                    default -> 0;
//                };
//                for (DrugInfo drug : matched) {
//                    rows.add(new Object[]{gene.symbol, drug.drug, drug.disease, score, drug.mechanism});
//                }
//            }
//        }
//        return rows;
//    }
//
//    // Sample data structure placeholders
//    public static class DrugInfo {
//        public String gene;
//        public String drug;
//        public String disease;
//        public String mechanism;
//        public DrugInfo(String gene, String drug, String disease, String mechanism) {
//            this.gene = gene;
//            this.drug = drug;
//            this.disease = disease;
//            this.mechanism = mechanism;
//        }
//    }
//
//    public static class InteractionData {
//        public List<GeneNode> nodes;
//        public InteractionData(List<GeneNode> nodes) {
//            this.nodes = nodes;
//        }
//    }
//
//    public static class GeneNode {
//        public String symbol;
//        public String type; // central, interactor, similar
//        public GeneNode(String symbol, String type) {
//            this.symbol = symbol;
//            this.type = type;
//        }
//    }
//}
