package DataModel;

import java.util.List;

public class Drug {
    private String Drug;
    private List<String> Gene;
    private List<String> Disease;
    private List<String> Mechanism;

    public String getDrug() { return Drug; }
    public void setDrug(String drug) { this.Drug = drug; }

    public List<String> getGene() { return Gene; }
    public void setGene(List<String> gene) { this.Gene = gene; }

    public List<String> getDisease() { return Disease; }
    public void setDisease(List<String> disease) { this.Disease = disease; }

    public List<String> getMechanism() { return Mechanism; }
    public void setMechanism(List<String> mechanism) { this.Mechanism = mechanism; }

    @Override
    public String toString() {
        return "Drug: " + Drug + "\nGenes: " + Gene + "\nDiseases: " + Disease + "\nMechanisms: " + Mechanism + "\n";
    }
}
