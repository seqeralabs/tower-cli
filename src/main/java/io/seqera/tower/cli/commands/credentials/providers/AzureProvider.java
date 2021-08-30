package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.AzureSecurityKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AzureProvider extends AbstractProvider<AzureSecurityKeys> {

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
    public AzureSecurityKeys securityKeys() throws IOException {
        return new AzureSecurityKeys()
                .provider(ProviderEnum.AZURE.getValue())
                .batchKey(batchKey)
                .batchName(batchName)
                .storageKey(storageKey)
                .storageName(storageName);
    }
}
