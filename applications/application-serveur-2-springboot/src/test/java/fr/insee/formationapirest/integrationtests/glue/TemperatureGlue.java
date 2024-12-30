package fr.insee.formationapirest.integrationtests.glue;

import org.assertj.core.api.Assertions;

import fr.insee.formationapirest.service.TemperatureService;
import io.cucumber.java8.En;
import lombok.extern.slf4j.Slf4j;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Slf4j
public class TemperatureGlue implements En {

    Double temperature;

    public TemperatureGlue(TemperatureService temperatureService) {
        When(
                "je récupère la température",
                () -> {
                    try {
                        stubFor(get("/temperature").willReturn(ok("5")));
                        temperature = temperatureService.getTemperature();
                    } catch (Exception e) {
                        temperature = null;
                        log.error("ERREUR", e);
                    }
                });

        Then(
                "la température suivante est renvoyée : {double}",
                (Double expectedContent) -> {
                    Assertions.assertThat(temperature).isEqualTo(expectedContent);
                });
    }

}
