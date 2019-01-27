package request;

import java.io.IOException;
import java.io.InputStream;
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
		requeteXml();

	}

	public static void requeteXml() throws JAXBException, IOException {
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
	
	public static void requeteJson() throws JsonParseException, JsonMappingException, IOException {
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

}
