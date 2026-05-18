package fr.insee.formationapirest.integrationtests.glue;

import fr.insee.formationapirest.integrationtests.utils.DatabaseManager;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BddGlue implements En {

    private static final String SCHEMA = "formation";

    public BddGlue(DatabaseManager databaseManager) {

        Before(databaseManager::clearDatabase);

        Given(
                "les vins",
                (DataTable dataTable) -> {
                    var dataToSave = dataTableToListMaps(dataTable);
                    completerVariableOptionel(dataToSave, "appellation", "Pommard");
                    databaseManager.insertInTable(SCHEMA, "vin", dataToSave);
                });
    }

    public static List<Map<String, String>> dataTableToListMaps(DataTable dataTable) {
        List<Map<String, String>> dataAsMaps = toModifiableList(dataTable);
        dataAsMaps.forEach(
                map -> {
                    map.forEach(
                            (key, value) -> {
                                if ("[empty]".equals(value)) {
                                    map.put(key, "");
                                }
                                if ("[blank]".equals(value)) {
                                    map.put(key, " ");
                                }
                            });
                });
        return dataAsMaps;
    }

    private static List<Map<String, String>> toModifiableList(DataTable dataTable) {
        return dataTable.asMaps().stream().map(HashMap::new).collect(Collectors.toList());
    }

    private void completerVariableOptionel(
            List<Map<String, String>> donnees, String colonne, String valeur) {
        if (!donnees.getFirst().containsKey(colonne)) {
            donnees.forEach(map -> map.put(colonne, valeur));
        }
    }

}
