//package Data;
//
//
//import org.graphstream.ui.swingViewer.ViewPanel;
//import org.json.JSONArray;
//import org.json.JSONObject;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.util.List;
//import java.util.Map;
//
//public class GUIFinal {
//
//    private JTextArea geneOverviewArea;
//    private JPanel networkPanel;
//    private JScrollPane drugScrollPane;
//    private JSplitPane splitPane;
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
//    }
//
//    public void createAndShowGUI() {
//        JFrame frame = new JFrame("Gene Explorer");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1400, 900);
//
//        // Câmp + Buton sus
//        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JTextField geneInput = new JTextField(20);
//        JButton exploreButton = new JButton("🔍 Explore");
//
//        topPanel.add(new JLabel("Enter Gene Symbol:"));
//        topPanel.add(geneInput);
//        topPanel.add(exploreButton);
//
//        // Zona stângă pentru overview
//        geneOverviewArea = new JTextArea();
//        geneOverviewArea.setEditable(false);
//        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
//        overviewScrollPane.setPreferredSize(new Dimension(400, 800));
//
//        // Zona dreaptă combinată (network + drug table)
//        networkPanel = new JPanel(new BorderLayout());
//
//        // SplitPane principal
//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
//        splitPane.setDividerLocation(400);
//
//        frame.setLayout(new BorderLayout());
//        frame.add(topPanel, BorderLayout.NORTH);
//        frame.add(splitPane, BorderLayout.CENTER);
//        frame.setVisible(true);
//
//        // Acțiune la click pe "Explore"
//        exploreButton.addActionListener(e -> {
//            String geneSymbol = geneInput.getText().trim();
//            if (!geneSymbol.isEmpty()) {
//                exploreGene(geneSymbol);
//            } else {
//                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
//            }
//        });
//    }
//
//    private void exploreGene(String geneSymbol) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                // Afișează overview
//                String overview = GeneOverview.fetchGeneInfoText(geneSymbol);
//                geneOverviewArea.setText(overview);
//
//                // Extrage KEGG ID din overview
//                String[] lines = overview.split("\n");
//                String keggId = null;
//                for (String line : lines) {
//                    if (line.startsWith("🧭 KEGG ID:")) {
//                        keggId = line.replace("🧭 KEGG ID:", "").trim();
//                        break;
//                    }
//                }
//
//                if (keggId == null || !keggId.contains(":")) {
//                    geneOverviewArea.append("\n❌ Could not extract KEGG ID.\n");
//                    return;
//                }
//
//                // Preia și afișează interacțiunile + drug suggestions
//                GeneInteractionNetworkv2.InteractionData data =
//                        GeneInteractionNetworkv2.fetchKEGGInteractions(keggId, geneSymbol);
//
//                // Creează graficul
//                org.graphstream.ui.view.Viewer viewer =
//                        GeneInteractionNetworkv2.createGraphViewer(data);
//                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
//                viewPanel.setPreferredSize(new Dimension(700, 600));
//
//                // Creează tabelul
//                Map<String, List<GeneInteractionNetworkv2.DrugInfo>> drugMap =
//                        GeneInteractionNetworkv2.loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
//                List<Object[]> drugRows =
//                        GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
//                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
//                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
//
//                drugTable.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        int row = drugTable.rowAtPoint(e.getPoint());
//                        int col = drugTable.columnAtPoint(e.getPoint());
//                        if (col == 1) {
//                            String drug = (String) drugTable.getValueAt(row, col);
//                            String mech = (String) drugTable.getValueAt(row, 4);
//                            JOptionPane.showMessageDialog(null, "Drug: " + drug + "\nMechanism: " + mech,
//                                    "Drug Details", JOptionPane.INFORMATION_MESSAGE);
//                        }
//                    }
//                });
//
//                drugScrollPane = new JScrollPane(drugTable);
//                drugScrollPane.setPreferredSize(new Dimension(700, 200));
//
//                // Creează panou combinat
//                JPanel rightPanel = new JPanel(new BorderLayout());
//                rightPanel.add(viewPanel, BorderLayout.CENTER);
//                rightPanel.add(drugScrollPane, BorderLayout.SOUTH);
//
//                networkPanel.removeAll();
//                networkPanel.add(rightPanel, BorderLayout.CENTER);
//                networkPanel.revalidate();
//                networkPanel.repaint();
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(null,
//                        "❌ Error fetching gene data.\n" + ex.getMessage(),
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        });
//    }
//}
package GUI;

