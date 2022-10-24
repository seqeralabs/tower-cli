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

import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.K8sComputeConfig;
import io.seqera.tower.model.PodCleanupPolicy;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

public class K8sPlatform extends AbstractPlatform<K8sComputeConfig> {

    @Option(names = {"-s", "--server"}, description = "Master server.", required = true)
    public String server;

    @Option(names = {"--namespace"}, description = "Namespace.", required = true)
    public String namespace;

    @Option(names = {"--ssl-cert"}, description = "SSL certificate.", required = true)
    public Path sslCert;

    @Option(names = {"--head-account"}, description = "Head service account.", required = true)
    public String headAccount;

    @Option(names = {"--storage-claim"}, description = "Storage claim name.", required = true)
    public String storageClaim;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public AdvancedOptions adv;

    public K8sPlatform() {
        super(PlatformEnum.K8S_PLATFORM);
    }

    @Override
    public K8sComputeConfig computeConfig() throws IOException {
        return new K8sComputeConfig()
                .workDir(workDir)
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString())
                .environment(environmentVariables())
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
    }

    private AdvancedOptions adv() {
        if (adv == null) {
            return new K8sPlatform.AdvancedOptions();
        }
        return adv;
    }

    public static class AdvancedOptions {
        @Option(names = {"--storage-mount"}, description = "Storage mount path.")
        public String storageMount;

        @Option(names = {"--compute-account"}, description = "Compute service account.")
        public String computeAccount;

        @Option(names = "--pod-cleanup", description = "Pod cleanup policy (ON_SUCCESS, ALWAYS, NEVER).")
        public PodCleanupPolicy podCleanup;

        @Option(names = {"--head-pod-spec"}, description = "Custom head pod specs file.")
        public Path headPodSpec;

        @Option(names = {"--service-pod-spec"}, description = "Custom service pod specs file.")
        public Path servicePodSpec;
    }
}
