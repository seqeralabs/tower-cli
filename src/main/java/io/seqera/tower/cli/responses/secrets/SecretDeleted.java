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

package io.seqera.tower.cli.responses.secrets;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.PipelineSecret;

public class SecretDeleted extends Response {

    public final PipelineSecret secret;
    public final String workspaceRef;

    public SecretDeleted(PipelineSecret secret, String workspaceRef) {
        this.secret = secret;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Secret '%s' deleted at %s workspace|@%n", secret.getName(), workspaceRef));
    }
}
