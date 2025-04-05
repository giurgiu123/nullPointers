package Data;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PathwayParser implements GetParsePath{

    // Atribut pentru o genă (poate fi folosit pentru referință)
    private String gene;

    public PathwayParser(String gene) {
        this.gene = gene;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    // Clasa Entry: reprezintă o intrare din KGML cu atributele ID, NUME și TYPE.
    public static class Entry {
        private String id;
        private String name;
        private String type;

        public Entry(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Entry [ID=" + id + ", Name=" + name + ", Type=" + type + "]";
        }
    }

    // Clasa Relation: reprezintă o relație între două intrări.
    // Are două atribute de tip Entry și un string care reține tipul relației.
    // Dacă tipul extras nu este "inhibition", se va seta ca "activation".
    public static class Relation {
        private Entry entry1;
        private Entry entry2;
        private String relationType;

        public Relation(Entry entry1, Entry entry2, String relationType) {
            this.entry1 = entry1;
            this.entry2 = entry2;
            if (relationType.equalsIgnoreCase("inhibition")) {
                this.relationType = "inhibition";
            } else {
                this.relationType = "activation";
            }
        }

        public Entry getEntry1() {
            return entry1;
        }

        public Entry getEntry2() {
            return entry2;
        }

        public String getRelationType() {
            return relationType;
        }

        @Override
        public String toString() {
            return "Relation [Entry1=" + entry1 + ", Entry2=" + entry2 + ", RelationType=" + relationType + "]";
        }
    }

    /**
     * Metoda parseKGML descarcă fișierul KGML pentru un pathway dat (de exemplu "hsa04140")
     * și parcurge elementele <entry>. Pentru fiecare element <entry> se extrag atributele "id",
     * "name" și "type", creând un obiect Entry care este adăugat la o listă.
     */
    public List<Entry> parseKGML(String pathwayId) throws Exception {
        // Construim URL-ul pentru KGML (ex: https://rest.kegg.jp/get/hsa04140/kgml)
        String urlString = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
        String kgmlText = getTextFromUrl(urlString);

        // Inițializăm parser-ul XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(kgmlText.getBytes(StandardCharsets.UTF_8)));
        doc.getDocumentElement().normalize();

        List<Entry> entries = new ArrayList<>();
        NodeList entryNodes = doc.getElementsByTagName("entry");
        for (int i = 0; i < entryNodes.getLength(); i++) {
            Node node = entryNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String id = element.getAttribute("id");
                String name = element.getAttribute("name");
                String type = element.getAttribute("type");
                Entry entry = new Entry(id, name, type);
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * Metoda parseRelations descarcă același fișier KGML pentru un pathway dat,
     * parcurge elementele <relation> și, pentru fiecare relație, extrage atributele "entry1" și "entry2"
     * și tipul relației din elementul <subtype>. Se construiește un obiect Relation pentru fiecare relație.
     */
    public List<Relation> parseRelations(String pathwayId) throws Exception {
        // Descărcăm KGML
        String urlString = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
        String kgmlText = getTextFromUrl(urlString);

        // Parse XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(kgmlText.getBytes(StandardCharsets.UTF_8)));
        doc.getDocumentElement().normalize();

        // Parcurgem intrările pentru a crea o hartă de tip id -> Entry
        List<Entry> entries = parseKGML(pathwayId);
        Map<String, Entry> entryMap = new HashMap<>();
        for (Entry e : entries) {
            entryMap.put(e.getId(), e);
        }

        List<Relation> relations = new ArrayList<>();
        NodeList relationNodes = doc.getElementsByTagName("relation");
        for (int i = 0; i < relationNodes.getLength(); i++) {
            Node relNode = relationNodes.item(i);
            if (relNode.getNodeType() == Node.ELEMENT_NODE) {
                Element relElem = (Element) relNode;
                String entry1Id = relElem.getAttribute("entry1");
                String entry2Id = relElem.getAttribute("entry2");
                // Implicit, dacă nu se găsește subtype, se setează "activation"
                String relationType = "activation";
                NodeList subtypeNodes = relElem.getElementsByTagName("subtype");
                if (subtypeNodes.getLength() > 0) {
                    Element subtypeElem = (Element) subtypeNodes.item(0);
                    String subtypeValue = subtypeElem.getAttribute("name");
                    if (subtypeValue.equalsIgnoreCase("inhibition")) {
                        relationType = "inhibition";
                    }
                }
                Entry entry1 = entryMap.get(entry1Id);
                Entry entry2 = entryMap.get(entry2Id);
                if (entry1 != null && entry2 != null) {
                    Relation relation = new Relation(entry1, entry2, relationType);
                    relations.add(relation);
                }
            }
        }
        return relations;
    }

    // Metodă care afișează intrările (entry) din KGML pentru un pathway dat.
    public void displayEntries(String pathwayId) throws Exception {
        List<Entry> entries = parseKGML(pathwayId);
        System.out.println("Entries for pathway " + pathwayId + ":");
        for (Entry e : entries) {
            System.out.println(e);
        }
    }

    // Metodă care afișează relațiile (relation) din KGML pentru un pathway dat.
    public void displayRelations(String pathwayId) throws Exception {
        List<Relation> relations = parseRelations(pathwayId);
        System.out.println("Relations for pathway " + pathwayId + ":");
        for (Relation r : relations) {
            System.out.println(r);
        }
    }

    // Metodă utilitară pentru a descărca textul de la un URL.
    private String getTextFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line).append("\n");
        }
        in.close();
        return sb.toString();
    }

    // Metoda main de test: se apelează displayEntries și displayRelations pentru un pathway dat (ex: "hsa04140").
    public static void main(String[] args) {
        try {
            PathwayParser parser = new PathwayParser("TP53");
            // Afișează intrările
            parser.displayEntries("hsa04140");
            System.out.println("\n----------------------\n");
            // Afișează relațiile
            parser.displayRelations("hsa04140");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}