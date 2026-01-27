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

package io.seqera.tower.cli.commands.global;

import io.seqera.tower.cli.exceptions.TowerException;
import picocli.CommandLine;

public class PaginationOptions {

    public static final int MAX = 100;

    // TODO: The use of the validate option is a work around to an existing Picocli issue. Please refer to https://github.com/seqeralabs/tower-cli/pull/72#issuecomment-952588876
    @CommandLine.ArgGroup(validate = false)
    public Pageable pageable;

    // TODO: The use of the validate option is a work around to an existing Picocli issue. Please refer to https://github.com/seqeralabs/tower-cli/pull/72#issuecomment-952588876
    @CommandLine.ArgGroup(validate = false)
    public Sizeable sizeable;

    public static class Pageable {
        @CommandLine.Option(names = {"--page"}, description = "Page number for paginated results (default: 1)")
        public Integer page;

        @CommandLine.Option(names = {"--offset"}, description = "Row offset for paginated results (default: 0)")
        public Integer offset;
    }

    public static class Sizeable {
        @CommandLine.Option(names = {"--max"}, description = "Maximum number of records to display (default: " + MAX + ")")
        public Integer max;
    }

    public static Integer getMax(PaginationOptions paginationOptions) throws TowerException {
        Integer max = PaginationOptions.MAX;

        if (paginationOptions.sizeable != null) {
            max = paginationOptions.sizeable.max != null ? paginationOptions.sizeable.max : max;
        }

        return max;
    }

    public static Integer getOffset(PaginationOptions paginationOptions, Integer max) throws TowerException {
        int offset = 0;

        if (paginationOptions.pageable != null) {

            if (paginationOptions.pageable.page != null && paginationOptions.pageable.offset != null) {
                throw new TowerException("Please use either --page or --offset as pagination parameter");
            }

            offset = max == null ? offset : paginationOptions.pageable.offset != null ? paginationOptions.pageable.offset : offset;

            if (offset < 0) {
                throw new TowerException("Record offset number must be a positive value.");
            }

            if (max != null && paginationOptions.pageable.page != null) {

                if (paginationOptions.pageable.page < 1) {
                    throw new TowerException("Page number must be greater than zero.");
                }

                offset = (paginationOptions.pageable.page - 1) * max;
            }
        }

        return offset;
    }

}
