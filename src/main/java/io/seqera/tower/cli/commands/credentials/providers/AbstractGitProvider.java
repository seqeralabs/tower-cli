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

package io.seqera.tower.cli.commands.credentials.providers;

import io.seqera.tower.model.Credentials.ProviderEnum;
import io.seqera.tower.model.SecurityKeys;
import picocli.CommandLine.Option;

public abstract class AbstractGitProvider<T extends SecurityKeys> extends AbstractProvider<T> {

    @Option(names = {"--base-url"}, description = "Repository base URL.", order = 10)
    public String baseUrl;

    public AbstractGitProvider(ProviderEnum type) {
        super(type);
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

}
