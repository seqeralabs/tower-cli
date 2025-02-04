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

package io.seqera.tower.cli.exceptions;

public class StudioNotFoundException extends TowerException {

    public StudioNotFoundException(String studioIdentifier, Long workspaceId) {
        super(String.format("Studio '%s' not found at workspace '%s'", studioIdentifier, workspaceId));
    }

    public StudioNotFoundException(String studioIdentifier, String workspace) {
        super(String.format("Studio '%s' not found at workspace '%s'", studioIdentifier, workspace));
    }

}
