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

package io.seqera.tower.cli.commands.computeenvs;

import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.AbstractApiCmd;
import io.seqera.tower.model.ComputeEnv;
import picocli.CommandLine.Command;

@Command
public abstract class AbstractComputeEnvCmd extends AbstractApiCmd {

    public AbstractComputeEnvCmd() {
    }
    protected ComputeEnv fetchComputeEnv(ComputeEnvRefOptions computeEnvRefOptions, Long wspId) throws ApiException {
        ComputeEnv computeEnv;

        if (computeEnvRefOptions.computeEnv.computeEnvId != null) {
            computeEnv = computeEnvById(wspId, computeEnvRefOptions.computeEnv.computeEnvId);
        } else {
            computeEnv = computeEnvByName(wspId, computeEnvRefOptions.computeEnv.computeEnvName);
        }

        return computeEnv;
    }
}


