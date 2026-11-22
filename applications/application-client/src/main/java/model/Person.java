package model;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "root")
public class Person {

	private String firstName;
	private String lastName;
	private String city;
	private String state;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public String toString() {
		return "Person [firstName=" + firstName + ", lastName=" + lastName + ", city=" + city + ", state=" + state + "]";
	}

}
