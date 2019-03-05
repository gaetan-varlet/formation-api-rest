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

	public List<String> getListeAppellation2(){
		List<String> retour = new ArrayList<>();
		try {
			Connection connection = ((EntityManagerFactoryInfo) entityManager.getEntityManagerFactory())
					.getDataSource().getConnection();
			String sql = "select distinct appellation from formation.vin";
			PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				retour.add(rs.getString("appellation"));
			}
			rs.close();
			ps.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("erreur SQL : " + e.getMessage());
		}
		return retour;
	}
}
