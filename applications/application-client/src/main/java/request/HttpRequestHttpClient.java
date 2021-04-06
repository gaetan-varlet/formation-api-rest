package request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.User;

public class HttpRequestHttpClient {

	public static void main(String[] args) throws Exception {
		// requeteGetSynchroneXmlJson("application/xml");
		requeteGetSynchroneXmlJson("application/json");
		// requetePostSynchroneJson();
		// uneRequeteGetAsynchrone();
		// multiplesRequetesGetAsynchrone();
		// requeteGetSynchroneWithBasicAuth();
		// requeteGetSynchroneWithBearerAuth();
	}

	public static void requeteGetSynchroneXmlJson(String accept) throws Exception {
		// requête en GET avec réponse en XML ou JSON que l'on récupère dans un String
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", accept)
				.uri(URI.create("http://fakerestapi.azurewebsites.net/api/v1/Users/1")).GET().build();
		HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
		System.out.println(response.statusCode()); // 200
		System.out.println(response.headers().allValues("content-type"));
		// [application/xml; charset=utf-8] ou application/json; charset=utf-8]
		System.out.println(response.body()); // le User en XML ou JSON
		// même chose avec récupération du body dans un InputStream
		HttpResponse<InputStream> responseStream = httpClient.send(httpRequest, BodyHandlers.ofInputStream());
		String contentType = responseStream.headers().firstValue("Content-Type").get();
		if (contentType.contains("application/xml")) {
			JAXBContext jc = JAXBContext.newInstance(User.class);
			User user = (User) jc.createUnmarshaller().unmarshal(responseStream.body());
			System.out.println(user); // User [id=1, userName=User 1, password=Password1]
		} else if (contentType.contains("application/json")) {
			ObjectMapper mapper = new ObjectMapper();
			User user = mapper.readValue(responseStream.body(), User.class);
			System.out.println(user); // User [id=1, userName=User 1, password=Password1]
		} else {
			throw new Exception("On veut du XML ou du JSON !");
		}
	}

	public static void requetePostSynchroneJson()
			throws JsonParseException, JsonMappingException, IOException, InterruptedException {
		ObjectMapper mapper = new ObjectMapper();
		User user = new User();
		user.setUserName("toto");
		user.setPassword("azerty");
		String userString = mapper.writeValueAsString(user);

		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json")
				.header("Content-type", "application/json")
				.uri(URI.create("http://fakerestapi.azurewebsites.net/api/v1/Users"))
				.POST(BodyPublishers.ofString(userString)).build();
		HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
		System.out.println(response.body());
		System.out.println("");

		HttpResponse<InputStream> responseStream = httpClient.send(httpRequest, BodyHandlers.ofInputStream());
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(responseStream.body(), Charset.defaultCharset()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
	}

	public static void uneRequeteGetAsynchrone() throws Exception {
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json")
				.uri(URI.create("http://fakerestapi.azurewebsites.net/api/Users/1")).GET().build();
		System.out.println(LocalDateTime.now()); // 15:44:19.690822
		CompletableFuture<HttpResponse<String>> cfResponse = httpClient.sendAsync(httpRequest, BodyHandlers.ofString());
		System.out.println(LocalDateTime.now()); // 15:44:19.717822
		HttpResponse<String> response = cfResponse.join();
		System.out.println(LocalDateTime.now()); // 15:44:19.876822
		System.out.println(response.statusCode()); // 200
		System.out.println(response.headers().allValues("content-type"));
		// [application/xml; charset=utf-8] ou [application/json; charset=utf-8]
		System.out.println(response.body()); // le User en XML ou JSON
	}

	public static void multiplesRequetesGetAsynchrone() throws Exception {
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		List<String> urls = List.of("http://fakerestapi.azurewebsites.net/api/Users/1",
				"http://fakerestapi.azurewebsites.net/api/Users/2", "http://fakerestapi.azurewebsites.net/api/Users/3",
				"http://fakerestapi.azurewebsites.net/api/Users/4", "http://fakerestapi.azurewebsites.net/api/Users/5");
		System.out.println(LocalDateTime.now()); // 16:03:28.104822
		List<CompletableFuture<HttpResponse<String>>> cfResponses = urls.stream().map(url -> httpClient.sendAsync(
				HttpRequest.newBuilder().header("Accept", "application/json").uri(URI.create(url)).GET().build(),
				BodyHandlers.ofString())).collect(Collectors.toList());
		System.out.println(LocalDateTime.now()); // 16:03:28.137822
		// on attend d'avoir toutes les réponses
		CompletableFuture<Void> conbinedCF = CompletableFuture
				.allOf(cfResponses.toArray(new CompletableFuture[cfResponses.size()]));
		conbinedCF.join();
		System.out.println(LocalDateTime.now()); // 16:03:28.319822
		for (CompletableFuture<HttpResponse<String>> completableFuture : cfResponses) {
			System.out.println(completableFuture.join().body());
		}
		System.out.println(LocalDateTime.now()); // 16:03:28.319822
	}

	public static void requeteGetSynchroneWithBasicAuth() throws Exception {
		String idMdp = "ID" + ":" + "MDP";
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json")
				.header("Authorization", "Basic " + Base64.getEncoder().encodeToString(idMdp.getBytes()))
				.uri(URI.create("URL_API")).GET().build();
		HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
		System.out.println(response.statusCode());
		System.out.println(response.headers().allValues("content-type"));
		System.out.println(response.body());
	}

	public static void requeteGetSynchroneWithBearerAuth() throws Exception {
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json")
				.header("Authorization", "Bearer " + "CONTENU_BEARER").uri(URI.create("URL_API")).GET().build();
		HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
		System.out.println(response.statusCode());
		System.out.println(response.headers().allValues("content-type"));
		System.out.println(response.body());
	}

}
