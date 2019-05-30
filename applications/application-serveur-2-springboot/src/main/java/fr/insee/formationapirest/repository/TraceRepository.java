package fr.insee.formationapirest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.insee.formationapirest.model.Trace;

@Repository
public interface TraceRepository extends JpaRepository<Trace, Integer> {
	
}