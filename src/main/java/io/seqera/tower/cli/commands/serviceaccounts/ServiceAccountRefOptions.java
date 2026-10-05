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

package io.seqera.tower.cli.commands.serviceaccounts;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class ServiceAccountRefOptions {

    @ArgGroup(multiplicity = "1")
    public ServiceAccountRef serviceAccount;

    public static class ServiceAccountRef {

        @Option(names = {"-i", "--id"}, description = "Service account numeric identifier.")
        public Long id;

        @Option(names = {"-n", "--name"}, description = "Service account name.")
        public String name;
    }
}
