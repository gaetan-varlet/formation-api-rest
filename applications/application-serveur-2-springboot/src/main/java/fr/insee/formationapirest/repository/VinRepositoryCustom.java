package fr.insee.formationapirest.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

@Repository
public interface VinRepositoryCustom {
    List<String> getListeAppellationJpa();
    List<String> getListeAppellationJdbc();
}