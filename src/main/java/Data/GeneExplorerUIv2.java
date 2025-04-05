//package Data;
//
//
//
//import Data.GeneInteractionNetworkv2.*;
//import Data.GeneInteractionNetworkv2;
//import Data.GeneOverview;
//import org.graphstream.graph.implementations.SingleGraph;
//import org.graphstream.ui.view.Viewer;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.List;
//import java.util.Map;
//
//public class GeneExplorerUIv2 {
//    private JFrame frame;
//    private JTextField geneInput;
//    private JButton exploreButton;
//    private JTextArea geneInfoArea;
//    private JPanel networkPanel;
//    private JTable drugTable;
//    private JScrollPane drugScroll;
//    private JSplitPane mainSplit;
//    private JSplitPane rightSplit;
//
//    public GeneExplorerUIv2() {
//        buildUI();
//    }
//
//    private void buildUI() {
//        frame = new JFrame("🧬 Gene Explorer - Premium Network & Drug UI");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1400, 900);
//        frame.setLayout(new BorderLayout(10, 10));
//
//        // 🔝 Top panel
//        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        geneInput = new JTextField(20);
//        exploreButton = new JButton("🔍 Explore");
//        exploreButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
//        topPanel.add(new JLabel("Enter Gene:"));
//        topPanel.add(geneInput);
//        topPanel.add(exploreButton);
//        frame.add(topPanel, BorderLayout.NORTH);
//
//        // 📄 Left Panel - Gene Info
//        geneInfoArea = new JTextArea();
//        geneInfoArea.setEditable(false);
//        geneInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
//        JScrollPane geneScroll = new JScrollPane(geneInfoArea);
//        geneScroll.setPreferredSize(new Dimension(300, 600));
//
//        // 🌐 Center Panel - Graph Placeholder
//        networkPanel = new JPanel(new BorderLayout());
//
//        // 💊 Right Panel - Drug Table
//        drugTable = new JTable();
//        drugScroll = new JScrollPane(drugTable);
//
//        // 🧩 Split layout assembly
//        rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, networkPanel, drugScroll);
//        rightSplit.setResizeWeight(0.65);
//
//        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, geneScroll, rightSplit);
//        mainSplit.setResizeWeight(0.2);
//        frame.add(mainSplit, BorderLayout.CENTER);
//
//        // 🎬 Button Action
//        exploreButton.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                String gene = geneInput.getText().trim().toUpperCase();
//                if (!gene.isEmpty()) {
//                    exploreGene(gene);
//                }
//            }
//        });
//
//        frame.setVisible(true);
//    }
//
//    private void exploreGene(String gene) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                // 🧬 Step 1: Fetch Gene Overview
//                String info = GeneOverview.fetchGeneInfoText(gene); // <-- you need to extract that into a method!
//                geneInfoArea.setText(info);
//
//                // 🔗 Step 2: Build KEGG ID
//                String keggFindUrl= "https://rest.kegg.jp/find/genes/" + gene;
//                String keggData = GeneInteractionNetworkv2.getTextFromUrl(keggFindUrl);
//                String[] lines = keggData.split("\n");
//                String keggId = lines[0].split("\t")[0];
//
//
//                // 🧠 Step 3: Fetch interactions
//                GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.fetchKEGGInteractions(keggId, gene);
//
//                // 🔁 Step 4: Create graph panel
//                Viewer viewer = GeneInteractionNetworkv2.createGraphViewer(data);
//                Component graphView = viewer.getDefaultView();
//                networkPanel.removeAll();
//                networkPanel.add(graphView, BorderLayout.CENTER);
//                networkPanel.revalidate();
//
//                // 💊 Step 5: Fill drug table
//                Map<String,List<DrugInfo>> drugDB = GeneInteractionNetworkv2.loadDrugMapFromCSV("drug_repurposing_map.csv");
//                List<Object[]> rows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugDB);
//                String[] headers = { "Gene", "Drug", "Indication", "Score", "Mechanism" };
//                Object[][] tableData = rows.toArray(new Object[0][]);
//                drugTable.setModel(new javax.swing.table.DefaultTableModel(tableData, headers));
//
//            } catch (Exception ex) {
//                geneInfoArea.setText("❌ Error loading gene: " + ex.getMessage());
//                ex.printStackTrace();
//            }
//        });
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(GeneExplorerUI::new);
//    }
//}