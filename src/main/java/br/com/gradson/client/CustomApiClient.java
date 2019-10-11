package br.com.gradson.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CustomApiClient {

    private String PROXY_USER = "";
    private String PROXY_PASS = "";
    private String PROXY_HOST = "";
    private Integer PROXY_PORT;
    private String target;

    public CustomApiClient() {
        this.target = System.getenv("target");
        this.PROXY_HOST = System.getenv("PROXY_HOST");
        this.PROXY_USER = System.getenv("PROXY_USER");
        this.PROXY_PASS = System.getenv("PROXY_PASS");
        this.target = "https://authorizer.getsandbox.com";

        String proxy_port = System.getenv("PROXY_PORT");
        if (proxy_port != null) {
            this.PROXY_PORT = Integer.valueOf(proxy_port).intValue();
        }
    }

    public KeycloakSpiResponse isAllowed(final String usernameOrEmail) {
        try {
            final Response response = getClient()
                    .target(target)
                    .path("user").path(usernameOrEmail).path("validate")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            final KeycloakSpiResponse keycloakSpiResponse = response.readEntity(KeycloakSpiResponse.class);
            keycloakSpiResponse.setStatusType(response.getStatusInfo());

            return keycloakSpiResponse;

        } catch (Exception e) {
            return new KeycloakSpiResponse();
        }
    }

    private ResteasyClient getClient() {
        return new ResteasyClientBuilder().httpEngine(getHttpClient()).build();
    }

    private ApacheHttpClient4Engine getHttpClient() {
        if (PROXY_PORT != null && PROXY_HOST != null) {
            new ApacheHttpClient4Engine();
        }
        Credentials credentials = new UsernamePasswordCredentials(PROXY_USER, PROXY_PASS);
        CredentialsProvider credProvider = new BasicCredentialsProvider();
        credProvider.setCredentials(new AuthScope(PROXY_HOST, PROXY_PORT), credentials);

        HttpClient httpClient = HttpClientBuilder.create()
                .setProxy(new HttpHost(PROXY_HOST, PROXY_PORT))
                .setDefaultCredentialsProvider(credProvider)
                .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                .build();

        return new ApacheHttpClient4Engine(httpClient);
    }
}
