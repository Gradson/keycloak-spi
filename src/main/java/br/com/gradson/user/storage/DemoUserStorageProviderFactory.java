package br.com.gradson.user.storage;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.io.InputStream;
import java.util.List;

/**
 * @author Niko Köbler, http://www.n-k.de, @dasniko
 */
public class DemoUserStorageProviderFactory implements UserStorageProviderFactory<DemoUserStorageProvider> {

    @Override
    public DemoUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        // here you can setup the user storage provider, initiate some connections, etc.

        System.out.println("Factory ====");
        DemoRepository repository = new DemoRepository();

        return new DemoUserStorageProvider(session, model, repository);
    }

    @Override
    public String getId() {
        return "demo-user-provider";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
                .property("myParam", "My Param", "Some Description", ProviderConfigProperty.STRING_TYPE, "some value", null)
                .build();
    }

    @Override
    public void init(Config.Scope config) {
        InputStream is = getClass().getClassLoader().getResourceAsStream("/users.properties");

        System.out.println(is);
    }

}
