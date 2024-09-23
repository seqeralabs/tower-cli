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

/*
 * This Java source file was generated by the Gradle 'init' task.
 */

package io.seqera.tower.cli;

import io.seqera.tower.cli.commands.AbstractCmd;
import io.seqera.tower.cli.commands.ActionsCmd;
import io.seqera.tower.cli.commands.CollaboratorsCmd;
import io.seqera.tower.cli.commands.ComputeEnvsCmd;
import io.seqera.tower.cli.commands.CredentialsCmd;
import io.seqera.tower.cli.commands.DataLinksCmd;
import io.seqera.tower.cli.commands.DatasetsCmd;
import io.seqera.tower.cli.commands.InfoCmd;
import io.seqera.tower.cli.commands.LaunchCmd;
import io.seqera.tower.cli.commands.MembersCmd;
import io.seqera.tower.cli.commands.OrganizationsCmd;
import io.seqera.tower.cli.commands.ParticipantsCmd;
import io.seqera.tower.cli.commands.PipelinesCmd;
import io.seqera.tower.cli.commands.RunsCmd;
import io.seqera.tower.cli.commands.SecretsCmd;
import io.seqera.tower.cli.commands.TeamsCmd;
import io.seqera.tower.cli.commands.WorkspacesCmd;
import io.seqera.tower.cli.commands.enums.OutputType;
import io.seqera.tower.cli.commands.labels.LabelsCmd;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.io.PrintWriter;

import static picocli.AutoComplete.GenerateCompletion;


@Command(
        name = "tw",
        description = "Nextflow Tower CLI.",
        subcommands = {
                ActionsCmd.class,
                CollaboratorsCmd.class,
                ComputeEnvsCmd.class,
                CredentialsCmd.class,
                DataLinksCmd.class,
                DatasetsCmd.class,
                GenerateCompletion.class,
                InfoCmd.class,
                LabelsCmd.class,
                LaunchCmd.class,
                MembersCmd.class,
                OrganizationsCmd.class,
                ParticipantsCmd.class,
                PipelinesCmd.class,
                RunsCmd.class,
                TeamsCmd.class,
                WorkspacesCmd.class,
                SecretsCmd.class,
        }
)
public class Tower extends AbstractCmd {
    @Spec
    public CommandSpec spec;

    @Option(names = {"-t", "--access-token"}, description = "Tower personal access token (TOWER_ACCESS_TOKEN).", defaultValue = "${TOWER_ACCESS_TOKEN}")
    public String token;

    @Option(names = {"-u", "--url"}, description = "Tower server API endpoint URL (TOWER_API_ENDPOINT) [default: 'api.cloud.seqera.io'].", defaultValue = "${TOWER_API_ENDPOINT:-https://api.cloud.seqera.io}")
    public String url;

    @Option(names = {"-o", "--output"}, description = "Show output in defined format (only the 'json' option is available at the moment).", defaultValue = "${TOWER_CLI_OUTPUT_FORMAT:-console}")
    public OutputType output;

    @Option(names = {"-v", "--verbose"}, description = "Show HTTP request/response logs at stderr.")
    public boolean verbose;

    @Option(names = {"--insecure"}, description = "Explicitly allow to connect to a non-SSL secured Tower server (this is not recommended).")
    public boolean insecure;

    public Tower() {
    }

    public static void main(String[] args) {
        System.exit(buildCommandLine().execute(args));
    }

    protected static CommandLine buildCommandLine() {
        Tower app = new Tower();
        CommandLine cmd = new CommandLine(app);
        cmd.setUsageHelpLongOptionsMaxWidth(40);
        return cmd;
    }

    @Override
    public Integer call() {
        // if the command was invoked without subcommand, show the usage help
        spec.commandLine().usage(System.err);
        return -1;
    }

    public PrintWriter getErr() {
        return spec.commandLine().getErr();
    }

    public PrintWriter getOut() {
        return spec.commandLine().getOut();
    }
}
