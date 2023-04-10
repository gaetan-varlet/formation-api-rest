package fr.insee.formationapirest.integrationtests.glue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepositoryCustomImpl;
import fr.insee.formationapirest.service.VinService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;

public class VinGlue implements En {

    private List<Vin> vins;
    private String exception;

    public VinGlue(VinRepositoryCustomImpl vinRepository, VinService vinService) {

        Before(() -> {
            // clearDatabase
            vinRepository.findAll().forEach(v -> vinRepository.deleteById(v.getId()));
        });

        Given("il n'y a pas de données en base", () -> {
            vinRepository.findAll().forEach(v -> vinRepository.deleteById(v.getId()));
        });

        Given("des vins avec les attributs suivants", (DataTable dataTable) -> {
            List<Map<String, String>> dataAsMaps = dataTable.asMaps();
            List<Vin> toSave = dataAsMaps.stream().map(this::transformMapToVin).toList();
            toSave.forEach(v -> vinRepository.save(v));
        });

        Given("je veux créer un vin avec les attributs suivants", (DataTable dataTable) -> {
            try {
                List<Map<String, String>> line = dataTable.asMaps();
                vinService.add(transformMapToVin(line.get(0)));
                exception = null;
            } catch (Exception e) {
                exception = e.getMessage();
            }
        });

        When("je récupère tous les vins", () -> {
            vins = vinService.findAll(null);
        });

        When("je récupère tous les vins de l'appellation {string}", (String app) -> {
            vins = vinService.findAll(app);
        });

        Then("le nombre de vins est {int}", (Integer nbExcepted) -> {
            assertThat(vins).hasSize(nbExcepted);
        });

        Then("j'ai le message d'erreur suivant {string}", (String expectedError) -> {
            assertThat(exception).isEqualTo(expectedError);
        });

        Then("le vin du chateau {string} de l'appellation {string} à {double}€ est renvoyé",
                (String chateau, String appellation, Double prix) -> {
                    List<Vin> vinsFiltres = vins.stream()
                            .filter(v -> v.getChateau().equals(chateau) && v.getAppellation().equals(appellation))
                            .toList();
                    assertThat(vinsFiltres).hasSize(1);
                    assertThat(vinsFiltres.get(0).getPrix()).isEqualTo(prix);
                });
    }

    private Vin transformMapToVin(Map<String, String> line) {
        Vin v = new Vin();
        v.setChateau(line.get("chateau"));
        v.setAppellation(line.get("appellation"));
        v.setPrix(Double.valueOf(line.get("prix")));
        return v;
    }

}
