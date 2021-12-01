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

package io.seqera.tower.cli.commands.datasets;

import picocli.CommandLine;

public class DatasetRefOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public DatasetRef dataset;

    public static class DatasetRef {

        @CommandLine.Option(names = {"-i", "--id"}, description = "Dataset unique id.")
        public String datasetId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Dataset name.")
        public String datasetName;
    }
}
