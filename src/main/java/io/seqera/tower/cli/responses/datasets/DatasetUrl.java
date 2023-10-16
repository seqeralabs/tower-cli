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

package io.seqera.tower.cli.responses.datasets;

import io.seqera.tower.cli.responses.Response;

import java.io.PrintWriter;

public class DatasetUrl extends Response {

    public final String datasetUrl;
    public final String dataset;
    public final String workspace;

    public DatasetUrl(String datasetUrl, String dataset, String workspace) {
        this.datasetUrl = datasetUrl;
        this.dataset = dataset;
        this.workspace = workspace;
    }

    @Override
    public void toString(PrintWriter out) {
        out.println(ansi(String.format("%n  @|bold Dataset URL|@")));
        out.println(ansi(String.format("%n  @|bold -----------|@")));
        out.println(ansi(String.format("%n  %s", datasetUrl)));
    }
}
