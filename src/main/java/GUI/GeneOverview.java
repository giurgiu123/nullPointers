package GUI;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GeneOverview {

    public static void main(String[] args) {
        try {
            String gene = "CEPR";
            String BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

            // STEP 1: Get Gene ID from NCBI
            String esearch_url = BASE_URL + "esearch.fcgi?db=gene&term=" + gene + "[gene]+AND+Homo+sapiens[orgn]&retmode=json";
            JSONObject searchResponse = getJsonFromUrl(esearch_url);
            JSONArray idList = searchResponse.getJSONObject("esearchresult").getJSONArray("idlist");
            String geneId = idList.getString(0);

            // STEP 2: Get Gene Info
            String esummary_url = BASE_URL + "esummary.fcgi?db=gene&id=" + geneId + "&retmode=json";
            JSONObject summaryResponse = getJsonFromUrl(esummary_url);
            JSONObject geneInfo = summaryResponse.getJSONObject("result").getJSONObject(geneId);

            System.out.println("🧬 Gene Info:");
            System.out.println("Name: " + geneInfo.getString("name"));
            System.out.println("Description: " + geneInfo.getString("description"));
            System.out.println("Chromosome: " + geneInfo.getString("chromosome"));
            System.out.println("Organism: " + geneInfo.getJSONObject("organism").getString("scientificname"));

            // STEP 3: Get KEGG ID
            String keggFindUrl = "https://rest.kegg.jp/find/genes/" + gene;
            String keggFindResult = getTextFromUrl(keggFindUrl);
            String[] keggLines = keggFindResult.split("\n");
            String keggId = keggLines[0].split("\t")[0]; // hsa:1956

            System.out.println("\n🧭 KEGG ID: " + keggId);

            // STEP 2: Get KEGG Pathways for the Gene
            String keggPathwayUrl = "https://rest.kegg.jp/link/pathway/hsa:" + geneId;
            String keggPathways = getTextFromUrl(keggPathwayUrl);

            // Extract the pathway IDs
            System.out.println("\n🧪 KEGG Pathways for Gene:");
            Map<String, String> pathways = new HashMap<>();
            for (String line : keggPathways.split("\n")) {
                String[] parts = line.split("\t");
                String pathwayId = parts[1];  // path:hsa04012
                String pathwayName = pathwayId.split(":")[1];  // Extract pathway name
                pathways.put(pathwayId, pathwayName);
                System.out.println("- " + pathwayName + " (" + pathwayId + ")");
            }

            // STEP 3: Fetch details for each pathway
            for (String pathwayId : pathways.keySet()) {
                System.out.println("\n🧭 Fetching details for pathway: " + pathwayId);
                String pathwayDetailsUrl = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
                String pathwayDetails = getTextFromUrl(pathwayDetailsUrl);

                // Display pathway details (optional for debugging)
                System.out.println("Pathway KGML Data:\n" + pathwayDetails);

                // STEP 4: Find Gene-Gene Interactions
                findGeneGeneInteractions(pathwayDetails, geneId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String fetchGeneInfoText(String gene) throws Exception {
        StringBuilder result = new StringBuilder();
        String BASE_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";

        // STEP 1: Get Gene ID from NCBI
        String esearch_url = BASE_URL + "esearch.fcgi?db=gene&term=" + gene + "[gene]+AND+Homo+sapiens[orgn]&retmode=json";
        JSONObject searchResponse = getJsonFromUrl(esearch_url);
        JSONArray idList = searchResponse.getJSONObject("esearchresult").getJSONArray("idlist");

        if (idList.isEmpty()) {
            return "❌ Gene not found: " + gene;
        }

        String geneId = idList.getString(0);

        // STEP 2: Get Gene Info
        String esummary_url = BASE_URL + "esummary.fcgi?db=gene&id=" + geneId + "&retmode=json";
        JSONObject summaryResponse = getJsonFromUrl(esummary_url);
        JSONObject geneInfo = summaryResponse.getJSONObject("result").getJSONObject(geneId);

        result.append("🧬 Gene Info\n");
        result.append("Name: ").append(geneInfo.getString("name")).append("\n");
        result.append("Description: ").append(geneInfo.getString("description")).append("\n");
        result.append("Chromosome: ").append(geneInfo.getString("chromosome")).append("\n");
        result.append("Organism: ").append(geneInfo.getJSONObject("organism").getString("scientificname")).append("\n\n");

        // STEP 3: Get KEGG ID
        String keggFindUrl = "https://rest.kegg.jp/find/genes/" + gene;
        String keggFindResult = getTextFromUrl(keggFindUrl);
        String[] keggLines = keggFindResult.split("\n");
        String keggId = keggLines[0].split("\t")[0];

        result.append("🧭 KEGG ID: ").append(keggId).append("\n\n");

        // STEP 4: Get KEGG Pathways
        String keggPathwayUrl = "https://rest.kegg.jp/link/pathway/hsa:" + geneId;
        String keggPathways = getTextFromUrl(keggPathwayUrl);
        result.append("🧪 KEGG Pathways:\n");

        Map<String, String> pathways = new HashMap<>();
        for (String line : keggPathways.split("\n")) {
            String[] parts = line.split("\t");
            if (parts.length >= 2) {
                String pathwayId = parts[1];
                String pathwayName = pathwayId.split(":")[1];
                pathways.put(pathwayId, pathwayName);
                result.append("- ").append(pathwayName).append(" (").append(pathwayId).append(")\n");
            }
        }

        return result.toString();
    }

    private static void findGeneGeneInteractions(String pathwayDetails, String geneId) {
        if (pathwayDetails.contains(geneId)) {
            // Search for interactions related to the gene (activation or inhibition)
            if (pathwayDetails.contains("activation")) {
                System.out.println("Activation relation found for gene: " + geneId);
            }
            if (pathwayDetails.contains("inhibition")) {
                System.out.println("Inhibition relation found for gene: " + geneId);
            }
        }
    }

    // Fetch and parse JSON
    public static JSONObject getJsonFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return new JSONObject(response.toString());
    }

    // Fetch plain text (e.g., from KEGG)
    private static String getTextFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine).append("\n");
        }
        in.close();
        return response.toString();
    }
}