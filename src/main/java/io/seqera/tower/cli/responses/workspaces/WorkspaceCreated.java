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

package io.seqera.tower.cli.responses.workspaces;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.Visibility;

public class WorkspaceCreated extends Response {

    public final String workspaceName;
    public final String organizationName;
    public final Visibility visibility;

    public WorkspaceCreated(String workspaceName, String organizationName, Visibility visibility) {
        this.workspaceName = workspaceName;
        this.organizationName = organizationName;
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow A '%s' workspace '%s' created for '%s' organization|@%n", visibility, workspaceName, organizationName));
    }

}
