package fr.insee.formationapirest.integrationtests.glue;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import io.cucumber.java8.En;

public class SecurityGlue implements En {

    private final String rolePrefix = "ROLE_";
    private ResultActions result;
    private Authentication currentAuthentication;


    public SecurityGlue(MockMvc mockMvc) {

        After(
                () -> {
                    this.currentAuthentication = null;
                    SecurityContextHolder.clearContext();
                });

        Before("@WithRoleAdmin", () -> {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "admin",
                            "N/A",
                            AuthorityUtils.createAuthorityList(rolePrefix + "ADMIN_TOUCAN"));
            this.currentAuthentication = auth;
            SecurityContextHolder.getContext().setAuthentication(auth);
        });

        Before("@WithRoleToto", () -> {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            "admin",
                            "N/A",
                            AuthorityUtils.createAuthorityList(rolePrefix + "TOTO"));
            this.currentAuthentication = auth;
            SecurityContextHolder.getContext().setAuthentication(auth);
        });

        When("je fais une requête HTTP en GET sur l'url {string}", (String url) -> {
            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(url);
            if (this.currentAuthentication != null) {
                requestBuilder.with(authentication(this.currentAuthentication));
            }
            result = mockMvc.perform(requestBuilder);
        });

        Then("j'obtiens un code retour HTTP {int}", (Integer codeReponse) -> {
            result.andExpect(MockMvcResultMatchers.status().is(codeReponse));
        });

    }

}
