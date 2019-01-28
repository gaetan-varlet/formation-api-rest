package request;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
		// requête en GET avec réponse en XML
		URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users/1");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");
		InputStream response = connection.getInputStream();
		JAXBContext jc = JAXBContext.newInstance(User.class);
		User user = (User) jc.createUnmarshaller().unmarshal(response);
		connection.disconnect();
		System.out.println(user);
	}
	
	public static void requeteGetJson() throws JsonParseException, JsonMappingException, IOException {
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
		User user = new User();
		user.setUserName("toto");
		user.setPassword("azerty");
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = mapper.writeValueAsString(user);
		System.out.println(jsonInString);
		
		URL url = new URL("http://fakerestapi.azurewebsites.net/api/Users");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept", "application/json");
		connection.setDoOutput(true); //this is to enable writing
	    OutputStream out = new ObjectOutputStream(connection.getOutputStream());
	    out.write(jsonInString.getBytes());
	    out.close();
	    
		InputStream response = connection.getInputStream();
		connection.disconnect();
		System.out.println(response);
	}

}
