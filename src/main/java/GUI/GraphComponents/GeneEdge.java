package GUI.GraphComponents;

public class GeneEdge {
    String id;
    String source;
    String target;
    String interaction;
    public GeneEdge(String id, String source, String target, String interaction) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.interaction = interaction;
    }

    public String getFrom() {
        return source;
    }

    public String getTo() {
        return target;
    }
}
