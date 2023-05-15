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

import picocli.CommandLine;

import java.util.List;

public class LabelsOptionalOptions {

    @CommandLine.Option(names = {"--labels"}, split = ",", description = "List of labels seperated by coma.", converter = Label.LabelConverter.class)
    public List<Label> labels = null;

}
