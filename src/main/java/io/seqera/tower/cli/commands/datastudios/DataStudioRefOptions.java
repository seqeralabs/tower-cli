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

package io.seqera.tower.cli.commands.datastudios;

import picocli.CommandLine;

public class DataStudioRefOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public DataStudioRef dataStudio;

    public static class DataStudioRef {
        @CommandLine.Option(names = {"-i", "--id"}, description = "Studio session ID.")
        public String sessionId;

        @CommandLine.Option(names = {"-n", "--name"}, description = "Studio name.")
        public String dataStudioName;
    }

    public String getDataStudioIdentifier() {
        return dataStudio.sessionId != null ? dataStudio.sessionId : dataStudio.dataStudioName;
    }
}
