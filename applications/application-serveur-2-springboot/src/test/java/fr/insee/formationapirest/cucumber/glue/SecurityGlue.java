package fr.insee.formationapirest.cucumber.glue;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityGlue {

    private final MockMvc mockMvc;

    private String rolePrefix = "ROLE_";
    private ResultActions result;

    @Before("@WithRoleAdmin")
    public void setupRoleAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "N/A",
                AuthorityUtils.createAuthorityList(rolePrefix + "ADMIN_TOUCAN")));
    }

    @Before("@WithRoleToto")
    public void setupRoleToto() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "N/A",
                AuthorityUtils.createAuthorityList(rolePrefix + "TOTO")));
    }

    @When("je fais une requête HTTP en GET sur l'url {string}")
    public void requeteGet(String url) throws Exception {
        result = mockMvc.perform(MockMvcRequestBuilders.get(url));
    }

    @Then("j'obtiens un code retour HTTP {int}")
    public void testCodeReponse(int codeReponse) throws Exception {
        result.andExpect(MockMvcResultMatchers.status().is(codeReponse));
    }

}
