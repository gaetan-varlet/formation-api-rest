package fr.insee.formationapirest.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import fr.insee.formationapirest.model.Vin;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class VinRepositoryCustomImpl implements VinRepository {

	private final VinRepositorySpringData vinRepositorySpringData;

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Vin> findAll() {
		return vinRepositorySpringData.findAll();
	}

	@Override
	public List<Vin> findByAppellation(String app) {
		return vinRepositorySpringData.findByAppellation(app);
	}

	@Override
	public boolean existsById(Integer id) {
		return vinRepositorySpringData.existsById(id);
	}

	@Override
	public Optional<Vin> findById(Integer id) {
		return vinRepositorySpringData.findById(id);
	}

	@Override
	public Vin save(Vin vin) {
		return vinRepositorySpringData.save(vin);
	}

	@Override
	public void deleteById(Integer id) {
		vinRepositorySpringData.deleteById(id);
	}

	@Override
	public Page<Vin> findAll(Pageable p) {
		return vinRepositorySpringData.findAll(p);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getListeAppellationJpa() {
		String sql = "select distinct appellation from formation.vin";
		Query query = entityManager.createNativeQuery(sql);
		return query.getResultList();
	}

}
