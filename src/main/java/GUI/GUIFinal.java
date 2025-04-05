////////package GUI;
////////
////////
////////import org.graphstream.ui.swingViewer.ViewPanel;
////////import javax.swing.*;
////////import java.awt.*;
////////import java.awt.event.*;
////////import java.util.ArrayList;
////////import java.util.List;
////////import java.util.Map;
////////
////////public class GUIFinal {
////////
////////    private JTextArea geneOverviewArea;
////////    private JPanel networkPanel;
////////    private JScrollPane drugScrollPane;
////////    private JSplitPane splitPane;
////////    private List<DataModel.Gene> allGenes; // Lista de gene încărcată din JSON
////////
////////    public static void main(String[] args) {
////////        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
////////    }
////////
////////    public void createAndShowGUI() {
////////        // Încărcăm lista de gene din JSON folosind clasa DbGene
////////        Data.DbGene dbGene = new Data.DbGene();
////////        allGenes = dbGene.getGenesFromJsonFile();
////////
////////        JFrame frame = new JFrame("Gene Explorer");
////////        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
////////        frame.setSize(1400, 900);
////////
////////        // Panou superior cu câmpul de input și butoanele
////////        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
////////        JTextField geneInput = new JTextField(20);
////////        JButton exploreButton = new JButton("🔍 Explore");
////////        JButton detailsButton = new JButton("See more details");
////////
////////        topPanel.add(new JLabel("Enter Gene Symbol:"));
////////        topPanel.add(geneInput);
////////        topPanel.add(exploreButton);
////////        topPanel.add(detailsButton);
////////
////////        // Zona stângă pentru afișarea informațiilor despre genă
////////        geneOverviewArea = new JTextArea();
////////        geneOverviewArea.setEditable(false);
////////        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
////////        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
////////        overviewScrollPane.setPreferredSize(new Dimension(400, 800));
////////
////////        // Zona dreaptă pentru rețea și tabelul cu medicamente
////////        networkPanel = new JPanel(new BorderLayout());
////////
////////        // JSplitPane principal
////////        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
////////        splitPane.setDividerLocation(400);
////////
////////        frame.setLayout(new BorderLayout());
////////        frame.add(topPanel, BorderLayout.NORTH);
////////        frame.add(splitPane, BorderLayout.CENTER);
////////        frame.setVisible(true);
////////
////////        // Acțiunea butonului "Explore"
////////        exploreButton.addActionListener(e -> {
////////            String geneSymbol = geneInput.getText().trim();
////////            if (!geneSymbol.isEmpty()) {
////////                exploreGene(geneSymbol);
////////            } else {
////////                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
////////            }
////////        });
////////
////////        // Butonul "See more details" – se afișează lista de gene conectate pe baza pathway-urilor
////////        detailsButton.addActionListener(e -> {
////////            SwingUtilities.invokeLater(() -> {
////////                String inputGeneSymbol = geneInput.getText().trim();
////////                if (inputGeneSymbol.isEmpty()) {
////////                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol in the input field first.");
////////                    return;
////////                }
////////                DataModel.Gene inputGene = findGeneByName(allGenes, inputGeneSymbol);
////////                if (inputGene == null) {
////////                    JOptionPane.showMessageDialog(null, "Input gene not found in the database!");
////////                    return;
////////                }
////////                // Filtrare: extrage genele care împărtășesc cel puțin un pathway cu gena introdusă
////////                List<DataModel.Gene> connectedGenes = new ArrayList<>();
////////                for (DataModel.Gene gene : allGenes) {
////////                    if (!gene.getName().equalsIgnoreCase(inputGene.getName()) && shareCommonPathway(inputGene, gene)) {
////////                        connectedGenes.add(gene);
////////                    }
////////                }
////////                if (connectedGenes.isEmpty()) {
////////                    JOptionPane.showMessageDialog(null, "No connected genes found based on common pathways.");
////////                    return;
////////                }
////////                // Crează un array de nume de gene din lista filtrată
////////                String[] connectedGeneNames = connectedGenes.stream().map(g -> g.getName()).toArray(String[]::new);
////////                String selectedGeneName = (String) JOptionPane.showInputDialog(
////////                        null,
////////                        "Select a gene from the connected genes:",
////////                        "Gene Selection",
////////                        JOptionPane.QUESTION_MESSAGE,
////////                        null,
////////                        connectedGeneNames,
////////                        connectedGeneNames[0]
////////                );
////////                if (selectedGeneName != null) {
////////                    DataModel.Gene selectedGene = findGeneByName(allGenes, selectedGeneName);
////////                    if (selectedGene != null) {
////////                        // Construieste graficul cu gena selectată ca nod central
////////                        GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
////////                        // Afișează într-o nouă fereastră rețeaua și sugestiile de repurposing
////////                        GeneInteractionNetworkv2.displayNetworkAndDrugSuggestions(data);
////////                    } else {
////////                        JOptionPane.showMessageDialog(null, "Selected gene not found!");
////////                    }
////////                }
////////            });
////////        });
////////    }
////////
////////    private void exploreGene(String geneSymbol) {
////////        SwingUtilities.invokeLater(() -> {
////////            try {
////////                DataModel.Gene selectedGene = findGeneByName(allGenes, geneSymbol);
////////                if (selectedGene == null) {
////////                    geneOverviewArea.setText("Gene not found in database.");
////////                    return;
////////                }
////////                // Afișează informațiile despre genă (folosind metoda toString a clasei Gene)
////////                geneOverviewArea.setText(selectedGene.toString());
////////                // Construieste graficul bazat pe pathway-uri comune
////////                GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
////////                org.graphstream.ui.view.Viewer viewer = GeneInteractionNetworkv2.createGraphViewer(data);
////////                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
////////                viewPanel.setPreferredSize(new Dimension(700, 600));
////////                // Creează tabelul cu informații despre medicamente
////////                Map<String, List<GeneInteractionNetworkv2.DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
////////                List<Object[]> drugRows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
////////                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
////////                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
////////                drugTable.addMouseListener(new MouseAdapter() {
////////                    @Override
////////                    public void mouseClicked(MouseEvent e) {
////////                        int row = drugTable.rowAtPoint(e.getPoint());
////////                        int col = drugTable.columnAtPoint(e.getPoint());
////////                        if (col == 1) {
////////                            String drug = (String) drugTable.getValueAt(row, col);
////////                            String mech = (String) drugTable.getValueAt(row, 4);
////////                            JOptionPane.showMessageDialog(null, "Drug: " + drug + "\nMechanism: " + mech,
////////                                    "Drug Details", JOptionPane.INFORMATION_MESSAGE);
////////                        }
////////                    }
////////                });
////////                drugScrollPane = new JScrollPane(drugTable);
////////                drugScrollPane.setPreferredSize(new Dimension(700, 200));
////////                // Panoul combinat: rețea + tabel
////////                JPanel rightPanel = new JPanel(new BorderLayout());
////////                rightPanel.add(viewPanel, BorderLayout.CENTER);
////////                rightPanel.add(drugScrollPane, BorderLayout.SOUTH);
////////                networkPanel.removeAll();
////////                networkPanel.add(rightPanel, BorderLayout.CENTER);
////////                networkPanel.revalidate();
////////                networkPanel.repaint();
////////            } catch (Exception ex) {
////////                ex.printStackTrace();
////////                JOptionPane.showMessageDialog(null,
////////                        "❌ Error fetching gene data.\n" + ex.getMessage(),
////////                        "Error", JOptionPane.ERROR_MESSAGE);
////////            }
////////        });
////////    }
////////
////////    // Metodă utilitară pentru a găsi o genă după nume în lista încărcată
////////    private DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String geneSymbol) {
////////        for (DataModel.Gene gene : genes) {
////////            if (gene.getName().equalsIgnoreCase(geneSymbol)) {
////////                return gene;
////////            }
////////        }
////////        return null;
////////    }
////////
////////    // Metodă utilitară pentru a verifica dacă două gene au cel puțin un pathway comun
////////    private boolean shareCommonPathway(DataModel.Gene gene1, DataModel.Gene gene2) {
////////        for (DataModel.PathWay p1 : gene1.getPathWays()) {
////////            for (DataModel.PathWay p2 : gene2.getPathWays()) {
////////                if (p1.getPath().equals(p2.getPath())) {
////////                    return true;
////////                }
////////            }
////////        }
////////        return false;
////////    }
////////}
//package GUI;
//
//import GUI.GeneInteractionNetworkv2;
//import GUI.GeneInteractionNetworkv2.DrugInfo;
//import GUI.GeneInteractionNetworkv2.InteractionData;
//import org.graphstream.ui.swingViewer.ViewPanel;
//import org.graphstream.ui.view.Viewer;
//import DataModel.Gene;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.*;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
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
//        // Încarcă lista de gene (din JSON, exemplu prin DbGene)
//        Data.DbGene dbGene = new Data.DbGene();
//        allGenes = dbGene.getGenesFromJsonFile();
//
//        JFrame frame = new JFrame("Gene Explorer");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setSize(1400, 900);
//
//        // Panelul de sus: input și butoane
//        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        JTextField geneInput = new JTextField(20);
//        JButton exploreButton = new JButton("🔍 Explore");
//        JButton detailsButton = new JButton("See more details");
//        JButton similarButton = new JButton("Show Similar Genes");
//        JButton showPathwaysButton = new JButton("Show Pathways");  // Noua funcționalitate
//
//        topPanel.add(new JLabel("Enter Gene Symbol:"));
//        topPanel.add(geneInput);
//        topPanel.add(exploreButton);
//        topPanel.add(detailsButton);
//        topPanel.add(similarButton);
//        topPanel.add(showPathwaysButton);
//
//        // Zona stângă: afișare informații gene
//        geneOverviewArea = new JTextArea();
//        geneOverviewArea.setEditable(false);
//        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
//        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
//        overviewScrollPane.setPreferredSize(new Dimension(400, 800));
//
//        // Zona dreaptă: pentru rețea și alte afișări
//        networkPanel = new JPanel(new BorderLayout());
//        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
//        splitPane.setDividerLocation(400);
//
//        frame.setLayout(new BorderLayout());
//        frame.add(topPanel, BorderLayout.NORTH);
//        frame.add(splitPane, BorderLayout.CENTER);
//        frame.setVisible(true);
//
//        // Butonul "Explore": acțiunea rămâne similară
//        exploreButton.addActionListener(e -> {
//            String geneSymbol = geneInput.getText().trim();
//            if (!geneSymbol.isEmpty()) {
//                exploreGene(geneSymbol);
//            } else {
//                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
//            }
//        });
//
//        // Butonul "See more details": afișează lista de gene conectate pe baza pathway-urilor
//        detailsButton.addActionListener(e -> {
//            SwingUtilities.invokeLater(() -> {
//                String inputGeneSymbol = geneInput.getText().trim();
//                if (inputGeneSymbol.isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol in the input field first.");
//                    return;
//                }
//                DataModel.Gene inputGene = findGeneByName(allGenes, inputGeneSymbol);
//                if (inputGene == null) {
//                    JOptionPane.showMessageDialog(null, "Input gene not found in the database!");
//                    return;
//                }
//                List<DataModel.Gene> connectedGenes = new ArrayList<>();
//                for (DataModel.Gene gene : allGenes) {
//                    if (!gene.getName().equalsIgnoreCase(inputGene.getName()) && shareCommonPathway(inputGene, gene)) {
//                        connectedGenes.add(gene);
//                    }
//                }
//                if (connectedGenes.isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "No connected genes found based on common pathways.");
//                    return;
//                }
//                String[] connectedGeneNames = connectedGenes.stream().map(g -> g.getName()).toArray(String[]::new);
//                String selectedGeneName = (String) JOptionPane.showInputDialog(
//                        null,
//                        "Select a gene from the connected genes:",
//                        "Gene Selection",
//                        JOptionPane.QUESTION_MESSAGE,
//                        null,
//                        connectedGeneNames,
//                        connectedGeneNames[0]
//                );
//                if (selectedGeneName != null) {
//                    DataModel.Gene selectedGene = findGeneByName(allGenes, selectedGeneName);
//                    if (selectedGene != null) {
//                        InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
//                        GeneInteractionNetworkv2.displayNetworkAndDrugSuggestions(data);
//                    } else {
//                        JOptionPane.showMessageDialog(null, "Selected gene not found!");
//                    }
//                }
//            });
//        });
//
//        // Butonul "Show Similar Genes": afișează un tabel cu genele similare, cu scor calculat
//        similarButton.addActionListener(e -> {
//            SwingUtilities.invokeLater(() -> {
//                String inputGeneSymbol = geneInput.getText().trim();
//                if (inputGeneSymbol.isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol in the input field first.");
//                    return;
//                }
//                DataModel.Gene selectedGene = findGeneByName(allGenes, inputGeneSymbol);
//                if (selectedGene == null) {
//                    JOptionPane.showMessageDialog(null, "Input gene not found in the database!");
//                    return;
//                }
//                Map<String, List<DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
//                List<Object[]> similarityRows = new ArrayList<>();
//                List<DrugInfo> selectedDrugs = drugMap.get(selectedGene.getName());
//                if (selectedDrugs == null || selectedDrugs.isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "No drug information found for the selected gene.");
//                    return;
//                }
//                for (DataModel.Gene gene : allGenes) {
//                    if (gene.getName().equalsIgnoreCase(selectedGene.getName()))
//                        continue;
//                    if (shareCommonPathway(selectedGene, gene)) {
//                        List<DrugInfo> otherDrugs = drugMap.get(gene.getName());
//                        if (otherDrugs != null && !otherDrugs.isEmpty()) {
//                            Set<String> selectedDrugNames = new HashSet<>();
//                            for (DrugInfo d : selectedDrugs) {
//                                selectedDrugNames.add(d.drugName);
//                            }
//                            Set<String> otherDrugNames = new HashSet<>();
//                            for (DrugInfo d : otherDrugs) {
//                                otherDrugNames.add(d.drugName);
//                            }
//                            Set<String> commonDrugs = new HashSet<>(selectedDrugNames);
//                            commonDrugs.retainAll(otherDrugNames);
//                            if (!commonDrugs.isEmpty()) {
//                                List<String> commonPathways = getCommonPathways(selectedGene, gene);
//                                if (!commonPathways.isEmpty()) {
//                                    int score = commonPathways.size() * 1 + commonDrugs.size() * 5;
//                                    similarityRows.add(new Object[]{
//                                            gene.getName(),
//                                            String.join(", ", commonPathways),
//                                            String.join(", ", commonDrugs),
//                                            score
//                                    });
//                                }
//                            }
//                        }
//                    }
//                }
//                if (similarityRows.isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "No similar genes found (sharing a common pathway and drug).");
//                    return;
//                }
//                similarityRows.sort((row1, row2) -> {
//                    Integer score1 = (Integer) row1[3];
//                    Integer score2 = (Integer) row2[3];
//                    return score2.compareTo(score1);
//                });
//                String[] columnNames = {"Gene", "Common Pathways", "Common Drugs", "Score"};
//                JTable similarityTable = new JTable(similarityRows.toArray(new Object[0][]), columnNames);
//                JScrollPane scrollPane = new JScrollPane(similarityTable);
//                JFrame similarityFrame = new JFrame("Gene Similarity");
//                similarityFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//                similarityFrame.add(scrollPane, BorderLayout.CENTER);
//                similarityFrame.setSize(800, 400);
//                similarityFrame.setLocationRelativeTo(null);
//                similarityFrame.setVisible(true);
//            });
//        });
//
//        // Noua funcționalitate: Butonul "Show Pathways"
//        // Când este apăsat, se caută genea după input, iar din obiectul Gene se preia lista de pathway-uri.
//        // Acestea sunt afișate într-un dialog.
//       // JButton showPathwaysButton = new JButton("Show Pathways");
//        // Adăugăm butonul în topPanel (sau puteți adăuga lângă celelalte butoane)
//        // (În codul de mai sus, butonul a fost adăugat deja)
//        showPathwaysButton.addActionListener(e -> {
//            SwingUtilities.invokeLater(() -> {
//                String inputGeneSymbol = geneInput.getText().trim();
//                if (inputGeneSymbol.isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol in the input field first.");
//                    return;
//                }
//                DataModel.Gene selectedGene = findGeneByName(allGenes, inputGeneSymbol);
//                if (selectedGene == null) {
//                    JOptionPane.showMessageDialog(null, "Gene not found in the database!");
//                    return;
//                }
//                // Preia lista de pathway-uri din obiectul Gene
//                if (selectedGene.getPathWays().isEmpty()) {
//                    JOptionPane.showMessageDialog(null, "No pathways found for gene " + selectedGene.getName());
//                    return;
//                }
//                List<String> pathways = new ArrayList<>();
//                for (DataModel.PathWay p : selectedGene.getPathWays()) {
//                    pathways.add(p.getPath());
//                }
//                // Afișează pathway-urile într-un dialog
//                String message = "Pathways for gene " + selectedGene.getName() + ":\n" + String.join("\n", pathways);
//                JOptionPane.showMessageDialog(null, message, "Gene Pathways", JOptionPane.INFORMATION_MESSAGE);
//            });
//        });
//        // Adăugăm butonul "Show Pathways" în topPanel
//        // Dacă nu este deja adăugat, îl puteți adăuga aici:
//        // topPanel.add(showPathwaysButton);
//
//        frame.setVisible(true);
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
//                geneOverviewArea.setText(selectedGene.toString());
//                GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
//                Viewer viewer = GeneInteractionNetworkv2.createGraphViewer(data);
//                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
//                viewPanel.setPreferredSize(new Dimension(700, 600));
//                Map<String, List<DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
//                List<Object[]> drugRows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
//                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
//                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
//                drugTable.addMouseListener(new MouseAdapter() {
//                    @Override
//                    public void mouseClicked(MouseEvent e) {
//                        int row = drugTable.rowAtPoint(e.getPoint());
//                        int col = drugTable.columnAtPoint(e.getPoint());
//                        if (row >= 0 && col == 1) {
//                            String drug = (String) drugTable.getValueAt(row, col);
//                            String mech = (String) drugTable.getValueAt(row, 4);
//                            JOptionPane.showMessageDialog(null, "Drug: " + drug + "\nMechanism: " + mech,
//                                    "Drug Info", JOptionPane.INFORMATION_MESSAGE);
//                        }
//                    }
//                });
//                drugScrollPane = new JScrollPane(drugTable);
//                drugScrollPane.setPreferredSize(new Dimension(700, 200));
//                JPanel rightPanel = new JPanel(new BorderLayout());
//                rightPanel.add(viewPanel, BorderLayout.CENTER);
//                rightPanel.add(drugScrollPane, BorderLayout.SOUTH);
//                networkPanel.removeAll();
//                networkPanel.add(rightPanel, BorderLayout.CENTER);
//                networkPanel.revalidate();
//                networkPanel.repaint();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                JOptionPane.showMessageDialog(null,
//                        "❌ Error fetching gene data.\n" + ex.getMessage(),
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        });
//    }
//
//    // Metodă utilitară pentru a găsi o genă după nume în lista încărcată.
//    private DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String geneSymbol) {
//        for (DataModel.Gene gene : genes) {
//            if (gene.getName().equalsIgnoreCase(geneSymbol)) {
//                return gene;
//            }
//        }
//        return null;
//    }
//
//    // Metodă utilitară pentru a verifica dacă două gene au cel puțin un pathway comun.
//    private boolean shareCommonPathway(DataModel.Gene gene1, DataModel.Gene gene2) {
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
//    // Metodă pentru a obține lista de pathway-uri comune între două gene.
//    private List<String> getCommonPathways(DataModel.Gene gene1, DataModel.Gene gene2) {
//        List<String> common = new ArrayList<>();
//        for (DataModel.PathWay p1 : gene1.getPathWays()) {
//            for (DataModel.PathWay p2 : gene2.getPathWays()) {
//                if (p1.getPath().equals(p2.getPath())) {
//                    common.add(p1.getPath());
//                }
//            }
//        }
//        return common;
//    }
//}
package GUI;

