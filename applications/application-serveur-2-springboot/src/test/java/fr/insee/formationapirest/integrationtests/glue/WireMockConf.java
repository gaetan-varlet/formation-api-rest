package fr.insee.formationapirest.integrationtests.glue;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import io.cucumber.java8.En;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WireMockConf implements En {

    WireMockServer wireMockServer;

    public WireMockConf() {

        Before(
                () -> {
                    wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8082));
                    wireMockServer.start();
                    configureFor("localhost", 8082);
                    log.debug("démarrage de wireMockServer");
                });

        After(
                () -> {
                    if (wireMockServer != null) {
                        log.debug("arrêt de wireMockServer");
                        wireMockServer.stop();
                    }
                });
    }
}
