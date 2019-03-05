package fr.insee.formationapirest.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository
public class VinDao {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@SuppressWarnings("unchecked")
	public List<String> getListeAppellation() {
		String sql = "select distinct appellation from formation.vin";
		Query query = entityManager.createNativeQuery(sql);
		List<String> retour = query.getResultList();
		return retour;
	}
}
