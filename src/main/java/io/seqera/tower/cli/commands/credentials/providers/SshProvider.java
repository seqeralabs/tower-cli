package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.SSHSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SshProvider extends AbstractProvider<SSHSecurityKeys> {

    @Option(names = {"-k", "--key"}, description = "SSH private key file", required = true)
    public Path serviceAccountKey;

    @Option(names = {"-p", "--passphrase"}, description = "Passphrase associated with the private key", interactive = true)
    public String passphrase;

    public SshProvider() {
        super(ProviderEnum.SSH);
    }

    @Override
    public SSHSecurityKeys securityKeys() throws IOException {
        return new SSHSecurityKeys()
                .provider(ProviderEnum.SSH.getValue())
                .privateKey(Files.readString(serviceAccountKey))
                .passphrase(passphrase);
    }
}
