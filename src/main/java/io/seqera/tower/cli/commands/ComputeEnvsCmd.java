/*
 * Copyright 2023, Seqera.
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

import io.seqera.tower.cli.commands.computeenvs.AddCmd;
import io.seqera.tower.cli.commands.computeenvs.DeleteCmd;
import io.seqera.tower.cli.commands.computeenvs.ExportCmd;
import io.seqera.tower.cli.commands.computeenvs.ImportCmd;
import io.seqera.tower.cli.commands.computeenvs.ListCmd;
import io.seqera.tower.cli.commands.computeenvs.PrimaryCmd;
import io.seqera.tower.cli.commands.computeenvs.UpdateCmd;
import io.seqera.tower.cli.commands.computeenvs.ViewCmd;
import picocli.CommandLine.Command;


@Command(
        name = "compute-envs",
        description = "Manage workspace compute environments.",
        subcommands = {
                AddCmd.class,
                UpdateCmd.class,
                DeleteCmd.class,
                ViewCmd.class,
                ListCmd.class,
                ExportCmd.class,
                ImportCmd.class,
                PrimaryCmd.class,
        }
)
public class ComputeEnvsCmd extends AbstractRootCmd {
}
