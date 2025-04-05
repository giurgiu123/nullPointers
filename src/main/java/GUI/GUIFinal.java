package GUI;

import Data.DbGene;
import Data.DrugManipulator;
import Data.GeneList;
import DataModel.Drug;
import DataModel.Gene;
import javax.swing.*;

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