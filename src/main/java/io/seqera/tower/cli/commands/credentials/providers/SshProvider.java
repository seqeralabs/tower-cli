package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SshProvider extends AbstractProvider {

    @Option(names = {"-k", "--key"}, description = "SSH private key file", required = true)
    public Path serviceAccountKey;

    @Option(names = {"-p", "--passphrase"}, description = "Passphrase associated with the private key", interactive = true)
    public String passphrase;

    public SshProvider() {
        super(ProviderEnum.SSH);
    }

    @Override
    public SecurityKeys securityKeys() throws IOException {
        return new SecurityKeys()
                .privateKey(Files.readString(serviceAccountKey))
                .passphrase(passphrase);
    }
}
