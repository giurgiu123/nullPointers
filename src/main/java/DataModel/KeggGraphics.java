package DataModel;

public class KeggGraphics {
    private String name;
    private String fgColor;
    private String bgColor;
    private String type;
    private int x;
    private int y;
    private int width;
    private int height;

    public KeggGraphics(String name, String fgColor, String bgColor, String type,
                        int x, int y, int width, int height) {
        this.name = name;
        this.fgColor = fgColor;
        this.bgColor = bgColor;
        this.type = type;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Getters
    public String getName() { return name; }
    public String getFgColor() { return fgColor; }
    public String getBgColor() { return bgColor; }
    public String getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    @Override
    public String toString() {
        return String.format("Graphics[%s @ (%d,%d), %dx%d, bg: %s]", name, x, y, width, height, bgColor);
    }
}
