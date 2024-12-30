package fr.insee.formationapirest.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TemperatureService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofMillis(5_000))
            .build();

    @Value("${temperature.base-url}")
    private String baseUrl;

    public Double getTemperature() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(baseUrl + "/temperature")).GET().build();
        log.debug(
                "requête de récupération de la température : {}",
                httpRequest.toString());
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        log.debug("code réponse HTTP : {}", response.statusCode());
        log.debug("body réponse HTTP : {}", response.body());
        return Double.valueOf(response.body());
    }

}
