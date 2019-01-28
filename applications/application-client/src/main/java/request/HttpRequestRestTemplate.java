package request;

import java.io.IOException;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.User;

public class HttpRequestRestTemplate {

	public static void main(String[] args) throws Exception {
//		requeteGet();
		requetePost();
	}

	public static void requeteGet() throws JsonParseException, JsonMappingException, IOException{
		String url = "http://fakerestapi.azurewebsites.net/api/Users/1";

		System.out.println("Demande de la réponse sous forme de String");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		System.out.println(response.getStatusCode()); // 200 OK
		System.out.println(response.getBody()); // {"ID":1,"UserName":"User 1","Password":"Password1"}
		System.out.println(response.getHeaders().getContentType()); // application/json;charset=utf-8
		
		System.out.println("Demande de la réponse sous forme de String au format XML"); // NE RENVOIE PAS DU XML !!!
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_XML);
		headers.set("my_other_key", "my_other_value");
		HttpEntity<String> entity = new HttpEntity<String>(headers);
		restTemplate = new RestTemplate();
		response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		System.out.println(response.getStatusCode());
		System.out.println(response.getBody());
		System.out.println(response.getHeaders().getContentType());
		
		System.out.println("Convertion du body en objet");
		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(response.getBody(), User.class);
		System.out.println(user);

		System.out.println("Demande de la réponse sous forme d'objet User");
		restTemplate = new RestTemplate();
		user = restTemplate.getForObject(url, User.class);
		System.out.println(user);
	}
	
	public static void requetePost() {
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
