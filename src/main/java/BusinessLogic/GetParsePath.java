package BusinessLogic;

import DataModel.Entry;
import DataModel.Relation;

import java.util.List;

public interface GetParsePath {

    public List<Relation> parseRelations(String pathwayId) throws Exception ;

    public List<Entry> parseKGML(String pathwayId) throws Exception ;
}
