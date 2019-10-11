package br.com.gradson.client;

import javax.ws.rs.core.Response;

public class KeycloakSpiResponse {

    private String message;
    private Response.StatusType statusType;

    public KeycloakSpiResponse() {
        this.message = "Invalid Credential";
        this.statusType = Response.status(500).build().getStatusInfo();
    }

    public String getMessage() {
        return message;
    }

    public Response.StatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(Response.StatusType statusType) {
        this.statusType = statusType;
    }
}
