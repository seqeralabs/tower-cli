/*
 * Copyright 2021-2026, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.GkeComputeConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class GkePlatform extends AbstractPlatform<GkeComputeConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored on the shared storage.", required = true)
    public String workDir;

    @Option(names = {"-r", "--region"}, description = "Google Cloud region or zone where the GKE cluster is deployed (e.g., us-central1, europe-west1).", required = true)
    public String region;

    @Option(names = {"--cluster-name"}, description = "Name of the Google GKE (Google Kubernetes Engine) cluster for workflow execution.", required = true)
    public String clusterName;

    @Option(names = {"--namespace"}, description = "Kubernetes namespace for workflow execution. Isolates resources within the cluster.", required = true)
    public String namespace;

    @Option(names = {"--head-account"}, description = "Kubernetes service account for connecting to the cluster. Used by the Nextflow head job to authenticate with the Kubernetes API.", required = true)
    public String headAccount;

    @Option(names = {"--storage-claim"}, description = "PersistentVolumeClaim name for scratch storage. Must support ReadWriteMany access mode for shared workflow data.")
    public String storageClaim;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public K8sPlatform.AdvancedOptions adv;

    public GkePlatform() {
        super(PlatformEnum.GKE_PLATFORM);
    }

    @Override
    public GkeComputeConfig computeConfig() throws ApiException, IOException {
        GkeComputeConfig config = new GkeComputeConfig();
        config
                .region(region)
                .clusterName(clusterName)
                .namespace(namespace)
                .headServiceAccount(headAccount)
                .storageClaimName(storageClaim)

                // Advance
                .storageMountPath(adv().storageMount)
                .computeServiceAccount(adv().computeAccount)
                .podCleanup(adv().podCleanup)
                .headPodSpec(FilesHelper.readString(adv().headPodSpec))
                .servicePodSpec(FilesHelper.readString(adv().servicePodSpec));

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    private K8sPlatform.AdvancedOptions adv() {
        if (adv == null) {
            return new K8sPlatform.AdvancedOptions();
        }
        return adv;
    }
}
