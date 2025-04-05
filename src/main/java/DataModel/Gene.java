package DataModel;

import scala.util.parsing.combinator.testing.Str;

import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Gene {
    private String name;
    private String idlist;
    private String description;
    private int nrChromosome;
    private String organism;
    private String keggId;
    private List<PathWay> pathWays = new ArrayList<>();

    public Gene(){

    }


    public Gene(String name, String description, int nrChromosome, String idlist, String organism, String keggId) {
        this.name = name;
        this.description = description;
        this.nrChromosome = nrChromosome;
        this.organism = organism;
        this.keggId = keggId;
        this.idlist = idlist;
    }

    @Override
    public String toString() {
        return "Gene {" +
                "\n  name='" + name + '\'' +
                ",\n  description='" + description + '\'' +
                ",\n  chromosome=" + nrChromosome +
                ",\n  organism='" + organism + '\'' +
                ",\n  keggId='" + keggId + '\'' +
                ",\n  idlist='" + idlist + '\'' +  // Afișăm idlist-ul
                "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gene gene)) return false;
        return Objects.equals(keggId, gene.keggId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keggId);
    }

    public void setGeneName(String name) {
        this.name = name;
    }

    public void setIdlist(String idlist) {
        this.idlist = idlist;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setChromosome(int nrChromosome) {
        this.nrChromosome = nrChromosome;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public void setKeggId(String keggId) {
        this.keggId = keggId;
    }

    public void setPathWays(List<PathWay> pathWays) {
        this.pathWays = pathWays;
    }

    public String getName() {
        return name;
    }

    public String getIdlist() {
        return idlist;
    }

    public String getDescription() {
        return description;
    }

    public int getNrChromosome() {
        return nrChromosome;
    }

    public String getOrganism() {
        return organism;
    }

    public String getKeggId() {
        return keggId;
    }

    public List<PathWay> getPathWays() {
        return pathWays;
    }


}
