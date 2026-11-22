package request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import model.Person;
import tools.jackson.databind.ObjectMapper;

public class HttpRequestJava {

	public static void main(String[] args) throws Exception {
		// requeteGetXml();
		// requeteGetJson();
		requetePostJson();
	}

	public static void requeteGetXml() throws JAXBException, IOException {
		// System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		// System.setProperty("http.proxyPort", "8080");
		URL url = URI.create("https://mocktarget.apigee.net/xml").toURL();
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		InputStream response = connection.getInputStream();
		JAXBContext jc = JAXBContext.newInstance(Person.class);
		Person person = (Person) jc.createUnmarshaller().unmarshal(response);
		System.out.println(person);
		System.out.println(connection.getResponseCode()); // 200
		System.out.println(connection.getContentType()); // application/xml; charset=utf-8

		connection.disconnect();
	}

	public static void requeteGetJson() throws IOException {
		// System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		// System.setProperty("http.proxyPort", "8080");
		URL url = URI.create("https://mocktarget.apigee.net/json").toURL();
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		InputStream response = connection.getInputStream();
		ObjectMapper mapper = new ObjectMapper();
		Person person = mapper.readValue(response, Person.class);
		connection.disconnect();
		System.out.println(person);
	}

	public static void requetePostJson() throws IOException {
		// System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		// System.setProperty("http.proxyPort", "8080");

		Person person = new Person();
		person.setFirstName("Jean");
		person.setLastName("Dupont");
		person.setCity("Paris");
		person.setState("FR");
		ObjectMapper mapper = new ObjectMapper();
		URL url = URI.create("https://jsonplaceholder.typicode.com/users").toURL();
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/json");
		connection.setDoOutput(true);
		mapper.writeValue(connection.getOutputStream(), person);

		InputStream response = connection.getInputStream();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(response, Charset.defaultCharset()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		}
		connection.disconnect();
	}

}
