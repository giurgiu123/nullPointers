package DataModel;

public enum RelationType {
    PPrel,
    Activation,
    Inhibition,
    Phosphorylation;

    @Override
    public String toString() {
        switch (this) {
            case PPrel:
                return "PPrel";
            case Activation:
                return "Activation";
            case Inhibition:
                return "Inhibition";
            case Phosphorylation:
                return "Phosphorylation";
            default:
                return super.toString();
        }
    }
}
