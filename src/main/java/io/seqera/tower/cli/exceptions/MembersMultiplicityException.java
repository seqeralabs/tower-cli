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

public class MembersMultiplicityException extends TowerException {

    public MembersMultiplicityException(String user, Long orgId) {
        super(String.format("Multiple members were found for the '%s' keyword %d organization", user, orgId));
    }
}
