package GUI.GraphComponents;

public class GeneNode {
    String id;
    String symbol;
    String type; // "central", "interactor", "similar", "related"
    public GeneNode(String id, String symbol, String type) {
        this.id = id;
        this.symbol = symbol;
        this.type = type;
    }
}