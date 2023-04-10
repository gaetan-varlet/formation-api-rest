package fr.insee.formationapirest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.insee.formationapirest.model.Vin;

public interface VinRepository {

    List<Vin> findAll();

    List<Vin> findByAppellation(String app);

    boolean existsById(Integer id);

    Optional<Vin> findById(Integer id);

    Vin save(Vin vin);

    void deleteById(Integer id);

    Page<Vin> findAll(Pageable p);

    List<String> getListeAppellationJpa();

}
