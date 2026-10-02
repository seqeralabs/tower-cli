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

import io.seqera.tower.ApiException;
import io.seqera.tower.model.SearchResult;
import picocli.CommandLine.Command;

@Command(
        name = "upstream",
        description = "List the records one hop upstream of a task or file record. By default, the task that produced a file, or the input files of a task."
)
public class UpstreamCmd extends AbstractNavigateCmd {

    @Override
    protected String direction() {
        return "upstream";
    }

    @Override
    protected SearchResult navigate(String lid, Long wspId, String type) throws ApiException {
        return lineageApi().navigateLineageUpstream(lid, wspId, type);
    }
}
