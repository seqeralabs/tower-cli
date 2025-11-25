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

package io.seqera.tower.cli.utils;

import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;

import jakarta.annotation.Nullable;
import java.io.PrintWriter;

public class PaginationInfo extends Response {

    private PaginationOptions paginationOptions;
    private @Nullable Long totalSize;

    private PaginationInfo(final PaginationOptions paginationOptions, final @Nullable Long totalSize) {
        this.paginationOptions = paginationOptions;
        this.totalSize = totalSize;
    }

    public static PaginationInfo from(final PaginationOptions paginationOptions) {
        return new PaginationInfo(paginationOptions, null);
    }
    public static PaginationInfo from(
            final PaginationOptions paginationOptions,
            final Long totalSize
    ) {
        return new PaginationInfo(paginationOptions, totalSize);
    }

    public static PaginationInfo from(
            final Integer offset,
            final Integer max,
            final Integer page,
            final Long totalSize
    ) {
        PaginationOptions options = new PaginationOptions();
        options.pageable = new PaginationOptions.Pageable();
        options.sizeable = new PaginationOptions.Sizeable();
        options.pageable.offset = offset;
        options.pageable.page = page;
        options.sizeable.max = max;
        return new PaginationInfo(options, totalSize);
    }

    public static PaginationInfo from(
            final Integer offset,
            final Integer max,
            final Long totalSize
    ) {
        return PaginationInfo.from(offset, max, null, totalSize);
    }

    public static PaginationInfo from(
            final Integer offset,
            final Integer max
    ) {
        return PaginationInfo.from(offset, max, null, null);
    }

    public static void addFooter(final PrintWriter out, final PaginationInfo paginationInfo) {
        if (paginationInfo != null) paginationInfo.toString(out);
    }

    @Override
    public void toString(PrintWriter out) {
        try {
            if (paginationOptions == null) return;
            if (paginationOptions.pageable == null) return;

            String msg = "";

            if (paginationOptions.pageable.page != null) {
                msg += String.format("Page %d, ", paginationOptions.pageable.page);
                msg += "showing ";
            } else {
                msg += "Showing ";
            }

            Integer max = PaginationOptions.getMax(paginationOptions);
            Integer offset = PaginationOptions.getOffset(paginationOptions, max);

            msg += String.format("from %d to %d", offset, offset + max - 1);

            if (totalSize != null) {
                msg += String.format(" from a total of %d entries", totalSize);
            }

            out.println(ansi(String.format("%n  @|bold %s. |@%n", msg)));

        } catch (TowerException ignore) {
            // we expect the pagination options to throw way before we print the results
        }
    }

}
