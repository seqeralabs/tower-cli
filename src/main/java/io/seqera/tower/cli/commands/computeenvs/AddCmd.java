/*
 * Copyright 2021-2023, Seqera.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.seqera.tower.cli.commands.computeenvs;


import io.seqera.tower.ApiException;
import io.seqera.tower.cli.commands.computeenvs.add.AddAltairCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddAwsCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddAzureCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddEksCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddGkeCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddGoogleBatchCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddGoogleLifeSciencesCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddK8sCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddLsfCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddMoabCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddSeqeraComputeCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddSlurmCmd;
import io.seqera.tower.cli.commands.computeenvs.add.AddUgeCmd;
import io.seqera.tower.cli.exceptions.ShowUsageException;
import io.seqera.tower.cli.responses.Response;
import picocli.CommandLine.Command;

import java.io.IOException;

@Command(
        name = "add",
        description = "Add a new compute environment.",
        subcommands = {
                AddK8sCmd.class,
                AddAwsCmd.class,
                AddEksCmd.class,
                AddSlurmCmd.class,
                AddLsfCmd.class,
                AddUgeCmd.class,
                AddAltairCmd.class,
                AddMoabCmd.class,
                AddGkeCmd.class,
                AddGoogleLifeSciencesCmd.class,
                AddGoogleBatchCmd.class,
                AddAzureCmd.class,
                AddSeqeraComputeCmd.class
        }
)
public class AddCmd extends AbstractComputeEnvCmd {
    @Override
    protected Response exec() throws ApiException, IOException {
        throw new ShowUsageException(getSpec(), "Missing Required Subcommand");
    }
}
