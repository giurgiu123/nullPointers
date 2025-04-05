package GUI.GraphComponents;

import java.util.List;

public class InteractionData {
    List<GeneNode> nodes;
    List<GeneEdge> edges;
    public InteractionData(List<GeneNode> nodes, List<GeneEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }
}
