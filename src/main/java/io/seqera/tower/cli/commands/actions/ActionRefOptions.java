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

package io.seqera.tower.cli.commands.actions;

import picocli.CommandLine;

public class ActionRefOptions {

    @CommandLine.ArgGroup
    public ActionRef action;

    public static class ActionRef {

        @CommandLine.Option(names = {"-i", "--id"}, description = "Action unique id.")
        public String actionId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Action name.")
        public String actionName;
    }
}
