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

package io.seqera.tower.cli.commands.data.links;

import picocli.CommandLine;

public class DataLinkRefOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public DataLinkRef dataLinkRef;

    public static class DataLinkRef {

        @CommandLine.Option(names = {"-i","--id"}, description = "Data link identifier")
        public String dataLinkId;
        @CommandLine.Option(names = {"-n", "--name"}, description = "Data link name")
        public String dataLinkName;
        @CommandLine.Option(names = {"--uri"}, description = "Data link URI (e.g., s3://bucket-name)")
        public String dataLinkUri;

    }
}
