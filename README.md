## Why

> _Why does this exist?_

This is a **SPI** for override operations in Keycloak. 


## What

> _What is the structure?_

This is a **Java** project that uses [Keycloak SPI](https://www.keycloak.org/docs/latest/server_development/index.html#_user-storage-spi) 
to override class (behavior)

## Get Started

#### Steps

```
mvn clean install
```

- copy jar from target to <host>/opt/jboss/keycloak/standalone/deployments

    
 - up jboss again. **Enjoy :)**
