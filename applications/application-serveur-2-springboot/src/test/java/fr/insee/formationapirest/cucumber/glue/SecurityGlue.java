package fr.insee.formationapirest.cucumber.glue;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import io.cucumber.java8.En;

public class SecurityGlue implements En {

    private String rolePrefix = "ROLE_";
    private ResultActions result;

    public SecurityGlue(MockMvc mockMvc) {

        Before("@WithRoleAdmin", () -> {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "N/A",
                    AuthorityUtils.createAuthorityList(rolePrefix + "ADMIN_TOUCAN")));
        });

        Before("@WithRoleToto", () -> {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "N/A",
                    AuthorityUtils.createAuthorityList(rolePrefix + "TOTO")));
        });

        When("je fais une requête HTTP en GET sur l'url {string}", (String url) -> {
            result = mockMvc.perform(MockMvcRequestBuilders.get(url));
        });

        Then("j'obtiens un code retour HTTP {int}", (Integer codeReponse) -> {
            result.andExpect(MockMvcResultMatchers.status().is(codeReponse));
        });

    }

}
