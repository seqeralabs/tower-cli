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

package io.seqera.tower.cli.commands.data.links;

import picocli.CommandLine;

public class SearchOption {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Show only data links with names that start with the given word.")
    public String startsWith;

    @CommandLine.Option(names = {"-r", "--region"}, description = "Show only data links belonging to given region")
    public String region;

    @CommandLine.Option(names = {"-p", "--providers"}, description = "Show only data links belonging to given providers. [aws, azure, google]")
    public String providers;

    @CommandLine.Option(names = {"-u", "--uri"}, description = "Show only data links with URI (resource reference) that start with the given URI.")
    public String uri;

}
