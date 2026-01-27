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

package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.runs.CancelCmd;
import io.seqera.tower.cli.commands.runs.DeleteCmd;
import io.seqera.tower.cli.commands.runs.LabelsCmd;
import io.seqera.tower.cli.commands.runs.DumpCmd;
import io.seqera.tower.cli.commands.runs.ListCmd;
import io.seqera.tower.cli.commands.runs.RelaunchCmd;
import io.seqera.tower.cli.commands.runs.ViewCmd;
import picocli.CommandLine;

@CommandLine.Command(
        name = "runs",
        description = "Manage pipeline runs",
        subcommands = {
                ViewCmd.class,
                ListCmd.class,
                RelaunchCmd.class,
                CancelCmd.class,
                LabelsCmd.class,
                DeleteCmd.class,
                DumpCmd.class
        }
)
public class RunsCmd extends AbstractRootCmd {
}
