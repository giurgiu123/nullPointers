package GUI;

public class DrugInfo {
    String geneSymbol;
    String drugName;
    String indication;
    String mechanism;
    public DrugInfo(String geneSymbol, String drugName, String indication, String mechanism) {
        this.geneSymbol = geneSymbol;
        this.drugName = drugName;
        this.indication = indication;
        this.mechanism = mechanism;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getIndication() {
        return indication;
    }

    public void setIndication(String indication) {
        this.indication = indication;
    }

    public String getMechanism() {
        return mechanism;
    }

    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }
}
