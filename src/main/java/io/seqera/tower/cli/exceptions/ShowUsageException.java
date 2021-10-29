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

import picocli.CommandLine;

public class ShowUsageException extends TowerException {

    private CommandLine.Model.CommandSpec specs;

    public ShowUsageException(CommandLine.Model.CommandSpec specs){
        this.specs = specs;
    }

    public ShowUsageException(CommandLine.Model.CommandSpec specs,String message){
        super(message);
        this.specs = specs;
    }

    public CommandLine.Model.CommandSpec getSpecs() {
        return specs;
    }
}
