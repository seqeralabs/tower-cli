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

package io.seqera.tower.cli.utils;

import io.seqera.tower.ApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Walks every page of a Platform list endpoint. A single request returns only one page, and the
 * default page is small (10 rows for team members and participants, 50 for teams).
 */
public final class Paginator {

    public static final int PAGE_SIZE = 100;

    @FunctionalInterface
    public interface PageFetcher<R> {
        R fetch(int max, int offset) throws ApiException;
    }

    private Paginator() {
    }

    public static <R, T> List<T> fetchAll(PageFetcher<R> fetcher, Function<R, List<T>> items, Function<R, Long> totalSize) throws ApiException {
        List<T> all = new ArrayList<>();
        findFirst(fetcher, items, totalSize, item -> {
            all.add(item);
            return false;
        });
        return all;
    }

    public static <R, T> Optional<T> findFirst(PageFetcher<R> fetcher, Function<R, List<T>> items, Function<R, Long> totalSize, Predicate<T> filter) throws ApiException {
        int offset = 0;
        while (true) {
            R response = fetcher.fetch(PAGE_SIZE, offset);
            List<T> page = response == null ? null : items.apply(response);
            if (page == null || page.isEmpty()) {
                return Optional.empty();
            }
            for (T item : page) {
                if (filter.test(item)) {
                    return Optional.of(item);
                }
            }
            offset += page.size();
            // Trust totalSize over a short page, so a server that clamps the page size can't end the walk early
            Long total = totalSize.apply(response);
            if (total != null ? offset >= total : page.size() < PAGE_SIZE) {
                return Optional.empty();
            }
        }
    }
}
