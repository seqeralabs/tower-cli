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

package io.seqera.tower.cli.responses.labels;

import io.seqera.tower.cli.responses.Response;

public class LabelUpdated extends Response {

    public final Long id;

    public final String name;

    public final String value;

    public final String workspaceRef;

    public LabelUpdated(Long id, String name, String value, String workspace) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.workspaceRef = workspace;
    }


    @Override
    public String toString() {
        String workspaceName = this.workspaceRef == null? "": String.format("at '%s' workspace ",workspaceRef);
        String labelFormat = value != null? String.format("%s=%s",name,value): name;
        return ansi(String.format("%n @|yellow Label with id '%s' %supdated to '%s'|@%n",id,workspaceName,labelFormat));
    }
}
