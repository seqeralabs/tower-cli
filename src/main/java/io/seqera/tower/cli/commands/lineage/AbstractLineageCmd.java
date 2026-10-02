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


package io.seqera.tower.cli.commands.lineage;

import io.seqera.tower.cli.commands.AbstractApiCmd;
import picocli.CommandLine.Command;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Command
public abstract class AbstractLineageCmd extends AbstractApiCmd {

    /**
     * The SDK encodes the LID path segment once, and the API decodes it twice: proxies normalize an
     * encoded '/' or ':' in paths, so the web UI double-encodes LIDs. Encode once more here to match.
     */
    protected static String lidPathSegment(String lid) {
        return URLEncoder.encode(lid, StandardCharsets.UTF_8);
    }
}
