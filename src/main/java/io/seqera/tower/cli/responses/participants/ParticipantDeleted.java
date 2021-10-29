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

package io.seqera.tower.cli.responses.participants;

import io.seqera.tower.cli.responses.Response;

public class ParticipantDeleted extends Response {

    final public String name;
    final public String workspaceName;

    public ParticipantDeleted(String name, String workspaceName) {
        this.name = name;
        this.workspaceName = workspaceName;
    }

    @Override
    public String toString() {
        return ansi(String.format("%n  @|yellow Participant '%s' was removed from '%s' workspace|@%n", name, workspaceName));
    }

}
