package DataModel;

import DataModel.EntryType.Entry;

import java.util.List;
import java.util.Objects;

public class PathWay {
    private String path;
    private List<Entry> entries;
    private List<Relation> relations;

    public PathWay(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathWay pathWay)) return false;
        return Objects.equals(path, pathWay.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public String getCode() {
        return path;
    }

    // Getters și Setters pentru entries și relations
    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    @Override
    public String toString() {
        return "PathWay { path='" + path + "', entries=" + entries + ", relations=" + relations + " }";
    }
}
