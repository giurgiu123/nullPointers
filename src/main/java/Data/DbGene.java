package Data;

import DataModel.Gene;
import DataModel.PathWay;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbGene {

    // Lista de gene pe care vrem să le preluăm
    List<String> geneNames = Arrays.asList("EGFR", "TP53", "BRCA1", "BRCA2", "KRAS", "BRAF", "PTEN", "MYC", "CDKN2A", "ALK", "RET", "MET", "PIK3CA", "ERBB2", "PDGFRA",
            "KIT", "FGFR1", "FGFR2", "FGFR3", "PDCD1", "CD274", "CTNNB1", "SMAD4", "APC",
            "MDM2", "AR", "ESR1", "MTHFR", "CYP2D6", "VEGFA", "FGFR4", "KITLG", "SMARCA4",
            "NOTCH1", "WNT1");


    // Lista care va conține obiectele Gene
    List<Gene> genes = new ArrayList<>();

    // Căile către fișierele de ieșire (JSON și CSV)
    private static final String JSON_FILE_PATH = "src/main/resources/genes_database.json";

    public DbGene() {
        // Verificăm dacă fișierul JSON există și dacă este gol
        File jsonFile = new File(JSON_FILE_PATH);
        if (jsonFile.exists() && jsonFile.length() > 0) {
            // Dacă fișierul JSON există și conține date, citim datele din el
            System.out.println("Fișierul JSON conține date. Citim datele din JSON...");
            genes = getGenesFromJsonFile();
            // Afișăm informațiile despre gene citite din fișier
            printGeneInformation(); // Afișează informațiile despre gene citite din fișier
        } else {
            // Dacă fișierul JSON nu există sau este gol, obținem datele de la API
            System.out.println("Fișierul JSON este gol sau nu există. Obținem datele de la API...");
            fetchGeneData();
            // Salvăm datele obținute din API în fișierul JSON
            saveGenesToJson(jsonFile);
        }
    }



    // Metodă nouă: returnează o listă de gene citite din fișierul JSON
    public List<Gene> getGenesFromJsonFile() {
        File file = new File(JSON_FILE_PATH);
        if (!file.exists()) {
            System.out.println("Fisierul JSON nu exista la: " + file.getAbsolutePath());
            return new ArrayList<>();
        }
        return readGenesFromJson(file);
    }

    // Funcție pentru a scrie datele despre gene într-un fișier JSON
    private void saveGenesToJson(File file) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            JSONArray jsonArray = new JSONArray();
            for (Gene gene : genes) {
                JSONObject geneObject = new JSONObject();
                geneObject.put("name", gene.getName());
                geneObject.put("description", gene.getDescription());
                geneObject.put("chromosome", gene.getNrChromosome());
                geneObject.put("organism", gene.getOrganism());
                geneObject.put("keggId", gene.getKeggId());
                geneObject.put("idlist", gene.getIdlist());

                // Salvăm lista de pathways
                JSONArray pathwaysArray = new JSONArray();
                for (PathWay pathway : gene.getPathWays()) {
                    pathwaysArray.put(pathway.getPath()); // Asumăm că PathWay are o metodă getPathwayCode()
                }
                geneObject.put("pathways", pathwaysArray); // Adăugăm pathways în obiectul JSON

                jsonArray.put(geneObject);
            }
            fileWriter.write(jsonArray.toString(4)); // indentare pentru o citire ușoară
            System.out.println("Datele au fost salvate în JSON la: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Funcție pentru a scrie datele despre gene într-un fișier CSV
    private void saveGenesToCsv(File file) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Scriem header-ul CSV
            pw.println("name,description,chromosome,organism,keggId,idlist");
            // Scriem fiecare genă
            for (Gene gene : genes) {
                String line = String.format("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\"",
                        gene.getName(),
                        gene.getDescription().replace("\"", "\"\""),  // escapare ghilimele
                        gene.getNrChromosome(),
                        gene.getOrganism(),
                        gene.getKeggId(),
                        gene.getIdlist());
                pw.println(line);
            }
            System.out.println("Datele au fost salvate în CSV la: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Funcție pentru a citi datele din fișierul JSON
    private List<Gene> readGenesFromJson(File file) {
        List<Gene> genesList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line);
            }
            JSONArray jsonArray = new JSONArray(jsonContent.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject geneObject = jsonArray.getJSONObject(i);
                String name = geneObject.getString("name");
                String description = geneObject.getString("description");
                int chromosome = geneObject.getInt("chromosome");
                String organism = geneObject.getString("organism");
                String keggId = geneObject.getString("keggId");
                String idlist = geneObject.getString("idlist");

                // Creăm obiectul Gene
                Gene gene = new Gene(name, description, chromosome, idlist, organism, keggId);

                // Citim lista de pathways
                JSONArray pathwaysArray = geneObject.getJSONArray("pathways");
                List<PathWay> pathways = new ArrayList<>();
                for (int j = 0; j < pathwaysArray.length(); j++) {
                    String pathwayCode = pathwaysArray.getString(j); // Presupunem că PathWay are doar un cod de pathway
                    pathways.add(new PathWay(pathwayCode)); // Creăm obiectul PathWay pentru fiecare pathway
                }
                gene.setPathWays(pathways); // Setăm lista de pathways pentru genă

                genesList.add(gene);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return genesList;
    }

    // Funcție pentru a obține datele despre gene de la API
    private void fetchGeneData() {
        for (String gene : geneNames) {
            try {
                // STEP 1: Obținem ID-ul genei de la NCBI
                String geneId = getGeneIdFromNCBI(gene);
                if (geneId == null) continue;

                // STEP 2: Obținem detalii despre genă de la NCBI
                JSONObject geneInfo = getGeneInfoFromNCBI(geneId);
                if (geneInfo == null) continue;

                // STEP 3: Obținem KEGG ID pentru genă
                String keggId = getKeggId(gene);
                if (keggId == null) continue;

                // STEP 4: Obținem idlist pentru genă (aici folosim geneId ca idlist)
                String idlist = geneId;

                // Creăm obiectul Gene cu informațiile obținute
                String name = geneInfo.getString("name");
                String description = geneInfo.getString("description");
                int chromosome = Integer.parseInt(geneInfo.getString("chromosome").replaceAll("[^0-9]", "0"));
                String organism = geneInfo.getJSONObject("organism").getString("scientificname");

                Gene geneObj = new Gene(name, description, chromosome, idlist, organism, keggId);
                geneObj.setPathWays(getPathwaysForGene(geneObj.getKeggId()));

                // Adăugăm genă la lista de gene
                genes.add(geneObj);

            } catch (Exception e) {
                e.printStackTrace();  // În caz de eroare, continuăm cu următoarea genă
            }
        }
    }

    // Funcție pentru obținerea ID-ului genei de la NCBI
    private static String getGeneIdFromNCBI(String gene) throws Exception {
        String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=gene&term=" + gene + "[gene]+AND+Homo+sapiens[orgn]&retmode=json";
        JSONObject json = getJsonFromUrl(url);
        JSONArray ids = json.getJSONObject("esearchresult").getJSONArray("idlist");
        return ids.length() > 0 ? ids.getString(0) : null;
    }

    // Funcție pentru obținerea detaliilor despre genă de la NCBI
    private static JSONObject getGeneInfoFromNCBI(String geneId) throws Exception {
        String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&id=" + geneId + "&retmode=json";
        JSONObject json = getJsonFromUrl(url);
        return json.getJSONObject("result").getJSONObject(geneId);
    }

    // Funcție pentru obținerea ID-ului KEGG pentru o genă
    private static String getKeggId(String geneName) throws Exception {
        String url = "https://rest.kegg.jp/find/genes/" + geneName;
        String text = getTextFromUrl(url);
        if (text.isEmpty()) return null;
        String[] lines = text.split("\n");
        return lines[0].split("\t")[0];
    }

    // Funcție pentru obținerea datelor JSON de la un URL
    private static JSONObject getJsonFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line);
        in.close();
        return new JSONObject(response.toString());
    }

    // Funcție pentru obținerea textului de la un URL
    private static String getTextFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) response.append(line).append("\n");
        in.close();
        return response.toString();
    }

    // Funcție pentru a afișa informațiile despre gene în consolă
    public void printGeneInformation() {
        System.out.println("\n🧬 Gene Information:");
        for (Gene gene : genes) {
            System.out.println("Gene: " + gene.getName());
            System.out.println("  Description: " + gene.getDescription());
            System.out.println("  Chromosome: " + gene.getNrChromosome());
            System.out.println("  Organism: " + gene.getOrganism());
            System.out.println("  KEGG ID: " + gene.getKeggId());
            System.out.println("  ID LIST: " + gene.getIdlist());

            // Afișăm pathway-urile asociate genei
            System.out.println("  🛤 Pathways:");
            for (PathWay pathWay : gene.getPathWays()) {
                System.out.println("    Pathway Code: " + pathWay.getPath());
            }

            System.out.println("---------------------------");
        }
    }


    private static List<PathWay> getPathwaysForGene(String keggId) throws Exception {
        String url = "https://rest.kegg.jp/link/pathway/" + keggId;
        String data = getTextFromUrl(url);
        List<PathWay> pathways = new ArrayList<>();
        for (String line : data.split("\n")) {
            if (line.isEmpty()) continue;
            String[] parts = line.split("\t");
            String pathwayCode = parts[1].split(":")[1];
            pathways.add(new PathWay(pathwayCode));
        }
        return pathways;
    }



    public static void main(String[] args) {
        // Creăm obiectul DbGene pentru a prelua și a salva informațiile direct în fișiere
        DbGene dbGene = new DbGene();
        dbGene.printGeneInformation(); // Afișăm informațiile despre gene în consolă

        // Exemplu de utilizare a metodei de citire a genelor din fișierul JSON
//        List<Gene> genesFromJson = dbGene.getGenesFromJsonFile();
//        System.out.println("\nGene citite din fișierul JSON:");
//        for (Gene gene : genesFromJson) {
//            System.out.println(gene.getName());
//        }
   }
}