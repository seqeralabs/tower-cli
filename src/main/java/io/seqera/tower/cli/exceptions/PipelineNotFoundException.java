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

package io.seqera.tower.cli.exceptions;

public class PipelineNotFoundException extends TowerException {

    public PipelineNotFoundException(String pipelineName, String workspaceRef) {
        super(String.format("Unknown pipeline '%s' at %s workspace", pipelineName, workspaceRef));
    }

    public PipelineNotFoundException(Long pipelineId, String workspaceRef) {
        super(String.format("Unknown pipeline '%s' at %s workspace", pipelineId, workspaceRef));
    }
}
