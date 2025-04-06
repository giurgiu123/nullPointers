package DataModel;


public class Relation {
    private Entry entry1;
    private Entry entry2;
    private String relationType; // "inhibition" or "activation"

    public Relation(Entry entry1, Entry entry2, String relationType) {
        this.entry1 = entry1;
        this.entry2 = entry2;
        if (relationType.equalsIgnoreCase("inhibition")) {
            this.relationType = "inhibition";
        } else {
            this.relationType = "activation";
        }
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

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        if (relationType.equalsIgnoreCase("inhibition")) {
            this.relationType = "inhibition";
        } else {
            this.relationType = "activation";
        }
    }

    @Override
    public String toString() {
        return "Relation [Entry1=" + entry1 + ", Entry2=" + entry2 + ", RelationType=" + relationType + "]";
    }

}