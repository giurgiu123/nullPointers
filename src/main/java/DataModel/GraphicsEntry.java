package DataModel;

public class GraphicsEntry {
    private int x;
    private int y;
    private static int width=46;
    private static int height = 17;

    public GraphicsEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static int getWidth() {
        return width;
    }

    public static void setWidth(int width) {
        GraphicsEntry.width = width;
    }

    public static int getHeight() {
        return height;
    }

    public static void setHeight(int height) {
        GraphicsEntry.height = height;
    }
}
