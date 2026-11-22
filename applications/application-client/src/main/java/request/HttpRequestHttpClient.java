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

import jakarta.xml.bind.JAXBContext;

import model.Person;
import tools.jackson.databind.ObjectMapper;

public class HttpRequestHttpClient {

	public static void main(String[] args) throws Exception {
		// requeteGetSynchroneXmlJson("/xml");
		requeteGetSynchroneXmlJson("/json");
		// requetePostSynchroneJson();
		// uneRequeteGetAsynchrone();
		// multiplesRequetesGetAsynchrone();
		// requeteGetSynchroneWithBasicAuth();
		// requeteGetSynchroneWithBearerAuth();
	}

	public static void requeteGetSynchroneXmlJson(String path) throws Exception {
		// requête en GET vers /xml ou /json que l'on récupère dans un String
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(URI.create("https://mocktarget.apigee.net" + path)).GET().build();
		HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
		System.out.println(response.statusCode()); // 200
		System.out.println(response.headers().allValues("content-type"));
		// [application/xml; charset=utf-8] ou [application/json; charset=utf-8]
		System.out.println(response.body()); // le Person en XML ou JSON

		// même chose avec récupération du body dans un InputStream
		HttpResponse<InputStream> responseStream = httpClient.send(httpRequest, BodyHandlers.ofInputStream());
		String contentType = responseStream.headers().firstValue("Content-Type").orElse("");
		if (contentType.contains("application/xml")) {
			JAXBContext jc = JAXBContext.newInstance(Person.class);
			Person person = (Person) jc.createUnmarshaller().unmarshal(responseStream.body());
			System.out.println(person); // Person [firstName=John, lastName=Doe, city=San Jose, state=CA]
		} else if (contentType.contains("application/json")) {
			ObjectMapper mapper = new ObjectMapper();
			Person person = mapper.readValue(responseStream.body(), Person.class);
			System.out.println(person); // Person [firstName=John, lastName=Doe, city=San Jose, state=CA]
		} else {
			throw new IllegalStateException("On veut du XML ou du JSON !");
		}
	}

	public static void requetePostSynchroneJson() throws IOException, InterruptedException {
		ObjectMapper mapper = new ObjectMapper();
		Person person = new Person();
		person.setFirstName("Jean");
		person.setLastName("Dupont");
		person.setCity("Paris");
		person.setState("FR");
		String personJson = mapper.writeValueAsString(person);

		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.uri(URI.create("https://jsonplaceholder.typicode.com/users"))
				.POST(BodyPublishers.ofString(personJson)).build();
		HttpResponse<String> response = httpClient.send(httpRequest, BodyHandlers.ofString());
		System.out.println(response.body());
		System.out.println();

		HttpResponse<InputStream> responseStream = httpClient.send(httpRequest, BodyHandlers.ofInputStream());
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(responseStream.body(), Charset.defaultCharset()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}
		}
	}

	public static void uneRequeteGetAsynchrone() {
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		HttpRequest httpRequest = HttpRequest.newBuilder().header("Accept", "application/json")
				.uri(URI.create("https://mocktarget.apigee.net/json")).GET().build();
		System.out.println(LocalDateTime.now()); // t0 : avant l'envoi
		// Non bloquant : sendAsync lance la requête en arrière-plan et rend la main immédiatement
		CompletableFuture<HttpResponse<String>> cfResponse = httpClient.sendAsync(httpRequest, BodyHandlers.ofString());
		System.out.println(LocalDateTime.now()); // t1 ≈ t0 : le thread n'est pas bloqué
		// Bloquant : on attend explicitement que la réponse soit disponible
		HttpResponse<String> response = cfResponse.join();
		System.out.println(LocalDateTime.now()); // t2 : inclut la durée de l'appel réseau
		System.out.println(response.statusCode()); // 200
		System.out.println(response.headers().allValues("content-type"));
		System.out.println(response.body());
	}

	public static void multiplesRequetesGetAsynchrone() {
		HttpClient httpClient = HttpClient.newBuilder()
				.proxy(ProxySelector.of(new InetSocketAddress("proxy-rie.http.insee.fr", 8080))).build();
		List<String> urls = List.of(
				"https://mocktarget.apigee.net/user?user=Alice",
				"https://mocktarget.apigee.net/user?user=Bob",
				"https://mocktarget.apigee.net/user?user=Charlie",
				"https://mocktarget.apigee.net/user?user=David",
				"https://mocktarget.apigee.net/user?user=Eve");
		System.out.println(LocalDateTime.now()); // t0 : avant lancement
		// Non bloquant : les 5 requêtes partent en parallèle quasi instantanément
		List<CompletableFuture<HttpResponse<String>>> cfResponses = urls.stream().map(url -> httpClient.sendAsync(
				HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
				BodyHandlers.ofString())).toList();
		System.out.println(LocalDateTime.now()); // t1 ≈ t0
		// Bloquant : on attend que toutes les requêtes soient terminées
		CompletableFuture<Void> combinedCF = CompletableFuture
				.allOf(cfResponses.toArray(new CompletableFuture[0]));
		combinedCF.join();
		System.out.println(LocalDateTime.now()); // t2 : temps ≈ durée de la requête la plus lente
		for (CompletableFuture<HttpResponse<String>> completableFuture : cfResponses) {
			System.out.println(completableFuture.join().body());
		}
		System.out.println(LocalDateTime.now()); // t3 ≈ t2
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
