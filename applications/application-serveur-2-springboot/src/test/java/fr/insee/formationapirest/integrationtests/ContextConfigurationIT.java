package fr.insee.formationapirest.integrationtests;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcPrint;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import io.cucumber.spring.CucumberContextConfiguration;

@SpringBootTest
// permet de faire des requêtes HTTP avec MockMvc
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@TestPropertySource(properties = {
        "logging.level.fr.insee=${log.level:TRACE}",
        "temperature.base-url=http://localhost:8082",
})
@CucumberContextConfiguration
public class ContextConfigurationIT {
}
