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

package io.seqera.tower.cli.responses;

public class CredentialsUpdated extends Response {

    public final String provider;
    public final String name;
    public final String workspaceRef;

    public CredentialsUpdated(String provider, String name, String workspaceRef) {
        this.provider = provider;
        this.name = name;
        this.workspaceRef = workspaceRef;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow %S credentials '%s' updated at %s workspace|@%n", provider, name, workspaceRef));
    }
}
