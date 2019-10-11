package br.com.gradson.authorization;

import br.com.gradson.client.CustomApiClient;
import br.com.gradson.client.KeycloakSpiResponse;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CustomAuthenticator implements Authenticator {
    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final KeycloakSpiResponse spiResponse = new CustomApiClient().isAllowed(context.getUser().getUsername());

        if (Response.Status.Family.SUCCESSFUL.equals(spiResponse.getStatusType().getFamily())) {
            context.success();
            return;
        }

        final Response login = context.form()
                .addError(new FormMessage(null, spiResponse.getMessage()))
                .createLogin();

        context.challenge(login);
    }

    @Override
    public void action(AuthenticationFlowContext authenticationFlowContext) {

    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession keycloakSession, RealmModel realmModel, UserModel userModel) {

    }

    @Override
    public void close() {

    }

    private Boolean isAllowed() {
        final Client client = ClientBuilder.newClient();
        final Invocation.Builder request = client.target("http://10.3.42.150:8081/identity-service/api/v1/keycloak/test").request(MediaType.APPLICATION_JSON);
        return request.get(Boolean.class);
    }
}
