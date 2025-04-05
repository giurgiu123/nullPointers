package DataModel;

import GUI.GraphComponents.GeneNode;

import java.awt.*;

public class Entry {
    private String id;
    private String name;
    private String type;
    private GraphicsEntry graphics; // coordonate și dimensiuni

    public Entry(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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