package fr.insee.formationapirest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.insee.formationapirest.model.Vin;

@Repository
public interface VinRepository extends JpaRepository<Vin, Integer> {
	
}