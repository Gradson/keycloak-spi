package br.com.gradson.user.secret_question;

import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.util.ServerCookie;
import org.keycloak.models.*;
import org.keycloak.models.utils.FormMessage;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.*;
import java.net.URI;

public class SecretQuestionAuthenticator implements Authenticator {

    public static void addCookie(String name, String value, String path, String domain, String comment, int maxAge, boolean secure, boolean httpOnly) {
        HttpResponse response = ResteasyProviderFactory.getContextData(HttpResponse.class);
        StringBuffer cookieBuf = new StringBuffer();
        ServerCookie.appendCookieValue(cookieBuf, 1, name, value, path, domain, comment, maxAge, secure, httpOnly);
        String cookie = cookieBuf.toString();
        response.getOutputHeaders().add(HttpHeaders.SET_COOKIE, cookie);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final Boolean test = isAllowed();

        if (test) {
            context.success();
            return;
        }

        final Response login = context.form().addError(new FormMessage(null, "coloque a mensage que você quiser")).createLogin();
        context.challenge(login);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        boolean validated = validateAnswer(context);
        if (!validated) {
            Response challenge = context.form()
                    .setError("badSecret")
                    .createForm("secret-question.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }
        setCookie(context);
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return session.userCredentialManager().isConfiguredFor(realm, user, SecretQuestionCredentialProvider.SECRET_QUESTION);
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        user.addRequiredAction(SecretQuestionRequiredAction.PROVIDER_ID);
    }

    @Override
    public void close() {

    }

    protected boolean validateAnswer(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String secret = formData.getFirst("secret_answer");
        UserCredentialModel input = new UserCredentialModel();
        input.setType(SecretQuestionCredentialProvider.SECRET_QUESTION);
        input.setValue(secret);
        return context.getSession().userCredentialManager().isValid(context.getRealm(), context.getUser(), input);
    }

    protected boolean hasCookie(AuthenticationFlowContext context) {
        Cookie cookie = context.getHttpRequest().getHttpHeaders().getCookies().get("SECRET_QUESTION_ANSWERED");
        boolean result = cookie != null;
        if (result) {
            System.out.println("Bypassing secret question because cookie is set");
        }
        return result;
    }

    protected void setCookie(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        int maxCookieAge = 60 * 60 * 24 * 30; // 30 days
        if (config != null) {
            maxCookieAge = Integer.valueOf(config.getConfig().get("cookie.max.age"));

        }
        URI uri = context.getUriInfo().getBaseUriBuilder().path("realms").path(context.getRealm().getName()).build();
        addCookie("SECRET_QUESTION_ANSWERED", "true",
                uri.getRawPath(),
                null, null,
                maxCookieAge,
                false, true);
    }

    private Boolean isAllowed() {
        final Client client = ClientBuilder.newClient();
        final Invocation.Builder request = client.target("http://10.3.42.150:8081/identity-service/api/v1/keycloak/test").request(MediaType.APPLICATION_JSON);
        return request.get(Boolean.class);
    }
}
