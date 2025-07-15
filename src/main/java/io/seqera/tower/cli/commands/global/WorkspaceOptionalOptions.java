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

package io.seqera.tower.cli.commands.global;

import picocli.CommandLine;

public class WorkspaceOptionalOptions {
    public static final String DESCRIPTION = "Workspace numeric identifier (TOWER_WORKSPACE_ID as default) or workspace reference as OrganizationName/WorkspaceName.";
    public static final String DEFAULT_VALUE = "${TOWER_WORKSPACE_ID}";

    @CommandLine.Option(names = {"-w", "--workspace"}, description = DESCRIPTION, defaultValue = DEFAULT_VALUE)
    public String workspace = null;
}
