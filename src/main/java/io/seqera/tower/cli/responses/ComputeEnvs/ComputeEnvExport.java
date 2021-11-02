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

package io.seqera.tower.cli.responses.ComputeEnvs;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.seqera.tower.JSON;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchForgePlatform;
import io.seqera.tower.cli.commands.computeenvs.platforms.AwsBatchManualPlatform;
import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.cli.utils.FilesHelper;
import io.seqera.tower.model.AwsBatchConfig;
import io.seqera.tower.model.ComputeConfig;

public class ComputeEnvExport extends Response {

    public final ComputeConfig computeConfig;
    public final String fileName;

    public ComputeEnvExport(ComputeConfig computeConfig, String fileName) {
        this.computeConfig = computeConfig;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        String configOutput = "";

        // Remove forged resources
        if (computeConfig instanceof AwsBatchConfig) {
            AwsBatchConfig awsCfg = (AwsBatchConfig) computeConfig;
            if (awsCfg.getForge() != null) {
                AwsBatchForgePlatform.clean(awsCfg);
            } else {
                AwsBatchManualPlatform.clean(awsCfg);
            }
        }

        try {
            configOutput = new JSON().getContext(ComputeConfig.class).writerWithDefaultPrettyPrinter().writeValueAsString(computeConfig);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (fileName != null && !fileName.equals("-")) {
            FilesHelper.saveString(fileName, configOutput);

            return ansi(String.format("%n  @|yellow Compute environment exported into '%s' |@%n", fileName));
        }

        return configOutput;
    }
}
