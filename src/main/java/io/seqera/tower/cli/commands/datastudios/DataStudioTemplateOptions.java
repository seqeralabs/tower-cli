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

public class DataStudioTemplateOptions {

    @CommandLine.ArgGroup(multiplicity = "1")
    public DataStudioTemplate template;

    public static class DataStudioTemplate {
        @CommandLine.Option(names = {"-t", "--template"}, description = "Container image template to be used for Data Studio. Available templates can be listed with 'studios templates' command")
        public String standardTemplate;

        @CommandLine.Option(names = {"-ct", "--custom-template"}, description = "Custom container image template to be used for Data Studio")
        public String customTemplate;
    }

    public String getTemplate() {
        return template.standardTemplate != null ? template.standardTemplate : template.customTemplate;
    }
}
