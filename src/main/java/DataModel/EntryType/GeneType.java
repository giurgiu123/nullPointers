package DataModel.EntryType;

import DataModel.KeggGraphics;

public class GeneType extends Entry{

    public GeneType(int id, String name,String link, KeggGraphics graphics) {
        super(id, name, link, graphics);
    }
    @Override
    public String toString() {
        return "GeneType [id=" + getId() + ", name=" + getName() + ", graphics=" + getGraphics() + "]";
    }
}
