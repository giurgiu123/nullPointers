package BusinessLogic;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import GUI.GraphComponents.GeneInteractionNetworkv2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import DataModel.*;

public class PathwayParser implements GetParsePath{
    private String gene;

    public PathwayParser(String gene) {
        this.gene = gene;
    }

public List<Entry> parseKGML(String pathwayId) throws Exception {
    String urlString = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
    String kgmlText = GeneInteractionNetworkv2.getTextFromUrl(urlString);

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
            String name = null;
            String type = element.getAttribute("type");

            NodeList graphicsNodes = element.getElementsByTagName("graphics");
            GraphicsEntry graphicsEntry = null;

            if (graphicsNodes.getLength() > 0) {
                Element graphicsElement = (Element) graphicsNodes.item(0);
                name = graphicsElement.getAttribute("name");
                int x = Integer.parseInt(graphicsElement.getAttribute("x"));
                int y = Integer.parseInt(graphicsElement.getAttribute("y"));
                graphicsEntry = new GraphicsEntry(x, y);
            }
            Entry entry = new Entry(id, name, type, graphicsEntry);
            entries.add(entry);
        }
    }
    return entries;
}

    public List<Relation> parseRelations(String pathwayId) throws Exception {
        String urlString = "https://rest.kegg.jp/get/" + pathwayId + "/kgml";
        String kgmlText = GeneInteractionNetworkv2.getTextFromUrl(urlString);

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


}