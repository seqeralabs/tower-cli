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

import io.seqera.tower.model.ContainerRegistryKeys;
import io.seqera.tower.model.Credentials.ProviderEnum;
import picocli.CommandLine.Option;

import java.io.IOException;

public class ContainerRegistryProvider extends AbstractProvider<ContainerRegistryKeys> {

    @Option(names = {"-u", "--username"}, description = "The user name to grant you access to the container registry.", required = true)
    public String userName;

    @Option(names = {"-p", "--password"}, description = "The password to grant you access to the container registry.", arity = "0..1", interactive = true, required = true)
    public String password;

    @Option(names = {"-r", "--registry"}, description = "The container registry server name [default: 'docker.io'].", defaultValue = "docker.io")
    public String registry;

    public ContainerRegistryProvider() {
        super(ProviderEnum.CONTAINER_REG);
    }

    @Override
    public ContainerRegistryKeys securityKeys() throws IOException {
        return new ContainerRegistryKeys()
                .userName(userName)
                .password(password)
                .registry(registry);
    }
}
