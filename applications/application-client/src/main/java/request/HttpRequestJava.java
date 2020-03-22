package request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.User;

public class HttpRequestJava {

	public static void main(String[] args) throws Exception {
//		requeteGetXml();
//		requeteGetJson();
		requetePostJson();
	}

	public static void requeteGetXml() throws JAXBException, IOException {
		System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		System.setProperty("http.proxyPort", "8080");
		// requête en GET avec réponse en XML
		URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users/1");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");
		InputStream response = connection.getInputStream();
		JAXBContext jc = JAXBContext.newInstance(User.class);
		User user = (User) jc.createUnmarshaller().unmarshal(response);
		System.out.println(user);
		System.out.println(connection.getResponseCode()); // 200
		System.out.println(connection.getContentType()); // application/xml; charset=utf-8
		
		connection.disconnect();
	}
	
	public static void requeteGetJson() throws JsonParseException, JsonMappingException, IOException {
		System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		System.setProperty("http.proxyPort", "8080");
		// requête en GET avec réponse en JSON
		URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users/1");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/json");
		InputStream response = connection.getInputStream();
		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(response, User.class);
		connection.disconnect();
		System.out.println(user);
	}
	
	public static void requetePostJson() throws JsonParseException, JsonMappingException, IOException {
		System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		System.setProperty("http.proxyPort", "8080");
		
		User user = new User(); user.setUserName("toto"); user.setPassword("azerty");
		ObjectMapper mapper = new ObjectMapper();
		URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-type", "application/json");
		connection.setDoOutput(true); //this is to enable writing
		mapper.writeValue(connection.getOutputStream(), user);
		
		InputStream response = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(response,Charset.defaultCharset()));
		String line = null;
		while ((line=reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
		connection.disconnect();
	}

}
