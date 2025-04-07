# Gene Explorer – Intelligent Drug Repurposing Through Gene Pathway Analysis

**Team:** nullPointers  
**Event:** PoliHack 2025  
**Challenge:** Drug Repurposing via Gene Similarity – QIAGEN Digital Insights  

---

## Overview

Gene Explorer is a Java-based desktop application designed to facilitate the discovery of drug repurposing opportunities by analyzing genetic similarities and biological pathways. The tool enables users to search for a specific gene, visualize its biological context and interaction network, and identify existing drugs that may be effective against associated conditions.

Developed as part of the PoliHack 2025 competition, this project brings a scientific approach to accelerating treatment discovery using curated biological datasets.

---

## Core Features

### Gene Information & Biological Role
- Input a gene of interest (e.g., `EGFR`, `TP53`)
- Retrieve key details:
  - Gene name and description
  - Chromosomal location
  - Involvement in biological pathways
  - Known associated diseases
  - Interactive graph of gene-to-gene relationships

### Gene Interaction Network
- Visualized using GraphStream
- Pathway-based gene similarity mapping
- Interactive graph with dynamic node exploration
  - Central node (query gene) in green
  - Related high-similarity genes in orange
- Click-to-explore node behavior to navigate deeper connections

### Drug Repurposing Insights
- Presents a curated list of drugs already in use for related conditions
- Highlights drugs potentially effective against the queried gene based on biological similarity
- Each drug entry includes:
  - Drug name
  - Current indications
  - Mechanism of action
  - Repurposing relevance score

---

## User Interface

- **Top:** Gene search input
- **Left panel:** Biological summary of the selected gene
- **Center panel:** Network visualization of gene interactions
- **Right panel:** Suggested repurposable drugs with associated data

---

## Technical Implementation

- **Language:** Java
- **GUI Framework:** Java Swing
- **Visualization:** GraphStream for network rendering
- **Data Storage:** Custom JSON/CSV datasets for genes and drug mappings
- **External Data Sources:** Prepared from NCBI and KEGG databases

---

## Project Architecture

```
src/
├── BusinessLogic/         # Data retrieval, caching, and processing logic
├── DataModel/             # Gene, Drug, and Pathway data structures
├── GUI/                   # Main UI components
│   └── GraphComponents/   # Custom graph rendering elements
├── resources/             # JSON datasets, images, and loaders
```

---

## Sample Datasets

- `genes_database.json`: Detailed gene metadata including KEGG IDs and chromosomal data
- `generated_gene_drug_summary.json`: Drug associations and known disease applications

---

## Getting Started

1. **Install Java** (IntelliJ IDEA recommended)
2. **Clone the repository**:
   ```
   git clone <repository-link>
   cd gene-explorer
   ```
3. **Build the project**:
   ```
   mvn clean package
   ```
4. **Run the application**:
   ```
   java -jar target/gene-explorer-1.0.jar
   ```

---

## How to Use

1. Enter the name of a gene.
2. View detailed biological and chromosomal information.
3. Explore the interactive gene interaction graph.
4. Analyze the list of suggested drugs for potential repurposing.

---

## Future Enhancements

- Integration with live APIs (NCBI, KEGG, DrugBank)
- Advanced drug scoring algorithm
- UI refinement with JavaFX or Cytoscape.js for web-based deployment

---

## Acknowledgements

Developed at **PoliHack 2025** by the `nullPointers` team. Inspired by QIAGEN Digital Insights' challenge on drug repurposing through pathway analysis.
