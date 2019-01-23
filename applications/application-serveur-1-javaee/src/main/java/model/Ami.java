package model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Ami {
	
	private String nom;
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}
	
}
