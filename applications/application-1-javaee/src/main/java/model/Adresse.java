package model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Adresse {
	
	private String nomCommune;
	private String codePostal;
	
	public String getNomCommune() {
		return nomCommune;
	}
	
	public void setNomCommune(String nomCommune) {
		this.nomCommune = nomCommune;
	}
	
	public String getCodePostal() {
		return codePostal;
	}
	
	public void setCodePostal(String codePostal) {
		this.codePostal = codePostal;
	}
	
	
	
}
