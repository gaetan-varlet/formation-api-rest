package fr.insee.formationapirest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.insee.formationapirest.model.Vin;

@Repository
public interface VinRepositorySpringData extends JpaRepository<Vin, Integer> {

    List<Vin> findByAppellation(String appellation);

}