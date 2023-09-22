/*
 * Copyright (c) 2021, Seqera Labs.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */


package io.seqera.tower.cli.utils;

import io.seqera.tower.cli.commands.global.PaginationOptions;
import io.seqera.tower.cli.exceptions.TowerException;
import io.seqera.tower.cli.responses.Response;

import javax.annotation.Nullable;
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
