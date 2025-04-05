package DataModel.EntryType;

import DataModel.KeggGraphics;

import java.util.ArrayList;
import java.util.List;

public class EntryMapType extends Entry{
    private List<Entry> components =  new ArrayList<>();

    public EntryMapType(int id, String name, KeggGraphics graphics, List<Entry> components) {
        super(id, name, null, graphics);
        this.components = components;
    }
    public List<Entry> getComponents() {
        return components;
    }

    @Override
    public String toString() {
        return "GroupEntry[" + getName() + " - Components: " + components.size() + "]";
    }
}
