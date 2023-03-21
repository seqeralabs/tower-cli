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

package io.seqera.tower.cli.commands.labels;

import io.seqera.tower.cli.responses.Response;

import java.io.PrintWriter;

public class GenericStrResponse extends Response {

    public String msg;

    GenericStrResponse(final String msg) {
        if (!msg.endsWith(System.lineSeparator())) {
            this.msg = msg + System.lineSeparator();
        } else {
            this.msg = msg;
        }
    }

    @Override
    public void toString(PrintWriter out) {
        out.printf(this.msg);
    }

}
