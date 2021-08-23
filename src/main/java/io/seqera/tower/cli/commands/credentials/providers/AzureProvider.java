package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.CredentialsSpec.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AzureProvider extends AbstractProvider {

    @Option(names = {"--batch-key"}, description = "Azure batch account key")
    public String batchKey;

    @Option(names = {"--batch-name"}, description = "Azure batch account name")
    public String batchName;

    @Option(names = {"--storage-key"}, description = "Azure blob storage account key")
    public String storageKey;

    @Option(names = {"--storage-name"}, description = "Azure blob storage account name")
    public String storageName;

    public AzureProvider() {
        super(ProviderEnum.AZURE);
    }

    @Override
    public SecurityKeys securityKeys() throws IOException {
        return new SecurityKeys()
                .batchKey(batchKey)
                .batchName(batchName)
                .storageKey(storageKey)
                .storageName(storageName);
    }
}
