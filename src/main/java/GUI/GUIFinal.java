//////package Data;
//////
//////
//////import org.graphstream.ui.swingViewer.ViewPanel;
//////import org.json.JSONArray;
//////import org.json.JSONObject;
//////import javax.swing.*;
//////import java.awt.*;
//////import java.awt.event.*;
//////import java.util.List;
//////import java.util.Map;
//////
//////public class GUIFinal {
//////
//////    private JTextArea geneOverviewArea;
//////    private JPanel networkPanel;
//////    private JScrollPane drugScrollPane;
//////    private JSplitPane splitPane;
//////
//////    public static void main(String[] args) {
//////        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
//////    }
//////
//////    public void createAndShowGUI() {
//////        JFrame frame = new JFrame("Gene Explorer");
//////        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//////        frame.setSize(1400, 900);
//////
//////        // Câmp + Buton sus
//////        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//////        JTextField geneInput = new JTextField(20);
//////        JButton exploreButton = new JButton("🔍 Explore");
//////
//////        topPanel.add(new JLabel("Enter Gene Symbol:"));
//////        topPanel.add(geneInput);
//////        topPanel.add(exploreButton);
//////
//////        // Zona stângă pentru overview
//////        geneOverviewArea = new JTextArea();
//////        geneOverviewArea.setEditable(false);
//////        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//////        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
//////        overviewScrollPane.setPreferredSize(new Dimension(400, 800));
//////
//////        // Zona dreaptă combinată (network + drug table)
//////        networkPanel = new JPanel(new BorderLayout());
//////
//////        // SplitPane principal
//////        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
//////        splitPane.setDividerLocation(400);
//////
//////        frame.setLayout(new BorderLayout());
//////        frame.add(topPanel, BorderLayout.NORTH);
//////        frame.add(splitPane, BorderLayout.CENTER);
//////        frame.setVisible(true);
//////
//////        // Acțiune la click pe "Explore"
//////        exploreButton.addActionListener(e -> {
//////            String geneSymbol = geneInput.getText().trim();
//////            if (!geneSymbol.isEmpty()) {
//////                exploreGene(geneSymbol);
//////            } else {
//////                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
//////            }
//////        });
//////    }
//////
//////    private void exploreGene(String geneSymbol) {
//////        SwingUtilities.invokeLater(() -> {
//////            try {
//////                // Afișează overview
//////                String overview = GeneOverview.fetchGeneInfoText(geneSymbol);
//////                geneOverviewArea.setText(overview);
//////
//////                // Extrage KEGG ID din overview
//////                String[] lines = overview.split("\n");
//////                String keggId = null;
//////                for (String line : lines) {
//////                    if (line.startsWith("🧭 KEGG ID:")) {
//////                        keggId = line.replace("🧭 KEGG ID:", "").trim();
//////                        break;
//////                    }
//////                }
//////
//////                if (keggId == null || !keggId.contains(":")) {
//////                    geneOverviewArea.append("\n❌ Could not extract KEGG ID.\n");
//////                    return;
//////                }
//////
//////                // Preia și afișează interacțiunile + drug suggestions
//////                GeneInteractionNetworkv2.InteractionData data =
//////                        GeneInteractionNetworkv2.fetchKEGGInteractions(keggId, geneSymbol);
//////
//////                // Creează graficul
//////                org.graphstream.ui.view.Viewer viewer =
//////                        GeneInteractionNetworkv2.createGraphViewer(data);
//////                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
//////                viewPanel.setPreferredSize(new Dimension(700, 600));
//////
//////                // Creează tabelul
//////                Map<String, List<GeneInteractionNetworkv2.DrugInfo>> drugMap =
//////                        GeneInteractionNetworkv2.loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
//////                List<Object[]> drugRows =
//////                        GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
//////                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
//////                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
//////
//////                drugTable.addMouseListener(new MouseAdapter() {
//////                    @Override
//////                    public void mouseClicked(MouseEvent e) {
//////                        int row = drugTable.rowAtPoint(e.getPoint());
//////                        int col = drugTable.columnAtPoint(e.getPoint());
//////                        if (col == 1) {
//////                            String drug = (String) drugTable.getValueAt(row, col);
//////                            String mech = (String) drugTable.getValueAt(row, 4);
//////                            JOptionPane.showMessageDialog(null, "Drug: " + drug + "\nMechanism: " + mech,
//////                                    "Drug Details", JOptionPane.INFORMATION_MESSAGE);
//////                        }
//////                    }
//////                });
//////
//////                drugScrollPane = new JScrollPane(drugTable);
//////                drugScrollPane.setPreferredSize(new Dimension(700, 200));
//////
//////                // Creează panou combinat
//////                JPanel rightPanel = new JPanel(new BorderLayout());
//////                rightPanel.add(viewPanel, BorderLayout.CENTER);
//////                rightPanel.add(drugScrollPane, BorderLayout.SOUTH);
//////
//////                networkPanel.removeAll();
//////                networkPanel.add(rightPanel, BorderLayout.CENTER);
//////                networkPanel.revalidate();
//////                networkPanel.repaint();
//////
//////            } catch (Exception ex) {
//////                ex.printStackTrace();
//////                JOptionPane.showMessageDialog(null,
//////                        "❌ Error fetching gene data.\n" + ex.getMessage(),
//////                        "Error", JOptionPane.ERROR_MESSAGE);
//////            }
//////        });
//////    }
//////}
////package GUI;
////
////import org.graphstream.ui.swingViewer.ViewPanel;
////
////import javax.swing.*;
////import java.awt.*;
////import java.awt.event.*;
////import java.util.List;
////import java.util.Map;
////
////public class GUIFinal {
////
////    private JTextArea geneOverviewArea;
////    private JPanel networkPanel;
////    private JScrollPane drugScrollPane;
////    private JSplitPane splitPane;
////
////    public static void main(String[] args) {
////        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
////    }
////
////    public void createAndShowGUI() {
////        JFrame frame = new JFrame("Gene Explorer");
////        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////        frame.setSize(1400, 900);
////
////        // Panou sus cu câmpul de input și butoanele
////        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
////        JTextField geneInput = new JTextField(20);
////        JButton exploreButton = new JButton("🔍 Explore");
////        JButton detailsButton = new JButton("See more details"); // Butonul adăugat
////
////        topPanel.add(new JLabel("Enter Gene Symbol:"));
////        topPanel.add(geneInput);
////        topPanel.add(exploreButton);
////        topPanel.add(detailsButton);  // Adăugare buton pe panou
////
////        // Zona stângă pentru overview
////        geneOverviewArea = new JTextArea();
////        geneOverviewArea.setEditable(false);
////        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
////        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
////        overviewScrollPane.setPreferredSize(new Dimension(400, 800));
////
////        // Zona dreaptă combinată (network + drug table)
////        networkPanel = new JPanel(new BorderLayout());
////
////        // SplitPane principal
////        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
////        splitPane.setDividerLocation(400);
////
////        frame.setLayout(new BorderLayout());
////        frame.add(topPanel, BorderLayout.NORTH);
////        frame.add(splitPane, BorderLayout.CENTER);
////        frame.setVisible(true);
////
////        // Acțiune la click pe "Explore"
////        exploreButton.addActionListener(e -> {
////            String geneSymbol = geneInput.getText().trim();
////            if (!geneSymbol.isEmpty()) {
////                exploreGene(geneSymbol);
////            } else {
////                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
////            }
////        });
////
////        // Acțiune la click pe "See more details"
////        detailsButton.addActionListener(e -> {
////            // Deschide o nouă fereastră rulând main-ul din GeneInteractionNetworkv2
////            SwingUtilities.invokeLater(() -> {
////                GeneInteractionNetworkv2.main(new String[0]);
////            });
////        });
////    }
////
////    private void exploreGene(String geneSymbol) {
////        SwingUtilities.invokeLater(() -> {
////            try {
////                // Afișează overview
////                String overview = GeneOverview.fetchGeneInfoText(geneSymbol);
////                geneOverviewArea.setText(overview);
////
////                // Extrage KEGG ID din overview
////                String[] lines = overview.split("\n");
////                String keggId = null;
////                for (String line : lines) {
////                    if (line.startsWith("🧭 KEGG ID:")) {
////                        keggId = line.replace("🧭 KEGG ID:", "").trim();
////                        break;
////                    }
////                }
////
////                if (keggId == null || !keggId.contains(":")) {
////                    geneOverviewArea.append("\n❌ Could not extract KEGG ID.\n");
////                    return;
////                }
////
////                // Preia și afișează interacțiunile + drug suggestions
////                GeneInteractionNetworkv2.InteractionData data =
////                        GeneInteractionNetworkv2.fetchKEGGInteractions(keggId, geneSymbol);
////
////                // Creează graficul
////                org.graphstream.ui.view.Viewer viewer =
////                        GeneInteractionNetworkv2.createGraphViewer(data);
////                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
////                viewPanel.setPreferredSize(new Dimension(700, 600));
////
////                // Creează tabelul
////                Map<String, List<GeneInteractionNetworkv2.DrugInfo>> drugMap =
////                        GeneInteractionNetworkv2.loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
////                List<Object[]> drugRows =
////                        GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
////                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
////                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
////
////                drugTable.addMouseListener(new MouseAdapter() {
////                    @Override
////                    public void mouseClicked(MouseEvent e) {
////                        int row = drugTable.rowAtPoint(e.getPoint());
////                        int col = drugTable.columnAtPoint(e.getPoint());
////                        if (col == 1) {
////                            String drug = (String) drugTable.getValueAt(row, col);
////                            String mech = (String) drugTable.getValueAt(row, 4);
////                            JOptionPane.showMessageDialog(null, "Drug: " + drug + "\nMechanism: " + mech,
////                                    "Drug Details", JOptionPane.INFORMATION_MESSAGE);
////                        }
////                    }
////                });
////
////                drugScrollPane = new JScrollPane(drugTable);
////                drugScrollPane.setPreferredSize(new Dimension(700, 200));
////
////                // Creează panou combinat
////                JPanel rightPanel = new JPanel(new BorderLayout());
////                rightPanel.add(viewPanel, BorderLayout.CENTER);
////                rightPanel.add(drugScrollPane, BorderLayout.SOUTH);
////
////                networkPanel.removeAll();
////                networkPanel.add(rightPanel, BorderLayout.CENTER);
////                networkPanel.revalidate();
////                networkPanel.repaint();
////
////            } catch (Exception ex) {
////                ex.printStackTrace();
////                JOptionPane.showMessageDialog(null,
////                        "❌ Error fetching gene data.\n" + ex.getMessage(),
////                        "Error", JOptionPane.ERROR_MESSAGE);
////            }
////        });
////    }
////}
//package GUI;
//
//import GUI.GeneInteractionNetworkv2;
//import org.graphstream.ui.swingViewer.ViewPanel;
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
//    private List<DataModel.Gene> allGenes; // Lista de gene încărcată din JSON
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
//    }
//
//    public void createAndShowGUI() {
//        // Încărcăm lista de gene din JSON folosind clasa DbGene
//        Data.DbGene dbGene = new Data.DbGene();
//        allGenes = dbGene.getGenesFromJsonFile();
//
//        JFrame frame = new JFrame("Gene Explorer");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1400, 900);
//
//        // Panou superior cu câmpul de input și butoanele
//        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JTextField geneInput = new JTextField(20);
//        JButton exploreButton = new JButton("🔍 Explore");
//        JButton detailsButton = new JButton("See more details");
//
//        topPanel.add(new JLabel("Enter Gene Symbol:"));
//        topPanel.add(geneInput);
//        topPanel.add(exploreButton);
//        topPanel.add(detailsButton);
//
//        // Zona stângă pentru afișarea informațiilor despre genă
//        geneOverviewArea = new JTextArea();
//        geneOverviewArea.setEditable(false);
//        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
//        overviewScrollPane.setPreferredSize(new Dimension(400, 800));
//
//        // Zona dreaptă pentru rețea și tabelul cu medicamente
//        networkPanel = new JPanel(new BorderLayout());
//
//        // JSplitPane principal
//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
//        splitPane.setDividerLocation(400);
//
//        frame.setLayout(new BorderLayout());
//        frame.add(topPanel, BorderLayout.NORTH);
//        frame.add(splitPane, BorderLayout.CENTER);
//        frame.setVisible(true);
//
//        // Acțiunea butonului "Explore"
//        exploreButton.addActionListener(e -> {
//            String geneSymbol = geneInput.getText().trim();
//            if (!geneSymbol.isEmpty()) {
//                exploreGene(geneSymbol);
//            } else {
//                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
//            }
//        });
//
//        // Butonul "See more details" lansează interfața clasică de rețea
//        detailsButton.addActionListener(e -> {
//            SwingUtilities.invokeLater(() -> {
//                GeneInteractionNetworkv2.main(new String[0]);
//            });
//        });
//    }
//
//    private void exploreGene(String geneSymbol) {
//        SwingUtilities.invokeLater(() -> {
//            try {
//                DataModel.Gene selectedGene = findGeneByName(allGenes, geneSymbol);
//                if (selectedGene == null) {
//                    geneOverviewArea.setText("Gene not found in database.");
//                    return;
//                }
//
//                // Afișează informațiile despre genă
//                geneOverviewArea.setText(selectedGene.toString());
//
//                // Construiește graficul bazat pe pathway-uri comune
//                GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
//                org.graphstream.ui.view.Viewer viewer = GeneInteractionNetworkv2.createGraphViewer(data);
//                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
//                viewPanel.setPreferredSize(new Dimension(700, 600));
//
//                // Creează tabelul cu informații despre medicamente
//                Map<String, List<GeneInteractionNetworkv2.DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromCSV("src/main/resources/drug_repurposing_map.csv");
//                List<Object[]> drugRows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
//                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
//                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
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
//                drugScrollPane = new JScrollPane(drugTable);
//                drugScrollPane.setPreferredSize(new Dimension(700, 200));
//
//                // Panoul combinat: rețea + tabel
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
//
//    // Metodă utilitară pentru a găsi o genă după nume în lista încărcată
//    private DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String geneSymbol) {
//        for (DataModel.Gene gene : genes) {
//            if (gene.getName().equalsIgnoreCase(geneSymbol)) {
//                return gene;
//            }
//        }
//        return null;
//    }
//}
package GUI;


