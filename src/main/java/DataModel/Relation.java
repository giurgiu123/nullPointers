package DataModel;

import DataModel.EntryType.Entry;

import java.util.ArrayList;
import java.util.List;

public class Relation {
    private Entry entry1;
    private Entry entry2;
    private String Type;
    private List<RelationSubtype> RelationSubtypeList = new ArrayList<>();
    private RelationType relationType;

    public Relation(Entry entry1, Entry entry2, String type, RelationType relationType) {
        this.entry1 = entry1;
        this.entry2 = entry2;
        Type = type;
        this.relationType = relationType;
    }

    public Entry getEntry1() {
        return entry1;
    }

    public void setEntry1(Entry entry1) {
        this.entry1 = entry1;
    }

    public Entry getEntry2() {
        return entry2;
    }

    public void setEntry2(Entry entry2) {
        this.entry2 = entry2;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public List<RelationSubtype> getRelationSubtypeList() {
        return RelationSubtypeList;
    }

    public void setRelationSubtypeList(List<RelationSubtype> relationSubtypeList) {
        RelationSubtypeList = relationSubtypeList;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }
}
