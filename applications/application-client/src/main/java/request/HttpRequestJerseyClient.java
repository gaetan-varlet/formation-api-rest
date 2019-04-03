package request;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.databind.ObjectMapper;

import model.User;

public class HttpRequestJerseyClient {

	public static void main(String[] args) throws Exception {
		requeteGetXml();
	}

	public static void requeteGetXml() throws JAXBException, IOException, NoSuchAlgorithmException {
		ClientConfig config = new ClientConfig();
		config.connectorProvider(new ApacheConnectorProvider());
		config.property(ClientProperties.PROXY_URI, "http://proxy-rie.http.insee.fr:8080");


		//SslConfigurator sslConfig = SslConfigurator.newInstance().keyStoreType("PKCS12").trustStoreFile("adresse d'un fichier");
		//SSLContext sslContext = sslConfig.createSSLContext();

		Client client = ClientBuilder.newBuilder()
				.withConfig(config)
				//.sslContext(sslContext)
				.build();
		WebTarget webTarget = client.target("http://fakerestapi.azurewebsites.net/api");
		webTarget = webTarget.path("Users").path("1");
		Response response = webTarget.request(MediaType.APPLICATION_JSON).get();
		InputStream inputStream = response.readEntity(InputStream.class);
		ObjectMapper mapper = new ObjectMapper();
		User user = mapper.readValue(inputStream, User.class);
		
		System.out.println(user);
		
		
	/*	System.setProperty("http.proxyHost", "proxy-rie.http.insee.fr:8080");
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
		
		connection.disconnect();*/
	}

}
