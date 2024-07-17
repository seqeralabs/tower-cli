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

package io.seqera.tower.cli.responses.data;

import io.seqera.tower.cli.responses.Response;

public class DataLinkMessageResponse extends Response {

    public final String id;
    public final Long wspId;

    public DataLinkMessageResponse(String id, Long wspId) {
        this.id = id;
        this.wspId = wspId;
    }

    @Override
    public String toString() {
        if (wspId != null) {
            return ansi(String.format("%n  @|yellow Data link '%s' deleted at '%s' workspace.|@%n", id, wspId));
        }
        return ansi(String.format("%n  @|yellow Data link '%s' deleted at user workspace.|@%n", id));
    }
}
