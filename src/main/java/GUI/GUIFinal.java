package GUI;

import BusinessLogic.DbGene;
import BusinessLogic.DrugManipulator;
import BusinessLogic.GeneList;
import DataModel.Gene;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
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

public class GUIFinal  {

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
    private JButton showAllDrugsButton;
    private DrugManipulator drugManipulator;



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIFinal().createAndShowGUI());
    }
    private void stylizeButton(JButton button) {
        button.setBackground(new Color(0, 120, 215));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    private void stylizeTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 230, 230));
        table.setGridColor(new Color(220, 220, 220));
        table.setSelectionBackground(new Color(0, 120, 215));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
    }

    private void stylizeSearchField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(300, 28));
        field.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
    }

    public void createAndShowGUI() {
        this.geneList  = new DbGene();
        this.allGenes = geneList.getGenesFromJsonFile();
        this.drugManipulator = new GeneInteractionNetworkv2();

        JFrame frame = new JFrame("Gene Explorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 900);

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
        stylizeButton(exploreButton);
        stylizeButton(detailsButton);
        stylizeButton(similarButton);
        stylizeButton(showPathwaysButton);
        stylizeButton(showAllDrugsButton);

        geneOverviewArea = new JTextArea();
        geneOverviewArea.setEditable(false);
        geneOverviewArea.setFont(new Font("Times New Roman", Font.BOLD, 16));
        JScrollPane overviewScrollPane = new JScrollPane(geneOverviewArea);
        overviewScrollPane.setPreferredSize(new Dimension(400, 800));


        ImageIcon dnaGifIcon = new ImageIcon(getClass().getResource("/dna.gif"));
        JLabel dnaGifLabel = new JLabel(dnaGifIcon, SwingConstants.CENTER);


        networkPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(overviewScrollPane, BorderLayout.CENTER);
        leftPanel.add(dnaGifLabel, BorderLayout.SOUTH);

        //  splitPane cu leftPanel pentru zona din stânga și networkPanel pentru dreapta
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, networkPanel);
        splitPane.setDividerLocation(400);

        frame.setLayout(new BorderLayout());
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.getContentPane().setBackground(Color.WHITE);
        topPanel.setBackground(new Color(245, 245, 245));
        networkPanel.setBackground(Color.WHITE);
        leftPanel.setBackground(Color.WHITE);
        geneOverviewArea.setBackground(new Color(250, 250, 250));
        geneOverviewArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        exploreButton.addActionListener(e -> {
            String geneSymbol = geneInput.getText().trim();
            if (!geneSymbol.isEmpty()) {
                exploreGene(geneSymbol);
            } else {
                JOptionPane.showMessageDialog(frame, "Please enter a gene symbol.", "Input Required", JOptionPane.WARNING_MESSAGE);
            }
        });


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
                        drugManipulator.displayNetworkAndDrugSuggestions(data);
                    } else {
                        JOptionPane.showMessageDialog(null, "Selected gene not found!");
                    }
                }
            });
        });


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
                Map<String, List<DrugInfo>> drugMap = drugManipulator.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
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
                                    float score = (float) (commonPathways.size() * 1 + commonDrugs.size() * 5) /100;                                    similarityRows.add(new Object[]{
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
                    Float score1 = (Float) row1[3];
                    Float score2 = (Float) row2[3];
                    return score2.compareTo(score1);
                });
                String[] columnNames = {"Gene", "Common Pathways", "Common Drugs", "Score"};
                Object[][] similarityData = similarityRows.toArray(new Object[0][]);
                JTable similarityTable = new JTable(new DefaultTableModel(similarityData, columnNames) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // toate celulele sunt doar pentru afișare
                    }
                });
                stylizeTable(similarityTable);
                similarityTable.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        int row = similarityTable.rowAtPoint(e.getPoint());
                        if (row >= 0) {
                            String gene = similarityTable.getValueAt(row, 0).toString();
                            String pathways = similarityTable.getValueAt(row, 1).toString();
                            String drugs = similarityTable.getValueAt(row, 2).toString();
                            String score = similarityTable.getValueAt(row, 3).toString();

                            String message = String.format(
                                    "Gene: %s\nCommon Pathways: %s\nCommon Drugs: %s\nScore: %s",
                                    gene, pathways, drugs, score
                            );

                            JOptionPane.showMessageDialog(null, message, "Gene Similarity Details", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                });
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
            loadingDialog.setSize(350, 180);
            loadingDialog.setResizable(false); // sa nu poata modifica dim
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
                        graphFrame.setSize(1500, 1000);
                        graphFrame.setResizable(false);
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
                JTable table = new JTable(tableData, columnNames){
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // toate celulele sunt doar pentru afișare
                    }
                };
                stylizeTable(table);
                table.setFont(new Font("Times New Roman", Font.PLAIN, 12));
                table.setRowHeight(30);
                table.setGridColor(new Color(200, 200, 200));
                table.setShowGrid(true);
                table.setSelectionBackground(new Color(0, 120, 215));
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                JScrollPane scrollPane = new JScrollPane(table);

                // 🔍 Căsuță de căutare
                JTextField searchField = new JTextField(30);
                stylizeSearchField(searchField);
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
                Map<String, List<DrugInfo>> drugMap = drugManipulator.loadDrugMapFromJSON("src/main/resources/generated_gene_drug_summary.json");
                List<Object[]> drugRows = GeneInteractionNetworkv2.filterDrugsForGene(data, drugMap);
                String[] columnNames = {"Gene", "Drug", "Indication", "Mechanism"};
                JTable drugTable = new JTable(drugRows.toArray(new Object[0][]), columnNames){

                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false; // toate celulele sunt doar pentru afișare
                    }
                };
                stylizeTable(drugTable);
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

    private DataModel.Gene findGeneByName(List<DataModel.Gene> genes, String geneSymbol) {
        for (DataModel.Gene gene : genes) {
            if (gene.getName().equalsIgnoreCase(geneSymbol)) {
                return gene;
            }
        }
        return null;
    }

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