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

package io.seqera.tower.cli.responses.computeenvs;

import java.util.HashMap;
import java.util.Map;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.ComputeEnv;

public class ComputeEnvsPrimaryGet extends Response {

    public final String workspaceRef;
    public final ComputeEnv computeEnv;

    public ComputeEnvsPrimaryGet(String workspaceRef, ComputeEnv computeEnv) {
        this.workspaceRef = workspaceRef;
        this.computeEnv = computeEnv;
    }

    @Override
    public Object getJSON() {
        Map<String, String> map = new HashMap<>();

        map.put("id", computeEnv.getId());
        map.put("name", computeEnv.getName());
        map.put("platform", computeEnv.getPlatform().getValue());

        return map;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Primary compute environment for workspace '%s' is '%s (%s)'  |@%n", workspaceRef, computeEnv.getName(), computeEnv.getId()));
    }
}
