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

package io.seqera.tower.cli.responses.computeenvs;

import io.seqera.tower.cli.responses.Response;

public class ComputeEnvExport extends Response {

    public String configOutput;
    public final String fileName;

    public ComputeEnvExport(String configOutput, String fileName) {
        this.configOutput = configOutput;
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        if (fileName != null && !fileName.equals("-")) {
            return ansi(String.format("%n  @|yellow Compute environment exported into '%s' |@%n", fileName));
        }

        return configOutput;
    }
}
