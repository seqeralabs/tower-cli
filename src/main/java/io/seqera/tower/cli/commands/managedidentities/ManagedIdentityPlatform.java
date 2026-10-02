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


package io.seqera.tower.cli.commands.managedidentities;

import io.seqera.tower.model.AbstractGridConfig;
import io.seqera.tower.model.AltairPbsComputeConfig;
import io.seqera.tower.model.LsfComputeConfig;
import io.seqera.tower.model.MoabComputeConfig;
import io.seqera.tower.model.SlurmComputeConfig;
import io.seqera.tower.model.UnivaComputeConfig;

import java.util.function.Supplier;

/**
 * HPC schedulers a managed identity can target, named like the 'tw compute-envs add' subcommands.
 */
public enum ManagedIdentityPlatform {
    altair("altair-platform", AltairPbsComputeConfig::new),
    lsf("lsf-platform", LsfComputeConfig::new),
    moab("moab-platform", MoabComputeConfig::new),
    slurm("slurm-platform", SlurmComputeConfig::new),
    uge("uge-platform", UnivaComputeConfig::new);

    public final String value;
    private final Supplier<AbstractGridConfig> configFactory;

    ManagedIdentityPlatform(String value, Supplier<AbstractGridConfig> configFactory) {
        this.value = value;
        this.configFactory = configFactory;
    }

    /**
     * Platform reads the config subtype from its discriminator, so it must be the platform's own config class.
     */
    public AbstractGridConfig config(String hostName, Integer port) {
        AbstractGridConfig config = configFactory.get();
        config.setHostName(hostName);
        config.setPort(port);
        return config;
    }

    public static ManagedIdentityPlatform fromValue(String value) {
        for (ManagedIdentityPlatform platform : values()) {
            if (platform.value.equals(value)) {
                return platform;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported managed identity platform '%s'", value));
    }
}
