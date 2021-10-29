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

public class ActionNotFoundException extends TowerException {

    public ActionNotFoundException(String workspaceName) {
        super(String.format("No actions found for workspace '%s'", workspaceName));
    }

    public ActionNotFoundException(String actionName, String workspaceName) {
        super(String.format("No action '%s' found for workspace '%s'", actionName, workspaceName));
    }
}