import org.graphstream.ui.swingViewer.ViewPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIFinal {

    private JTextArea geneOverviewArea;
    private JPanel networkPanel;
    private JScrollPane drugScrollPane;
    private JSplitPane splitPane;
    private List<DataModel.Gene> allGenes; // Lista de gene încărcată din JSON

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
    }

    public void createAndShowGUI() {
        // Încărcăm lista de gene din JSON folosind clasa DbGene
        Data.DbGene dbGene = new Data.DbGene();
        allGenes = dbGene.getGenesFromJsonFile();

        JFrame frame = new JFrame("Gene Explorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);

        // Panou superior cu câmpul de input și butoanele
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField geneInput = new JTextField(20);
        JButton exploreButton = new JButton("🔍 Explore");
        JButton detailsButton = new JButton("See more details");

        topPanel.add(new JLabel("Enter Gene Symbol:"));
        topPanel.add(geneInput);
        topPanel.add(exploreButton);
        topPanel.add(detailsButton);

        // Zona stângă pentru afișarea informațiilor despre genă
        geneOverviewArea = new JTextArea();
        geneOverviewArea.setEditable(false);
        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
        overviewScrollPane.setPreferredSize(new Dimension(400, 800));

        // Zona dreaptă pentru rețea și tabelul cu medicamente
        networkPanel = new JPanel(new BorderLayout());

        // JSplitPane principal
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
        splitPane.setDividerLocation(400);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);
        frame.setVisible(true);

        // Acțiunea butonului "Explore"
        exploreButton.addActionListener(e -> {
            String geneSymbol = geneInput.getText().trim();
            if (!geneSymbol.isEmpty()) {
                exploreGene(geneSymbol);
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Butonul "See more details" – se afișează lista de gene conectate pe baza pathway-urilor
        detailsButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                String inputGeneSymbol = geneInput.getText().trim();
                if (inputGeneSymbol.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol in the input field first.");
                    return;
                }
                DataModel.Gene inputGene = findGeneByName(allGenes, inputGeneSymbol);
                if (inputGene == null) {
                    JOptionPane.showMessageDialog(null, "Input gene not found in the database!");
                    return;
                }
                // Filtrare: extrage genele care împărtășesc cel puțin un pathway cu gena introdusă
                List<DataModel.Gene> connectedGenes = new ArrayList<>();
                for (DataModel.Gene gene : allGenes) {
                    if (!gene.getName().equalsIgnoreCase(inputGene.getName()) && shareCommonPathway(inputGene, gene)) {
                        connectedGenes.add(gene);
                    }
                }
                if (connectedGenes.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No connected genes found based on common pathways.");
                    return;
                }
                // Crează un array de nume de gene din lista filtrată
                String[] connectedGeneNames = connectedGenes.stream().map(g -> g.getName()).toArray(String[]::new);
                String selectedGeneName = (String) JOptionPane.showInputDialog(
                        null,
                        "Select a gene from the connected genes:",
                        "Gene Selection",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        connectedGeneNames,
                        connectedGeneNames[0]
                );
                if (selectedGeneName != null) {
                    DataModel.Gene selectedGene = findGeneByName(allGenes, selectedGeneName);
                    if (selectedGene != null) {
                        // Construieste graficul cu gena selectată ca nod central
                        GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
                        // Afișează într-o nouă fereastră rețeaua și sugestiile de repurposing
                        GeneInteractionNetworkv2.displayNetworkAndDrugSuggestions(data);
                    } else {
                        JOptionPane.showMessageDialog(null, "Selected gene not found!");
                    }
                }
            });
        });
    }

    private void exploreGene(String geneSymbol) {
        SwingUtilities.invokeLater(() -> {
            try {
                DataModel.Gene selectedGene = findGeneByName(allGenes, geneSymbol);
                if (selectedGene == null) {
                    geneOverviewArea.setText("Gene not found in database.");
                    return;
                }
                // Afișează informațiile despre genă (folosind metoda toString a clasei Gene)
                geneOverviewArea.setText(selectedGene.toString());
                // Construieste graficul bazat pe pathway-uri comune
                GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
                org.graphstream.ui.view.Viewer viewer = GeneInteractionNetworkv2.createGraphViewer(data);
                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
                viewPanel.setPreferredSize(new Dimension(700, 600));
                // Creează tabelul cu informații despre medicamente
                Map<String, List<GeneInteractionNetworkv2.DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
                List<Object[]> drugRows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
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
                // Panoul combinat: rețea + tabel
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

    // Metodă utilitară pentru a găsi o genă după nume în lista încărcată
    private DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String geneSymbol) {
        for (DataModel.Gene gene : genes) {
            if (gene.getName().equalsIgnoreCase(geneSymbol)) {
                return gene;
            }
        }
        return null;
    }

    // Metodă utilitară pentru a verifica dacă două gene au cel puțin un pathway comun
    private boolean shareCommonPathway(DataModel.Gene gene1, DataModel.Gene gene2) {
        for (DataModel.PathWay p1 : gene1.getPathWays()) {
            for (DataModel.PathWay p2 : gene2.getPathWays()) {
                if (p1.getPath().equals(p2.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }
}