package fr.insee.formationapirest.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.stereotype.Repository;

@Repository
public class VinRepositoryCustomImpl implements VinRepositoryCustom {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getListeAppellationJpa() {
		String sql = "select distinct appellation from formation.vin";
		Query query = entityManager.createNativeQuery(sql);
		List<String> retour = query.getResultList();
		return retour;
	}
	
	@Override
	public List<String> getListeAppellationJdbc() {
		List<String> retour = new ArrayList<>();
		String sql = "select distinct appellation from formation.vin";
		try(
				Connection connection = ((EntityManagerFactoryInfo) entityManager.getEntityManagerFactory())
				.getDataSource().getConnection();
				PreparedStatement ps = connection.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();){
			while(rs.next()){
				retour.add(rs.getString("appellation"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("erreur SQL : " + e.getMessage());
		}
		return retour;
	}
}
