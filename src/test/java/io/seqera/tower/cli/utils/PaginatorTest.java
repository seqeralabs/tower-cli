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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginatorTest {

    private record Page(List<Integer> items, Long totalSize) {
    }

    /**
     * Fake list endpoint over the integers [0, size). It serves at most pageCap rows per request and
     * records the offset of every request.
     */
    private static class FakeEndpoint {
        final int size;
        final int pageCap;
        final Long reportedTotal;
        final List<Integer> offsets = new ArrayList<>();

        FakeEndpoint(int size, int pageCap, Long reportedTotal) {
            this.size = size;
            this.pageCap = pageCap;
            this.reportedTotal = reportedTotal;
        }

        Page fetch(int max, int offset) {
            offsets.add(offset);
            int end = Math.min(size, offset + Math.min(max, pageCap));
            List<Integer> items = offset >= size ? List.of() : IntStream.range(offset, end).boxed().toList();
            return new Page(items, reportedTotal);
        }
    }

    private static List<Integer> fetchAll(FakeEndpoint endpoint) throws ApiException {
        return Paginator.fetchAll(endpoint::fetch, Page::items, Page::totalSize);
    }

    @Test
    void testFetchAllWalksEveryPage() throws ApiException {
        FakeEndpoint endpoint = new FakeEndpoint(250, 100, 250L);

        assertEquals(250, fetchAll(endpoint).size());
        assertEquals(List.of(0, 100, 200), endpoint.offsets);
    }

    @Test
    void testFetchAllStopsAtTotalSizeOnAFullLastPage() throws ApiException {
        FakeEndpoint endpoint = new FakeEndpoint(200, 100, 200L);

        assertEquals(200, fetchAll(endpoint).size());
        assertEquals(List.of(0, 100), endpoint.offsets);
    }

    @Test
    void testFetchAllKeepsGoingWhenTheServerServesShortPages() throws ApiException {
        FakeEndpoint endpoint = new FakeEndpoint(120, 50, 120L);

        assertEquals(IntStream.range(0, 120).boxed().toList(), fetchAll(endpoint));
        assertEquals(List.of(0, 50, 100), endpoint.offsets);
    }

    @Test
    void testFetchAllStopsOnAShortPageWithoutTotalSize() throws ApiException {
        FakeEndpoint endpoint = new FakeEndpoint(150, 100, null);

        assertEquals(150, fetchAll(endpoint).size());
        assertEquals(List.of(0, 100), endpoint.offsets);
    }

    @Test
    void testFindFirstStopsFetchingOnceItMatches() throws ApiException {
        FakeEndpoint endpoint = new FakeEndpoint(500, 100, 500L);

        Optional<Integer> found = Paginator.findFirst(endpoint::fetch, Page::items, Page::totalSize, it -> it == 150);

        assertEquals(Optional.of(150), found);
        assertEquals(List.of(0, 100), endpoint.offsets);
    }

    @Test
    void testFindFirstIsEmptyWithoutAMatch() throws ApiException {
        FakeEndpoint endpoint = new FakeEndpoint(150, 100, 150L);

        assertEquals(Optional.empty(), Paginator.findFirst(endpoint::fetch, Page::items, Page::totalSize, it -> it < 0));
        assertEquals(List.of(0, 100), endpoint.offsets);
    }
}
