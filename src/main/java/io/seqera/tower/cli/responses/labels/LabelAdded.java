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

public class LabelAdded extends Response {


    public final Long id;
    public final String name;
    public final boolean isResource;
    public final String value;

    public final String workspacRef;

    public LabelAdded(Long id, String name, Boolean isResource, String value, String workspacRef) {
        this.id = id;
        this.name = name;
        this.isResource = isResource != null && isResource;
        this.value = value;
        this.workspacRef = workspacRef;
    }

    @Override
    public String toString() {
        String workspaceName = this.workspacRef == null? "" : String.format("at '%s' workspace ",workspacRef);
        String labelFormat = this.isResource? String.format("%s=%s",this.name,this.value) : this.name;
        return ansi(String.format("%n @|yellow Label '%s' added %swith id '%s'|@%n", labelFormat, workspaceName, this.id));
    }
}
