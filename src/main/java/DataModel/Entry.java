
package DataModel;



public class Entry {
    private String id;
    private String name;
    private String type;
    private GraphicsEntry graphics;

    public Entry(String id, String name, String type,GraphicsEntry graphicsEntry) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.graphics = graphicsEntry;
    }

    public GraphicsEntry getGraphics() {
        return graphics;
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
    public String toString(){
        return "Entry [ID=" + id + ", Name=" + name + ", Type=" + type + "]";
    }



}