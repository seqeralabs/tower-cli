/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DefaultApi;
import io.seqera.tower.cli.exceptions.CredentialsNotFoundException;
import io.seqera.tower.model.AzBatchConfig;
import io.seqera.tower.model.AzBatchForgeConfig;
import io.seqera.tower.model.ComputeEnv;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.Credentials;
import io.seqera.tower.model.JobCleanupPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AzBatchForgePlatform extends AbstractPlatform<AzBatchConfig> {

    @Option(names = {"-l", "--location"}, description = "The Azure location where the workload will be deployed.", required = true)
    public String location;

    @Option(names = {"--vm-type"}, description = "Specify the virtual machine type used by this pool. It must be a valid Azure Batch VM type [default: Standard_D4_v3].")
    public String vmType;

    @Option(names = {"--vm-count"}, description = "The number of virtual machines in this pool. When autoscaling feature is enabled, this option represents the maximum number of virtual machines to which the pool can grow and automatically scales to zero when unused.", required = true)
    public Integer vmCount;

    @Option(names = {"--no-auto-scale"}, description = "Disable pool autoscaling which automatically adjust the pool size depending the number submitted jobs and scale to zero when the pool is unused.")
    public boolean noAutoScale;

    @Option(names = {"--preserve-resources"}, description = "Enable this if you want to preserve the Batch compute pool created by Tower independently from the lifecycle of this compute environment.")
    public boolean preserveResources;

    @Option(names = {"--registry-credentials"}, split = ",", paramLabel = "<credential_name>", description = "Comma-separated list of container registry credentials name.")
    public List<String> registryCredentials;


    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public AzBatchForgePlatform() {
        super(PlatformEnum.AZURE_BATCH);
    }

    @Override
    public AzBatchConfig computeConfig(Long workspaceId, DefaultApi api) throws ApiException, IOException {
        AzBatchConfig config = new AzBatchConfig();

        config
                // Common
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .region(location);

        if (adv != null) {
            config
                    .deleteJobsOnCompletion(adv.jobsCleanup)
                    .tokenDuration(adv.tokenDuration);
        }


        AzBatchForgeConfig forge = new AzBatchForgeConfig()
                .vmType(vmType)
                .vmCount(vmCount)
                .autoScale(!noAutoScale)
                .disposeOnDeletion(!preserveResources);

        if (registryCredentials != null && registryCredentials.size() > 0) {

            Map<String, String> credentialsNameToId = new HashMap<>();
            List<Credentials> credentials = api.listCredentials(workspaceId, null).getCredentials();
            if (credentials != null) {
                for (Credentials c : credentials) {
                    if (c.getProvider() == Credentials.ProviderEnum.CONTAINER_REG) {
                        credentialsNameToId.put(c.getName(), c.getId());
                    }
                }
            }

            for (String name : registryCredentials) {
                if (credentialsNameToId.containsKey(name)) {
                    forge.addContainerRegIdsItem(credentialsNameToId.get(name));
                } else {
                    throw new CredentialsNotFoundException(name, workspaceId);
                }
            }
        }

        config.forge(forge);

        return config;
    }

    public static class AdvancedOptions {

        @Option(names = {"--jobs-cleanup"}, description = "Enable the automatic deletion of Batch jobs created by the pipeline execution (ON_SUCCESS, ALWAYS, NEVER).")
        public JobCleanupPolicy jobsCleanup;

        @Option(names = {"--token-duration"}, description = "The duration of the shared access signature token created by Nextflow when the 'sasToken' option is not specified [default: 12h].")
        public String tokenDuration;

    }
}
