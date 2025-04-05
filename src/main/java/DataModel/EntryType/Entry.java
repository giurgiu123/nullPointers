package DataModel.EntryType;

import DataModel.KeggGraphics;

public abstract class Entry {
    private int id;
    private String name;
    private String link;
    private KeggGraphics graphics;

    public Entry(int id, String name, String link, KeggGraphics graphics) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.graphics = graphics;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public KeggGraphics getGraphics() {
        return graphics;
    }

    public void setGraphics(KeggGraphics graphics) {
        this.graphics = graphics;
    }
}
