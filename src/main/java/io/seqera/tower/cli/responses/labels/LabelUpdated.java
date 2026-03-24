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

package io.seqera.tower.cli.responses.labels;

import io.seqera.tower.cli.responses.Response;

public class LabelUpdated extends Response {

    public final Long id;

    public final String name;

    public final String value;

    public final String workspaceRef;

    public LabelUpdated(Long id, String name, String value, String workspace) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.workspaceRef = workspace;
    }


    @Override
    public String toString() {
        String workspaceName = this.workspaceRef == null? "": String.format("at '%s' workspace ",workspaceRef);
        String labelFormat = value != null? String.format("%s=%s",name,value): name;
        return ansi(String.format("%n @|yellow Label with id '%s' %supdated to '%s'|@%n",id,workspaceName,labelFormat));
    }
}
