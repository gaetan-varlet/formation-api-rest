package model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Personne {
	
	private String prenom;
	private Integer age;
	private Adresse adresse;
	private List<Ami> listeAmis;
	
	public String getPrenom() {
		return prenom;
	}
	
	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
	
	public Integer getAge() {
		return age;
	}
	
	public void setAge(Integer age) {
		this.age = age;
	}
	
	public Adresse getAdresse() {
		return adresse;
	}
	
	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}
	
	public List<Ami> getListeAmis() {
		return listeAmis;
	}
	
	public void setListeAmis(List<Ami> listeAmis) {
		this.listeAmis = listeAmis;
	}
	
}
