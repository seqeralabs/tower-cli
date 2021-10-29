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
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.ComputeEnv.PlatformEnum;
import io.seqera.tower.model.EksComputeConfig;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.IOException;

public class EksPlatform extends AbstractPlatform<EksComputeConfig> {

    @Option(names = {"-r", "--region"}, description = "AWS region", required = true)
    public String region;

    @Option(names = {"--cluster-name"}, description = "The AWS EKS cluster name", required = true)
    public String clusterName;

    @Option(names = {"--namespace"}, description = "Namespace", required = true)
    public String namespace;

    @Option(names = {"--head-account"}, description = "Head service account", required = true)
    public String headAccount;

    @Option(names = {"--storage-claim"}, description = "Storage claim name")
    public String storageClaim;

    @ArgGroup(heading = "%nAdvanced options:%n", validate = false)
    public K8sPlatform.AdvancedOptions adv;

    public EksPlatform() {
        super(PlatformEnum.EKS_PLATFORM);
    }

    @Override
    public EksComputeConfig computeConfig() throws ApiException, IOException {
        EksComputeConfig config = new EksComputeConfig();
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
                .servicePodSpec(FilesHelper.readString(adv().servicePodSpec))

                // Common
                .workDir(workDir)

                // Stagging
                .preRunScript(preRunScriptString())
                .postRunScript(postRunScriptString());

        return config;
    }

    private K8sPlatform.AdvancedOptions adv() {
        if (adv == null) {
            return new K8sPlatform.AdvancedOptions();
        }
        return adv;
    }
}
