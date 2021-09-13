package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AzureSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzureProvider extends AbstractProvider<AzureSecurityKeys> {

    @Option(names = {"--batch-key"}, description = "Azure batch account key", required = true)
    public String batchKey;

    @Option(names = {"--batch-name"}, description = "Azure batch account name", required = true)
    public String batchName;

    @Option(names = {"--storage-key"}, description = "Azure blob storage account key", required = true)
    public String storageKey;

    @Option(names = {"--storage-name"}, description = "Azure blob storage account name", required = true)
    public String storageName;

    public AzureProvider() {
        super(ProviderEnum.AZURE);
    }

    @Override
    public AzureSecurityKeys securityKeys() throws IOException {
        return new AzureSecurityKeys()
                .batchKey(batchKey)
                .batchName(batchName)
                .storageKey(storageKey)
                .storageName(storageName);
    }
}
