/*
 * Copyright 2021-2023, Seqera.
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
 *
 */

package io.seqera.tower.cli.commands.computeenvs.platforms;

import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnvComputeConfig.PlatformEnum;
import io.seqera.tower.model.K8sComputeConfig;
import io.seqera.tower.model.PodCleanupPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public class K8sPlatform extends AbstractPlatform<K8sComputeConfig> {

    @Option(names = {"--work-dir"}, description = "Nextflow work directory. Path where workflow intermediate files are stored on the shared storage.", required = true)
    public String workDir;

    @Option(names = {"-s", "--server"}, description = "Kubernetes control plane URL. The API server endpoint for cluster communication (e.g., https://your-k8s-host.com).", required = true)
    public String server;

    @Option(names = {"--namespace"}, description = "Kubernetes namespace for workflow execution. Isolates resources within the cluster.", required = true)
    public String namespace;

    @Option(names = {"--ssl-cert"}, description = "SSL certificate to authenticate the connection. Provide path to certificate file for secure cluster communication.", required = true)
    public Path sslCert;

    @Option(names = {"--head-account"}, description = "Kubernetes service account for connecting to the cluster. Used by the Nextflow head job to authenticate with the Kubernetes API.", required = true)
    public String headAccount;

    @Option(names = {"--storage-claim"}, description = "PersistentVolumeClaim name for scratch storage. Must support ReadWriteMany access mode for shared workflow data.", required = true)
    public String storageClaim;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public K8sPlatform() {
        super(PlatformEnum.K8S_PLATFORM);
    }

    @Override
    public K8sComputeConfig computeConfig() throws IOException {
        K8sComputeConfig config = new K8sComputeConfig()
                .computeServiceAccount(adv().computeAccount)
                .headPodSpec(FilesHelper.readString(adv().headPodSpec))
                .headServiceAccount(headAccount)
                .namespace(namespace)
                .podCleanup(adv().podCleanup)
                .server(server)
                .servicePodSpec(FilesHelper.readString(adv().servicePodSpec))
                .sslCert(FilesHelper.readString(sslCert))
                .storageClaimName(storageClaim)
                .storageMountPath(adv().storageMount);

        // Common
        config.workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .nextflowConfig(nextflowConfigString())
                .environment(environmentVariables());

        return config;
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new K8sPlatform.AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {
        @Option(names = {"--storage-mount"}, description = "Mount path for the PersistentVolumeClaim. Directory where the storage is mounted in containers. If absent, Platform defaults to /scratch.")
        public String storageMount;

        @Option(names = {"--compute-account"}, description = "Kubernetes service account for Nextflow-submitted pipeline jobs. Controls permissions for individual task pods. Default: default.")
        public String computeAccount;

        @Option(names = "--pod-cleanup", description = "Pod cleanup policy after job completion. ON_SUCCESS removes pods only on success. ALWAYS removes all pods. NEVER keeps all pods.")
        public PodCleanupPolicy podCleanup;

        @Option(names = {"--head-pod-spec"}, description = "Custom PodSpec YAML for the Nextflow head job pod. Provide path to YAML file with custom pod configuration.")
        public Path headPodSpec;

        @Option(names = {"--service-pod-spec"}, description = "Custom PodSpec YAML for the compute environment service pod. Provide path to YAML file with custom pod configuration.")
        public Path servicePodSpec;
    }
}
