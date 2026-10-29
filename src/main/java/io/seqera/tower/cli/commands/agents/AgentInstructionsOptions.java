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


package io.seqera.tower.cli.commands.agents;

import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;

import static io.seqera.tower.cli.utils.FilesHelper.readStringOrStdin;

public class AgentInstructionsOptions {

    @Option(names = {"--instructions"}, description = "Agent instructions: the role, behavior, and constraints for the agent.")
    public String text;

    @Option(names = {"--instructions-file"}, description = "File containing the agent instructions. Use '-' to read from stdin.")
    public Path file;

    public String read() throws IOException {
        return text != null ? text : readStringOrStdin(file);
    }
}