import DataModel.PathWay;
import GUI.GeneInteractionNetworkv2;
import GUI.GeneInteractionNetworkv2.DrugInfo;
import GUI.GeneInteractionNetworkv2.InteractionData;
import DataModel.Gene;
import javax.swing.*;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GUIFinal {

    private JTextField geneInput;
    private JButton exploreButton;
    private JButton detailsButton;
    private JButton similarButton;
    private JButton showPathwaysButton;
    private JTextArea geneOverviewArea;
    private JPanel networkPanel;
    private JScrollPane drugScrollPane;
    private JSplitPane splitPane;
    private List<Gene> allGenes; // Lista de gene încărcată din JSON

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
    }

    public void createAndShowGUI() {
        // Încarcă lista de gene (de exemplu, din DbGene)
        Data.DbGene dbGene = new Data.DbGene();
        allGenes = dbGene.getGenesFromJsonFile();

        JFrame frame = new JFrame("Gene Explorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);

        // Panelul de sus: câmp de input și butoane
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Enter Gene Symbol:"));
        geneInput = new JTextField(20);
        topPanel.add(geneInput);
        exploreButton = new JButton("🔍 Explore");
        detailsButton = new JButton("See more details");
        similarButton = new JButton("Show Similar Genes");
        showPathwaysButton = new JButton("Show Pathways");
        topPanel.add(exploreButton);
        topPanel.add(detailsButton);
        topPanel.add(similarButton);
        topPanel.add(showPathwaysButton);

        // Zona stângă: afișare informații despre genă
        geneOverviewArea = new JTextArea();
        geneOverviewArea.setEditable(false);
        geneOverviewArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
        overviewScrollPane.setPreferredSize(new Dimension(400, 800));

        // Zona dreaptă: pentru rețea și alte afișări
        networkPanel = new JPanel(new BorderLayout());
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, overviewScrollPane, networkPanel);
        splitPane.setDividerLocation(400);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);

        // Acțiunea butonului "Explore"
        exploreButton.addActionListener(e -> {
            String geneSymbol = geneInput.getText().trim();
            if (!geneSymbol.isEmpty()) {
                exploreGene(geneSymbol);
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Butonul "See more details" – afișează lista de gene conectate pe baza pathway-urilor
        detailsButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                String inputGeneSymbol = geneInput.getText().trim();
                if (inputGeneSymbol.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol in the input field first.");
                    return;
                }
                Gene inputGene = findGeneByName(allGenes, inputGeneSymbol);
                if (inputGene == null) {
                    JOptionPane.showMessageDialog(null, "Input gene not found in the database!");
                    return;
                }
                List<Gene> connectedGenes = new ArrayList<>();
                for (Gene gene : allGenes) {
                    if (!gene.getName().equalsIgnoreCase(inputGene.getName()) && shareCommonPathway(inputGene, gene)) {
                        connectedGenes.add(gene);
                    }
                }
                if (connectedGenes.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No connected genes found based on common pathways.");
                    return;
                }
                String[] connectedGeneNames = connectedGenes.stream().map(Gene::getName).toArray(String[]::new);
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
                    Gene selectedGene = findGeneByName(allGenes, selectedGeneName);
                    if (selectedGene != null) {
                        InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
                        GeneInteractionNetworkv2.displayNetworkAndDrugSuggestions(data);
                    } else {
                        JOptionPane.showMessageDialog(null, "Selected gene not found!");
                    }
                }
            });
        });

        // Butonul "Show Similar Genes" – afișează un tabel cu genele similare
        similarButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                String inputGeneSymbol = geneInput.getText().trim();
                if (inputGeneSymbol.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol in the input field first.");
                    return;
                }
                Gene selectedGene = findGeneByName(allGenes, inputGeneSymbol);
                if (selectedGene == null) {
                    JOptionPane.showMessageDialog(null, "Input gene not found in the database!");
                    return;
                }
                Map<String, List<DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
                List<Object[]> similarityRows = new ArrayList<>();
                List<DrugInfo> selectedDrugs = drugMap.get(selectedGene.getName());
                if (selectedDrugs == null || selectedDrugs.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No drug information found for the selected gene.");
                    return;
                }
                for (Gene gene : allGenes) {
                    if (gene.getName().equalsIgnoreCase(selectedGene.getName()))
                        continue;
                    if (shareCommonPathway(selectedGene, gene)) {
                        List<DrugInfo> otherDrugs = drugMap.get(gene.getName());
                        if (otherDrugs != null && !otherDrugs.isEmpty()) {
                            Set<String> selectedDrugNames = new HashSet<>();
                            for (DrugInfo d : selectedDrugs) {
                                selectedDrugNames.add(d.drugName);
                            }
                            Set<String> otherDrugNames = new HashSet<>();
                            for (DrugInfo d : otherDrugs) {
                                otherDrugNames.add(d.drugName);
                            }
                            Set<String> commonDrugs = new HashSet<>(selectedDrugNames);
                            commonDrugs.retainAll(otherDrugNames);
                            if (!commonDrugs.isEmpty()) {
                                List<String> commonPathways = getCommonPathways(selectedGene, gene);
                                if (!commonPathways.isEmpty()) {
                                    int score = commonPathways.size() * 1 + commonDrugs.size() * 5;
                                    similarityRows.add(new Object[]{
                                            gene.getName(),
                                            String.join(", ", commonPathways),
                                            String.join(", ", commonDrugs),
                                            score
                                    });
                                }
                            }
                        }
                    }
                }
                if (similarityRows.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No similar genes found (sharing a common pathway and drug).");
                    return;
                }
                similarityRows.sort((row1, row2) -> {
                    Integer score1 = (Integer) row1[3];
                    Integer score2 = (Integer) row2[3];
                    return score2.compareTo(score1);
                });
                String[] columnNames = {"Gene", "Common Pathways", "Common Drugs", "Score"};
                JTable similarityTable = new JTable(similarityRows.toArray(new Object[0][]), columnNames);
                JScrollPane scrollPane = new JScrollPane(similarityTable);
                JFrame similarityFrame = new JFrame("Gene Similarity");
                similarityFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                similarityFrame.add(scrollPane, BorderLayout.CENTER);
                similarityFrame.setSize(800, 400);
                similarityFrame.setLocationRelativeTo(null);
                similarityFrame.setVisible(true);
            });
        });

        // Butonul "Show Pathways": afișează un dialog cu un combo box ce conține toate pathway-urile din obiectul Gene
        showPathwaysButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                String inputGeneSymbol = geneInput.getText().trim();
                if (inputGeneSymbol.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please enter a gene symbol first.");
                    return;
                }
                Gene selectedGene = findGeneByName(allGenes, inputGeneSymbol);
                if (selectedGene == null) {
                    JOptionPane.showMessageDialog(null, "Gene not found in the database!");
                    return;
                }
                if (selectedGene.getPathWays().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No pathways found for gene " + selectedGene.getName());
                    return;
                }
                // Creează un array cu codurile pathway-urilor din obiectul Gene
                String[] pathwayOptions = selectedGene.getPathWays().stream()
                        .map(p -> p.getPath())
                        .toArray(String[]::new);
                String selectedPathway = (String) JOptionPane.showInputDialog(
                        null,
                        "Select a pathway:",
                        "Pathway Selection",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        pathwayOptions,
                        pathwayOptions[0]
                );
                if (selectedPathway != null) {
                    JOptionPane.showMessageDialog(null, "You selected pathway: " + selectedPathway);
                    // Aici se poate apela metoda de generare a grafului pe baza KGML pentru pathway-ul selectat.

                    try {
                        Component graphComponent = GeneInteractionNetworkv2.createKGMLGraph(selectedPathway);
                        // Afișează graful într-o fereastră nouă integrată într-un panou
                        JFrame graphFrame = new JFrame("KGML Graph - " + selectedPathway);
                        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        graphFrame.add(graphComponent, BorderLayout.CENTER);
                        graphFrame.setSize(1200, 800);
                        graphFrame.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error generating graph: " + ex.getMessage());
                    }
                }
            });
        });

        frame.setVisible(true);
    }


    private void exploreGene(String geneSymbol) {
        SwingUtilities.invokeLater(() -> {
            try {
                Gene selectedGene = findGeneByName(allGenes, geneSymbol);
                if (selectedGene == null) {
                    geneOverviewArea.setText("Gene not found in database.");
                    return;
                }
                geneOverviewArea.setText(selectedGene.toString());
                GeneInteractionNetworkv2.InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
                Viewer viewer = GeneInteractionNetworkv2.createGraphViewer(data);
                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
                viewPanel.setPreferredSize(new Dimension(700, 600));
                Map<String, List<DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
                List<Object[]> drugRows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
                String[] columnNames = {"Gene", "Drug", "Indication", "Score", "Mechanism"};
                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
                drugTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = drugTable.rowAtPoint(e.getPoint());
                        int col = drugTable.columnAtPoint(e.getPoint());
                        if (row >= 0 && col == 1) {
                            String drug = (String) drugTable.getValueAt(row, col);
                            String mech = (String) drugTable.getValueAt(row, 4);
                            JOptionPane.showMessageDialog(null, "Drug: " + drug + "\nMechanism: " + mech,
                                    "Drug Info", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
                drugScrollPane = new JScrollPane(drugTable);
                drugScrollPane.setPreferredSize(new Dimension(700, 200));
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

    // Metodă utilitară pentru a găsi o genă după nume.
    private DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String geneSymbol) {
        for (DataModel.Gene gene : genes) {
            if (gene.getName().equalsIgnoreCase(geneSymbol)) {
                return gene;
            }
        }
        return null;
    }

    // Metodă utilitară pentru a verifica dacă două gene au cel puțin un pathway comun.
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

    // Metodă pentru a obține lista de pathway-uri comune între două gene.
    private List<String> getCommonPathways(DataModel.Gene gene1, DataModel.Gene gene2) {
        List<String> common = new ArrayList<>();
        for (DataModel.PathWay p1 : gene1.getPathWays()) {
            for (DataModel.PathWay p2 : gene2.getPathWays()) {
                if (p1.getPath().equals(p2.getPath())) {
                    common.add(p1.getPath());
                }
            }
        }
        return common;
    }
}