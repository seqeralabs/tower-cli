package io.seqera.tower.cli.responses;

import io.seqera.tower.model.Credentials;

import java.util.List;

public class CredentialsList extends Response {

    private List<Credentials> credentials;

    public CredentialsList(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        for (Credentials cred : credentials) {
            res.append(String.format("- [%s] (%s) %s%s%n", cred.getId(), cred.getProvider(), cred.getName(), formatDescription(cred.getDescription())));
        }
        return res.toString();
    }

    private String formatDescription(String value) {
        if (value == null) {
            return "";
        }
        return String.format(" - %s", value);
    }
}
