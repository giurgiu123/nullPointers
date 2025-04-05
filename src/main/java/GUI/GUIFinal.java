package GUI;

import Data.DbGene;
import Data.DrugManipulator;
import Data.GeneList;
import DataModel.Drug;
import DataModel.Gene;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import GUI.GraphComponents.GeneInteractionNetworkv2;
import GUI.GraphComponents.InteractionData;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GUIFinal implements DrugManipulator {

    private JTextField geneInput;
    private JButton exploreButton;
    private JButton detailsButton;
    private JButton similarButton;
    private JButton showPathwaysButton;
    private JTextArea geneOverviewArea;
    private JPanel networkPanel;
    private JScrollPane drugScrollPane;
    private JSplitPane splitPane;
    private List<Gene> allGenes = new ArrayList<>();
    private GeneList geneList;
    private JComboBox<String> drugComboBox;
    private JButton showDrugGenesButton;
    private Map<String, List<Drug>> drugToGeneMap = new HashMap<>();
    JButton showAllDrugsButton;


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
    }

    public void createAndShowGUI() {
        this.geneList  = new DbGene();
        this.allGenes = geneList.getGenesFromJsonFile();

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
        showAllDrugsButton = new JButton("Show Related Drugs");
        topPanel.add(showAllDrugsButton);

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

            // Selectarea pathway-ului
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

            if (selectedPathway == null) return;

            // ----- INTERFAȚĂ DE LOADING CU BARA DE PROGRES -----
            // ----- INTERFAȚĂ DE LOADING CU BARĂ ALBASTRĂ, TEXT ȘI GIF MIC -----
            JDialog loadingDialog = new JDialog((Frame) null, "Loading Graph", true);
            loadingDialog.setUndecorated(false);
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

// Icon animat (GIF mic de loading) - din resources
            ImageIcon loadingIcon = new ImageIcon(getClass().getResource("/loading.gif"));
            JLabel iconLabel = new JLabel(loadingIcon, SwingConstants.CENTER);

// Text status
            JLabel statusLabel = new JLabel("Loading graph for " + selectedPathway + "...", SwingConstants.CENTER);

// Bara de progres albastră
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressBar.setForeground(new Color(0, 120, 215)); // albastru

// Asamblare
            panel.add(iconLabel, BorderLayout.NORTH);
            panel.add(statusLabel, BorderLayout.CENTER);
            panel.add(progressBar, BorderLayout.SOUTH);

            loadingDialog.getContentPane().add(panel);
            loadingDialog.setSize(350, 140);
            loadingDialog.setLocationRelativeTo(null);
            loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

// ---- TASK + ANIMARE ----
            SwingWorker<Component, Void> graphWorker = new SwingWorker<>() {
                private Component graphComponent;

                @Override
                protected Component doInBackground() throws Exception {
                    for (int i = 0; i <= 100; i++) {
                        Thread.sleep(150); // smooth anim
                        int progress = i;
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setValue(progress);
                            if (progress >= 80) {
                                statusLabel.setText("Almost done...");
                            }
                        });
                    }

                    graphComponent = GeneInteractionNetworkv2.createKGMLGraph(selectedPathway);
                    return graphComponent;
                }

                @Override
                protected void done() {
                    loadingDialog.dispose();
                    try {
                        Component graph = get();
                        JFrame graphFrame = new JFrame("KGML Graph - " + selectedPathway);
                        graphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        graphFrame.add(graph, BorderLayout.CENTER);
                        graphFrame.setSize(1200, 800);
                        graphFrame.setLocationRelativeTo(null);
                        graphFrame.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error generating graph: " + ex.getMessage());
                    }
                }
            };

            graphWorker.execute();
            loadingDialog.setVisible(true);
        });
        showAllDrugsButton.addActionListener(e -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("generated_gene_drug_summary.json");

                if (inputStream == null) {
                    JOptionPane.showMessageDialog(null, "JSON file not found.");
                    return;
                }

                List<Map<String, Object>> drugs = mapper.readValue(inputStream, new TypeReference<>() {});
                if (drugs.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No drugs found in the JSON.");
                    return;
                }

                String[] columnNames = {"Drug", "Genes", "Diseases", "Mechanisms"};
                List<Object[]> rowData = new ArrayList<>();

                for (Map<String, Object> drug : drugs) {
                    String drugName = (String) drug.get("Drug");
                    String genes = String.join(", ", (List<String>) drug.get("Gene"));
                    String diseases = String.join(", ", (List<String>) drug.get("Disease"));
                    String mechanisms = String.join(", ", (List<String>) drug.get("Mechanism"));
                    rowData.add(new Object[]{drugName, genes, diseases, mechanisms});
                }

                Object[][] tableData = rowData.toArray(new Object[0][]);
                JTable table = new JTable(tableData, columnNames);
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                JScrollPane scrollPane = new JScrollPane(table);

                // 🔍 Căsuță de căutare
                JTextField searchField = new JTextField(30);
                JLabel searchLabel = new JLabel("Search:");
                JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                searchPanel.add(searchLabel);
                searchPanel.add(searchField);

                TableRowSorter<TableModel> rowSorter = new TableRowSorter<>(table.getModel());
                table.setRowSorter(rowSorter);

                searchField.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) {
                        filter();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        filter();
                    }

                    public void changedUpdate(DocumentEvent e) {
                        filter();
                    }

                    private void filter() {
                        String text = searchField.getText().trim();
                        if (text.length() == 0) {
                            rowSorter.setRowFilter(null);
                        } else {
                            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                        }
                    }
                });

                // 🖱️ Click pe rând → popup cu detalii
                table.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        int row = table.getSelectedRow();
                        if (row >= 0) {
                            int modelRow = table.convertRowIndexToModel(row);
                            String drugName = (String) table.getModel().getValueAt(modelRow, 0);
                            String geneList = (String) table.getModel().getValueAt(modelRow, 1);
                            String diseaseList = (String) table.getModel().getValueAt(modelRow, 2);
                            String mechList = (String) table.getModel().getValueAt(modelRow, 3);

                            JTextArea detailArea = new JTextArea(
                                    "Drug: " + drugName + "\n\n" +
                                            "Genes:\n" + geneList + "\n\n" +
                                            "Diseases:\n" + diseaseList + "\n\n" +
                                            "Mechanisms:\n" + mechList
                            );
                            detailArea.setEditable(false);
                            detailArea.setLineWrap(true);
                            detailArea.setWrapStyleWord(true);
                            JScrollPane detailScroll = new JScrollPane(detailArea);
                            detailScroll.setPreferredSize(new Dimension(500, 300));

                            JOptionPane.showMessageDialog(null, detailScroll, "Drug Details", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });

                // 🔲 Fereastră cu căutare + tabel
                JFrame tableFrame = new JFrame("All Drugs from JSON");
                tableFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                tableFrame.setLayout(new BorderLayout());
                tableFrame.add(searchPanel, BorderLayout.NORTH);
                tableFrame.add(scrollPane, BorderLayout.CENTER);
                tableFrame.setSize(1000, 500);
                tableFrame.setLocationRelativeTo(null);
                tableFrame.setVisible(true);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            }
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
                InteractionData data = GeneInteractionNetworkv2.createGraphFromGenes(allGenes, selectedGene);
                Viewer viewer = GeneInteractionNetworkv2.createGraphViewer(data);
                ViewPanel viewPanel = (ViewPanel) viewer.getDefaultView();
                viewPanel.setPreferredSize(new Dimension(700, 600));
                Map<String, List<DrugInfo>> drugMap = GeneInteractionNetworkv2.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
                List<Object[]> drugRows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
                String[] columnNames = {"Gene", "Drug", "Indication", "Mechanism"};
                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames);
                drugTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = drugTable.rowAtPoint(e.getPoint());
                        int col = drugTable.columnAtPoint(e.getPoint());
                        if (row >= 0 && col == 1) {
                            String drug = (String) drugTable.getValueAt(row, col);
                            String mech = (String) drugTable.getValueAt(row, 3);
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


    public List<Drug> readDrugData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = getClass().getClassLoader().getResourceAsStream("src/main/resources/generated_gene_drug_summary.json");

            if (input == null) {
                throw new RuntimeException("Fișierul JSON nu a fost găsit în resources.");
            }

            List<Drug> drugs = mapper.readValue(input, new TypeReference<List<Drug>>() {});
            return drugs;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}