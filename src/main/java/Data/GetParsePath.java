package Data;

import java.util.List;

public interface GetParsePath {
    public List<PathwayParser.Relation> parseRelations(String pathwayId) throws Exception ;
    public List<PathwayParser.Entry> parseKGML(String pathwayId) throws Exception ;
}
