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

package io.seqera.tower.cli.commands.pipelines;

import io.seqera.tower.model.WorkflowLaunchRequest;
import io.seqera.tower.model.WorkflowLaunchRequest.SyntaxParserEnum;
import picocli.CommandLine.Option;

/**
 * Launch options backed by a {@code JsonNullable} field of {@link WorkflowLaunchRequest}.
 * <p>
 * They are grouped here because they cannot be set the way the other options are. Their setters wrap the
 * argument in {@code JsonNullable.of()}, so passing a null sends an explicit null rather than leaving the
 * field undefined, and Platform reads an explicit null {@code syntaxParser} as the legacy parser
 * (COMP-2318). {@code coalesce} cannot paper over this: by the time it runs, "the user passed nothing" and
 * "the user passed null" are the same value. So each option is applied only when it was actually given,
 * by {@link #applyNullableOptions}.
 * <p>
 * {@link LaunchOptions} extends this class so the commands mixing it in inherit these options; commands
 * declaring their own option set mix this class in directly. Adding an option here without handling it in
 * {@link #applyNullableOptions} would make it silently do nothing, so a test enforces that every option
 * declared here is applied.
 */
public class NullableLaunchOptions {

    @Option(names = {"--syntax-parser"}, description = "Nextflow language syntax parser version: 'v1' (legacy) or 'v2'. Takes precedence over the value stored in the launch configuration.")
    public SyntaxParserEnum syntaxParser;

    @Option(names = {"--nextflow-version"}, description = "Nextflow version to run the workflow with. Must exist in the Platform version catalog and meet the minimum required by the compute environment. Takes precedence over the value stored in the launch configuration.")
    public String nextflowVersion;

    @Option(names = {"--output-dir"}, description = "Per-run output directory, passed to Nextflow as '-output-dir'. Requires Nextflow 24.10.0 or later and the workflow outputs syntax. Takes precedence over the value stored in the launch configuration.")
    public String outputDir;

    public void applyNullableOptions(WorkflowLaunchRequest request) {
        if (syntaxParser != null) {
            request.syntaxParser(syntaxParser);
        }
        if (nextflowVersion != null) {
            request.nextflowVersion(nextflowVersion);
        }
        if (outputDir != null) {
            request.outputDir(outputDir);
        }
    }
}
