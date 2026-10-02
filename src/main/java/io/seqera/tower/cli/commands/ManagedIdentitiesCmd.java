/*
 * Copyright 2021-2026, Seqera.
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
 */


package io.seqera.tower.cli.commands;

import io.seqera.tower.cli.commands.managedidentities.AddCmd;
import io.seqera.tower.cli.commands.managedidentities.CredentialsCmd;
import io.seqera.tower.cli.commands.managedidentities.DeleteCmd;
import io.seqera.tower.cli.commands.managedidentities.ListCmd;
import io.seqera.tower.cli.commands.managedidentities.UpdateCmd;
import io.seqera.tower.cli.commands.managedidentities.ViewCmd;
import picocli.CommandLine.Command;

@Command(
        name = "managed-identities",
        description = "Manage organization managed identities for HPC clusters",
        subcommands = {
                ListCmd.class,
                AddCmd.class,
                ViewCmd.class,
                UpdateCmd.class,
                DeleteCmd.class,
                CredentialsCmd.class,
        }
)
public class ManagedIdentitiesCmd extends AbstractRootCmd {
}
