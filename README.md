
# 🧬 Gene Explorer - Drug Repurposer App

**Team:** nullPointers  
**Event:** PoliHack 2025  
**Challenge:** Genes Scary — Drug Repurposing Tool by QIAGEN Digital Insights  

---

## 🚀 Overview

Gene Explorer is a desktop Java application developed for PoliHack2025, aiming to assist researchers in exploring genes of interest and uncovering potential drug repurposing opportunities. Users can input a gene name and visualize its interactions, associated diseases, and possible drugs that could be used to target it — speeding up the path toward treatments for rare or complex diseases.

---

## 🎯 Objectives

- Allow users to **search for a gene** and view relevant biological info.
- Display a **gene-gene interaction network**.
- Identify and suggest **repurposable drugs** for the selected gene and its interactors.
- Present insights through a clean and interactive **graphical interface**.

---

## 🧩 Features

### ✅ Gene Overview
- Input a gene name (e.g., `TP53`, `BRCA1`)
- Outputs:
  - Full gene name
  - Biological role
  - Associated diseases

### ✅ Interaction Network
- Graph view placing the selected gene at the center.
- Connects with related genes based on pathways.
- Highlights similar genes based on pathway overlap.

### ✅ Drug Repurposing Suggestions
- Shows known drugs associated with the target gene and its neighbors.
- Displays each drug’s:
  - Name
  - Original indication
  - Mechanism of action
  - **Repurposing Score** (a heuristic prioritizing relevance)

---

## 🖼️ UI Layout

- **Top**: Gene search bar
- **Left Panel**: Gene information summary
- **Center**: Interactive network visualization (Pathway-based)
- **Right Panel**: Suggested drugs and repurposing potential

---

## 🛠️ Technologies Used

- **Java (Swing)** for GUI
- **Custom JSON datasets** for genes, drugs, and interactions
- **Data Models**: Java classes for genes, pathways, drugs, etc.
- **Graph Logic**: Java-based network mapping from pathway relationships

---

## 📁 Project Structure

```
src/
├── main/java/
│   ├── Data/                  # Pathway parsing & JSON utilities
│   ├── DataModel/             # POJOs for Gene, Drug, Relation etc.
│   ├── GUI/                   # GUI logic
│   │   └── GraphComponents/   # Graph drawing components
│
├── resources/                 # JSONs & visual assets (e.g., gif loaders)
```

---

## 🧪 Sample Datasets

- `genes_database.json` – Contains mock gene info
- `generated_gene_drug_summary.json` – Gene-drug relationships for suggestions
- `dna.gif`, `loading.gif` – Visual elements for app interactivity

---

## 🧠 How Drug Repurposing Works

Drug repurposing involves using existing approved drugs to treat diseases they weren’t originally intended for — leveraging their known safety profiles to reduce development time and cost. By mapping genes to shared pathways and existing drug interactions, our app identifies new potential therapeutic matches based on biological logic.

> “Speed, cost-effectiveness, and cross-disease insights — that's the power of repurposing.” 【5†source】

---

## 💡 Sample Use Case

1. Input gene: **TP53**
2. View:
   - Summary: Tumor protein p53 involved in DNA repair.
   - Network: Links to **BRCA1**, **ATM**, etc.
   - Drugs: Includes **Tamoxifen**, **Olaparib** (with repurposing scores).

---

## 📌 Future Work

- Integrate real-time API calls to:
  - NCBI Entrez (Gene info)
  - KEGG (Pathway mapping)
  - DrugBank/OpenTargets (Drug-gene data)
- Add advanced scoring for drug relevance
- Improve UI with Cytoscape.js or JavaFX for richer interaction

---

## 📽️ Demo

(Coming soon...)  
Shows a quick walkthrough of how a user can search a gene, see interactions, and discover drugs for repurposing.

---

## 👏 Credits

Developed with passion at **PoliHack 2025** by the `nullPointers` team. Inspired by the challenge set by **QIAGEN Digital Insights**.

---
