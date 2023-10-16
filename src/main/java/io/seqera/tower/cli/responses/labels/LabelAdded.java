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

package io.seqera.tower.cli.responses.labels;

import io.seqera.tower.cli.responses.Response;

public class LabelAdded extends Response {


    public final Long id;
    public final String name;
    public final boolean isResource;
    public final String value;

    public final String workspacRef;

    public LabelAdded(Long id, String name, Boolean isResource, String value, String workspacRef) {
        this.id = id;
        this.name = name;
        this.isResource = isResource != null && isResource;
        this.value = value;
        this.workspacRef = workspacRef;
    }

    @Override
    public String toString() {
        String workspaceName = this.workspacRef == null? "" : String.format("at '%s' workspace ",workspacRef);
        String labelFormat = this.isResource? String.format("%s=%s",this.name,this.value) : this.name;
        return ansi(String.format("%n @|yellow Label '%s' added %swith id '%s'|@%n", labelFormat, workspaceName, this.id));
    }
}
