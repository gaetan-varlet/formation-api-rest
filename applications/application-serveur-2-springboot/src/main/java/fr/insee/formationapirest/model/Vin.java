package fr.insee.formationapirest.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "vin", schema = "formation")
public class Vin {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_vin")
	@SequenceGenerator(name = "seq_vin", sequenceName = "formation.vin_id_seq", allocationSize = 1)
	private Integer id;
	
	private String chateau;
	private String appellation;
	private Double prix;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getChateau() {
		return chateau;
	}
	
	public void setChateau(String chateau) {
		this.chateau = chateau;
	}
	
	public String getAppellation() {
		return appellation;
	}
	
	public void setAppellation(String appellation) {
		this.appellation = appellation;
	}
	
	public Double getPrix() {
		return prix;
	}
	
	public void setPrix(Double prix) {
		this.prix = prix;
	}
	
	// ajouter les getters et setters
	
}