import org.graphstream.ui.swingViewer.ViewPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

public class GUIFinal {

    private JTextArea geneOverviewArea;
    private JPanel networkPanel;
    private JScrollPane drugScrollPane;
    private JSplitPane splitPane;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Gene Explorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);

        // Panou sus cu câmpul de input și butoanele
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField geneInput = new JTextField(20);
        JButton exploreButton = new JButton("🔍 Explore");
        JButton detailsButton = new JButton("See more details"); // Butonul adăugat

        topPanel.add(new JLabel("Enter Gene Symbol:"));
        topPanel.add(geneInput);
        topPanel.add(exploreButton);
        topPanel.add(detailsButton);  // Adăugare buton pe panou

        // Zona stângă pentru overview
        geneOverviewArea = new JTextArea();
        geneOverviewArea.setEditable(false);
        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
        overviewScrollPane.setPreferredSize(new Dimension(400, 800));

        // Zona dreaptă combinată (network + drug table)
        networkPanel = new JPanel(new BorderLayout());

        // SplitPane principal
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
        splitPane.setDividerLocation(400);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.setVisible(true);

        // Acțiune la click pe "Explore"
        exploreButton.addActionListener(e -> {
            String geneSymbol = geneInput.getText().trim();
            if (!geneSymbol.isEmpty()) {
                exploreGene(geneSymbol);
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Acțiune la click pe "See more details"
        detailsButton.addActionListener(e -> {
            // Deschide o nouă fereastră rulând main-ul din GeneInteractionNetworkv2
            SwingUtilities.invokeLater(() -> {
                GeneInteractionNetworkv2.main(new String[0]);
            });
        });
    }

    private void exploreGene(String geneSymbol) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Afișează overview
                String overview = GeneOverview.fetchGeneInfoText(geneSymbol);
                geneOverviewArea.setText(overview);

                // Extrage KEGG ID din overview
                String[] lines = overview.split("\n");
                String keggId = null;
                for (String line : lines) {
                    if (line.startsWith("🧭 KEGG ID:")) {
                        keggId = line.replace("🧭 KEGG ID:", "").trim();
                        break;
                    }
                }

                if (keggId == null || !keggId.contains(":")) {
                    geneOverviewArea.append("\n❌ Could not extract KEGG ID.\n");
                    return;
                }

                // Preia și afișează interacțiunile + drug suggestions
                GeneInteractionNetworkv2.InteractionData data =
                        GeneInteractionNetworkv2.fetchKEGGInteractions(keggId, geneSymbol);

                // Creează graficul
                org.graphstream.ui.view.Viewer viewer =
                        GeneInteractionNetworkv2.createGraphViewer(data);
                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
                viewPanel.setPreferredSize(new Dimension(700, 600));

                // Creează tabelul
                Map<String, List<GeneInteractionNetworkv2.DrugInfo>> drugMap =
                        GeneInteractionNetworkv2.loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
                List<Object[]> drugRows =
                        GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);

                drugTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = drugTable.rowAtPoint(e.getPoint());
                        int col = drugTable.columnAtPoint(e.getPoint());
                        if (col == 1) {
                            String drug = (String) drugTable.getValueAt(row, col);
                            String mech = (String) drugTable.getValueAt(row, 4);
                            JOptionPane.showMessageDialog(null, "Drug: " + drug + "\nMechanism: " + mech,
                                    "Drug Details", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });

                drugScrollPane = new JScrollPane(drugTable);
                drugScrollPane.setPreferredSize(new Dimension(700, 200));

                // Creează panou combinat
                JPanel rightPanel = new JPanel(new BorderLayout());
                rightPanel.add(viewPanel, BorderLayout.CENTER);
                rightPanel.add(drugScrollPane, BorderLayout.SOUTH);

                networkPanel.removeAll();
                networkPanel.add(rightPanel, BorderLayout.CENTER);
                networkPanel.revalidate();
                networkPanel.repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "❌ Error fetching gene data.\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}