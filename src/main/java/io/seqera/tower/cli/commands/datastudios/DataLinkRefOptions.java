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

import java.util.List;

import picocli.CommandLine;

public class DataLinkRefOptions {

    @CommandLine.ArgGroup
    public DataLinkRef dataLinkRef;

    public static class DataLinkRef {
        @CommandLine.Option(names = {"--mount-data"}, description = "Optional configuration override for 'mountData' setting (comma separate list of data-link names)", split = ",")
        public List<String> mountDataNames;

        @CommandLine.Option(names = {"--mount-data-ids"}, description = "Optional configuration override for 'mountData' setting (comma separate list of data-link Ids)", split = ",")
        public List<String> mountDataIds;

        @CommandLine.Option(names = {"--mount-data-resource-refs"}, description = "Optional configuration override for 'mountData' setting (comma separate list of data-link resource refs)", split = ",")
        public List<String> mountDataResourceRefs;
    }

}
