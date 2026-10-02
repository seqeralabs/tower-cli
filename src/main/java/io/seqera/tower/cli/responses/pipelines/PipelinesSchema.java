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


package io.seqera.tower.cli.responses.pipelines;

import io.seqera.tower.cli.responses.Response;
import io.seqera.tower.model.PipelineSchemaAttributes;

import java.util.Map;

public class PipelinesSchema extends Response {

    public final PipelineSchemaAttributes type;
    public final String content;

    public PipelinesSchema(PipelineSchemaAttributes type, String content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public Object getJSON() {
        return Map.of(type.getValue(), content);
    }

    @Override
    public String toString() {
        return content;
    }
}
