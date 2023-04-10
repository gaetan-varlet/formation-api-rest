package fr.insee.formationapirest.unittests.glue;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import fr.insee.formationapirest.model.Vin;
import fr.insee.formationapirest.service.VinService;
import fr.insee.formationapirest.unittests.mock.VinRepoMock;
import io.cucumber.datatable.DataTable;
import io.cucumber.java8.En;

public class VinGlue implements En {
    private VinRepoMock vinRepository;
    private VinService vinService;
    private List<Vin> vins;

    public VinGlue() {
        Before(() -> {
            vinRepository = new VinRepoMock();
            vinService = new VinService(vinRepository);
        });
        Given("des vins avec les attributs suivants", (DataTable dataTable) -> {
            List<Vin> toSave = dataTable.asMaps().stream().map(this::transformMapToVin).toList();
            toSave.forEach(v -> vinRepository.save(v));
        });
        When("je récupère tous les vins", () -> {
            vins = vinService.findAll(null);
        });
        Then("le nombre de vins est {int}", (Integer nbExcepted) -> {
            assertThat(vins).hasSize(nbExcepted);
        });
    }

    private Vin transformMapToVin(Map<String, String> line) {
        return Vin.builder().chateau(line.get("chateau")).appellation(line.get("appellation"))
                .prix(Double.valueOf(line.get("prix"))).build();
    }
}
