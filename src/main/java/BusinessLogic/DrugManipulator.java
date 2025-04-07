package BusinessLogic;

import GUI.DrugInfo;
import GUI.GraphComponents.InteractionData;
import java.util.List;
import java.util.Map;

public interface DrugManipulator {

    public Map<String, List<DrugInfo>> loadDrugMapFromJSON(String filePath);

    public void displayNetworkAndDrugSuggestions(InteractionData data);
}
