package model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Calcul {
	
	private Integer somme;
	private Integer produit;

	public Integer getSomme() {
		return somme;
	}
	public void setSomme(Integer somme) {
		this.somme = somme;
	}
	public Integer getProduit() {
		return produit;
	}
	public void setProduit(Integer produit) {
		this.produit = produit;
	}

}
