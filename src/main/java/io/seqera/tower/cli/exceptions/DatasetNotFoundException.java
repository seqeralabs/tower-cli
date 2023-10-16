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

package io.seqera.tower.cli.exceptions;

public class DatasetNotFoundException extends TowerException {

    public DatasetNotFoundException(String workspaceRef) {
        super(String.format("No datasets found for workspace '%s'", workspaceRef));
    }

    public DatasetNotFoundException(String datasetName, String workspaceName) {
        super(String.format("No dataset '%s' found for workspace '%s'", datasetName, workspaceName));
    }
}
