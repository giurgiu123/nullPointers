package DataModel.EntryType;

import DataModel.KeggGraphics;

import java.util.List;

public class CompoundType extends Entry{

    public CompoundType(int id, String name,String link, KeggGraphics graphics) {
        super(id, name, link, graphics);
    }
    @Override
    public String toString() {
        return "CompoundType [id=" + getId() + ", name=" + getName() + ", graphics=" + getGraphics() + "]";
    }

}
