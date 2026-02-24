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

package io.seqera.tower.cli.commands.studios;

import picocli.CommandLine;

public class StudioRefOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public StudioRef studio;

    public static class StudioRef {
        @CommandLine.Option(names = {"-i", "--id"}, description = "Studio session identifier")
        public String sessionId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Studio name")
        public String studioName;
    }

    public String getStudioIdentifier() {
        return studio.sessionId != null ? studio.sessionId : studio.studioName;
    }
}
