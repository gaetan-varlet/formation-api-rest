package fr.insee.formationapirest.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFilter;

@Entity
@Table(name = "vin")
@XmlRootElement
@JsonFilter("monFiltreDynamique")
public class Vin {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_vin")
	@SequenceGenerator(name = "seq_vin", sequenceName = "formation.vin_id_seq", allocationSize = 1)
	private Integer id;

	private String chateau;
	private String appellation;
	private Double prix;

	public Vin() {
	}

	public Vin(Integer id, String chateau, String appellation, Double prix) {
		this.id = id;
		this.chateau = chateau;
		this.appellation = appellation;
		this.prix = prix;
	}

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

	@Override
	public String toString() {
		return "Vin [id=" + id + ", chateau=" + chateau + ", appellation=" + appellation + ", prix=" + prix + "]";
	}

}