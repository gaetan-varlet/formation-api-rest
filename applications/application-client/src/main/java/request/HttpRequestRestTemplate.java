package request;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.User;

public class HttpRequestRestTemplate {

	public static void main(String[] args) throws Exception {
//		requeteGet();
		requetePost();
	}

	public static void requeteGet() throws Exception{
		System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		System.setProperty("http.proxyPort", "8080");
		String url = "http://fakerestapi.azurewebsites.net/api/Users/1";

		System.out.println("Demande de la réponse sous forme de String (que l'on récupère au format JSON)");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		System.out.println(response.getStatusCode()); // 200 OK
		System.out.println(response.getBody()); // {"ID":1,"UserName":"User 1","Password":"Password1"}
		System.out.println(response.getHeaders().getContentType()); // application/json;charset=utf-8
		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(response.getBody(), User.class);
		System.out.println(user);
		System.out.println();
		
		
		System.out.println("Demande de la réponse sous forme de String au format XML");
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/xml");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		restTemplate = new RestTemplate();
		response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		JAXBContext jc = JAXBContext.newInstance(User.class);
		User userXml = (User) jc.createUnmarshaller().unmarshal(new StringReader(response.getBody()));
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		System.out.println(response.getHeaders().getContentType());
		System.out.println(userXml);
		System.out.println();


		System.out.println("Demande de la réponse sous forme d'objet User");
		restTemplate = new RestTemplate();
		user = restTemplate.getForObject(url, User.class);
		System.out.println(user);
	}
	
	public static void requetePost() {
		System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr");
		System.setProperty("http.proxyPort", "8080");
		String url = "http://fakerestapi.azurewebsites.net/api/Users";
		
		User user = new User();
		user.setUserName("toto");
		user.setPassword("azerty");
		System.out.println(user);
		
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<User> request = new HttpEntity<>(user);
		User userResponse = restTemplate.postForObject(url, request, User.class);
		System.out.println(userResponse);
	}

}
