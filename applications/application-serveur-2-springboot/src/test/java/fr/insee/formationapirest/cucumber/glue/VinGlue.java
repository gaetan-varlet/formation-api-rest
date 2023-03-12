package fr.insee.formationapirest.cucumber.glue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.repository.VinRepository;
import fr.insee.formationapirest.service.VinService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VinGlue {

    private final VinRepository vinRepository;
    private final VinService vinService;

    private List<Vin> vins;
    private String exception;

    @Before
    public void clearDatabase() {
        vinRepository.deleteAllInBatch();
    }

    @Given("il n'y a pas de données en base")
    public void baseVide() {
        clearDatabase();
    }

    @Given("des vins avec les attributs suivants")
    public void givenVins(DataTable dataTable) {
        List<Map<String, String>> dataAsMaps = dataTable.asMaps();
        List<Vin> toSave = dataAsMaps.stream().map(this::transformMapToVin).toList();
        vinRepository.saveAll(toSave);
    }

    @Given("je veux créer un vin avec les attributs suivants")
    public void saveVins(DataTable dataTable) {
        try {
            List<Map<String, String>> line = dataTable.asMaps();
            vinService.add(transformMapToVin(line.get(0)));
            exception = null;
        } catch (Exception e) {
            exception = e.getMessage();
        }
    }

    private Vin transformMapToVin(Map<String, String> line) {
        Vin v = new Vin();
        v.setChateau(line.get("chateau"));
        v.setAppellation(line.get("appellation"));
        v.setPrix(Double.valueOf(line.get("prix")));
        return v;
    }

    @When("je récupère tous les vins")
    public void getVins() {
        vins = vinService.findAll(null);
    }

    @When("je récupère tous les vins de l'appellation {string}")
    public void getVinsApp(String app) {
        vins = vinService.findAll(app);
    }

    @Then("le nombre de vins est {int}")
    public void nbVins(int nbExcepted) {
        assertThat(vins).hasSize(nbExcepted);
    }

    @Then("j'ai le message d'erreur suivant {string}")
    public void verifierErreur(String expectedError) {
        assertThat(exception).isEqualTo(expectedError);
    }

    @Then("le vin du chateau {string} de l'appellation {string} à {double}€ est renvoyé")
    public void verifVins(String chateau, String appellation, Double prix) {
        List<Vin> vinsFiltres = vins.stream()
                .filter(v -> v.getChateau().equals(chateau) && v.getAppellation().equals(appellation)).toList();
        assertThat(vinsFiltres).hasSize(1);
        assertThat(vinsFiltres.get(0).getPrix()).isEqualTo(prix);
    }

}
