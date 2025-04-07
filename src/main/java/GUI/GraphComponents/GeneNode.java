package GUI.GraphComponents;

import java.awt.*;

public class GeneNode {
    String id;
    String symbol;
    String type; // "central", "interactor", "similar", "related"
    Color color = Color.BLUE;
    public GeneNode(String id, String symbol, String type) {
        this.id = id;
        this.symbol = symbol;
        this.type = type;
    }
    private int x, y;

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public String getType() {
        return this.type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeneNode)) return false;
        GeneNode gn = (GeneNode) o;
        return id.equals(gn.id);
    }

    public Object getId() {
        return id;
    }

    public String getName() {
        return symbol;
    }
   // default (izolat)

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}