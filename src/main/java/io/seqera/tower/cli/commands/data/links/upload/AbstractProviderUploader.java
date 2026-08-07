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

package io.seqera.tower.cli.commands.data.links.upload;

import io.seqera.tower.ApiException;
import io.seqera.tower.api.DataLinksApi;
import io.seqera.tower.cli.exceptions.TowerRuntimeException;
import io.seqera.tower.cli.utils.progress.PartProgress;
import io.seqera.tower.cli.utils.progress.ProgressTracker;
import io.seqera.tower.cli.utils.progress.ProgressTrackingBodyPublisher;
import io.seqera.tower.model.DataLinkMultiPartUploadRequest;
import io.seqera.tower.model.DataLinkMultiPartUploadResponse;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractProviderUploader implements CloudProviderUploader {

    static final int DEFAULT_PART_SIZE_IN_BYTES = 250 * 1024 * 1024; // 250 MB

    /**
     * Overrides the multipart part size (in bytes).
     */
    public static final String PART_SIZE_ENV = "TOWER_UPLOAD_SIZE_PART_BYTES";

    /** Multipart upload part size in bytes; honors {@link #PART_SIZE_ENV}, defaulting to 250 MB. */
    static int partSizeBytes() {
        String value = System.getenv(PART_SIZE_ENV);
        if (value == null) {
            value = System.getProperty(PART_SIZE_ENV);
        }
        if (value != null) {
            try {
                int parsed = Integer.parseInt(value.trim());
                if (parsed > 0) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the default
            }
        }
        return DEFAULT_PART_SIZE_IN_BYTES;
    }

    /** Max attempts per part, covering both refresh-on-expiry and transient-error retries. */
    static final int MAX_PART_ATTEMPTS = 5;

    /** Number of upcoming parts whose URLs are refreshed in a single call when a credential expires. */
    static final int REFRESH_WINDOW = 100;

    private static final long BACKOFF_BASE_MILLIS = 500L;
    private static final long BACKOFF_MAX_MILLIS = 10_000L;

    // Provider error codes (from the S3/Azure XML <Error><Code>...</Code></Error> body) that mean the
    // signing credentials have expired and the URL must be refreshed before retrying.
    private static final Pattern ERROR_CODE = Pattern.compile("<Code>(.*?)</Code>", Pattern.DOTALL);

    protected final String id;
    protected final String credId;
    protected final Long wspId;
    protected final String outputDir;
    protected final String relativeKey;
    protected final DataLinksApi dataLinksApi;
    protected final int concurrency;

    protected AbstractProviderUploader(String id, String credId, Long wspId, String outputDir, String relativeKey, DataLinksApi dataLinksApi, int concurrency) {
        this.id = id;
        this.credId = credId;
        this.wspId = wspId;
        this.outputDir = outputDir;
        this.relativeKey = relativeKey;
        this.dataLinksApi = dataLinksApi;
        this.concurrency = concurrency;
    }

    protected enum UploadErrorType { EXPIRY, TRANSIENT, HARD_FAIL }

    /** A unit of parallel work: uploads one part (1-based part number) and returns its result. */
    @FunctionalInterface
    protected interface PartTask<R> {
        R run(int partNumber) throws Exception;
    }

    protected byte[] getChunk(File file, int index) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long start = (long) index * partSizeBytes();
            long end = Math.min(start + partSizeBytes(), file.length());
            int length = (int) (end - start);

            byte[] buffer = new byte[length];
            raf.seek(start);
            raf.readFully(buffer);

            return buffer;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected int totalParts(long contentLength) {
        if (contentLength <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) contentLength / partSizeBytes());
    }

    /**
     * Uploads a single part.
     * When {@code refreshable}, an expiry-class error causes the presigned URL (and a forward window of upcoming parts)
     * to be refreshed via the Platform and the part retried;
     * otherwise an expiry is terminal (providers such as Azure/GCS cannot re-mint URLs for an in-progress upload).
     *
     * In both modes a transient error retries the same URL with exponential backoff.
     * On a hard failure, or once the attempt budget is exhausted, the error is propagated so the
     * caller can finalize/abort the upload.
     *
     * @param partUrls     mutable part-number -> URL map, seeded from the initial upload response and
     *                     updated in place as URLs are refreshed
     * @param uploadId     the in-progress multi-part upload id (may be {@code null}, e.g. for Azure)
     * @param successStatus the HTTP status that indicates a successful part upload (200 for S3, 201 for Azure)
     * @param refreshable  whether the provider supports refreshing URLs on expiry (S3 only)
     * @return the successful HTTP response (headers/body available to the caller, e.g. for the S3 ETag)
     */
    protected HttpResponse<String> uploadPartWithRetry(HttpClient client, Map<Integer, String> partUrls, int partNumber,
            byte[] chunk, ProgressTracker tracker, String uploadId, long contentLength, int successStatus, boolean refreshable)
            throws ApiException, IOException, InterruptedException {

        PartProgress part = tracker.newPart();
        for (int attempt = 1; attempt <= MAX_PART_ATTEMPTS; attempt++) {
            String url = partUrls.get(partNumber);
            if (url == null) {
                if (!refreshable) {
                    throw new TowerRuntimeException("Failed to obtain an upload URL for part " + partNumber);
                }
                partUrls.putAll(refreshUrls(uploadId, contentLength, refreshWindow(partNumber, contentLength)));
                url = partUrls.get(partNumber);
                if (url == null) {
                    throw new TowerRuntimeException("Failed to obtain an upload URL for part " + partNumber);
                }
            }

            final String partUrl = url;
            HttpResponse<String> response = sendWithRetryOnTransientError(client, part,
                    () -> HttpRequest.newBuilder()
                            .uri(URI.create(partUrl))
                            .PUT(new ProgressTrackingBodyPublisher(chunk, part))
                            .build());

            if (response.statusCode() == successStatus) {
                return response;
            }

            // Non-success: discard the bytes this attempt reported before deciding what to do.
            part.reset();
            UploadErrorType type = classify(response.statusCode(), response.body());
            if (type == UploadErrorType.EXPIRY && refreshable && attempt < MAX_PART_ATTEMPTS) {
                // Re-sign this part and a forward window of upcoming parts in a single call, then retry.
                partUrls.putAll(refreshUrls(uploadId, contentLength, refreshWindow(partNumber, contentLength)));
                continue;
            }
            throw new IOException("Failed to upload part " + partNumber + ": HTTP " + response.statusCode()
                    + (isNotEmpty(response.body()) ? ", Message: " + response.body() : ""));
        }
        throw new IOException("Failed to upload part " + partNumber + " after " + MAX_PART_ATTEMPTS + " attempts");
    }

    /**
     * Uploads all parts concurrently using a bounded worker pool, returning the per-part results
     * keyed by part number. The effective worker count is {@code min(concurrency, totalParts)} so a
     * small file never spawns more threads than parts.
     *
     * @param totalParts the number of parts (tasks run for part numbers {@code 1..totalParts})
     * @param task       uploads a single part and returns its result (must be non-null to be recorded)
     * @return a map of part number to task result for every part (only populated on full success)
     */
    protected <R> Map<Integer, R> uploadPartsInParallel(int totalParts, PartTask<R> task) {
        int workers = Math.max(1, Math.min(concurrency, totalParts));
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        Map<Integer, R> results = new ConcurrentHashMap<>();
        List<Future<Integer>> futures = new ArrayList<>();
        try {
            CompletionService<Integer> completion = new ExecutorCompletionService<>(pool);
            for (int p = 1; p <= totalParts; p++) {
                final int partNumber = p;
                futures.add(completion.submit(() -> {
                    R r = task.run(partNumber);
                    if (r != null) {
                        results.put(partNumber, r);
                    }
                    return partNumber;
                }));
            }
            for (int i = 0; i < totalParts; i++) {
                completion.take().get();
            }
            return results;
        } catch (ExecutionException e) {
            // First failure: cancel the rest and surface the cause to the caller.
            futures.forEach(f -> f.cancel(true));
            Throwable cause = e.getCause();
            throw new TowerRuntimeException(cause != null ? cause.getMessage() : e.getMessage(), cause);
        } catch (InterruptedException e) {
            futures.forEach(f -> f.cancel(true));
            Thread.currentThread().interrupt();
            throw new TowerRuntimeException("Upload interrupted", e);
        } finally {
            pool.shutdownNow();
            try {
                pool.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Sends a request with transient-failure recovery shared by all providers: network errors and transient
     * HTTP responses (5xx / throttling, per {@link #classify}) are retried by re-sending the same request
     * with exponential backoff, up to {@link #MAX_PART_ATTEMPTS}. Returns the first response that is not a
     * transient failure — the caller decides whether that means success, expiry, resume, or hard failure.
     * Throws the last network error if the budget is exhausted by network failures.
     *
     * @param part     progress accounting for this part; its bytes are rolled back before each attempt
     *                 so a re-send does not double-count (a no-op on the first attempt)
     * @param request  factory invoked once per attempt to build a fresh request (and body publisher)
     */
    protected HttpResponse<String> sendWithRetryOnTransientError(HttpClient client, PartProgress part,
                                                                 Supplier<HttpRequest> request) throws IOException, InterruptedException {

        IOException lastError = null;
        for (int attempt = 1; attempt <= MAX_PART_ATTEMPTS; attempt++) {
            part.reset();
            try {
                HttpResponse<String> response = client.send(request.get(), HttpResponse.BodyHandlers.ofString());
                if (classify(response.statusCode(), response.body()) == UploadErrorType.TRANSIENT && attempt < MAX_PART_ATTEMPTS) {
                    backoff(attempt);
                    continue;
                }
                return response;
            } catch (IOException e) {
                // Network-level failure (connection reset, socket timeout, ...) — treat as transient.
                lastError = e;
                if (attempt == MAX_PART_ATTEMPTS) {
                    break;
                }
                backoff(attempt);
            }
        }
        throw lastError != null ? lastError : new IOException("Request failed after " + MAX_PART_ATTEMPTS + " attempts");
    }

    private List<Integer> refreshWindow(int partNumber, long contentLength) {
        int total = totalParts(contentLength);
        List<Integer> parts = new ArrayList<>();
        for (int p = partNumber; p < partNumber + REFRESH_WINDOW && p <= total; p++) {
            parts.add(p);
        }
        return parts;
    }

    /**
     * Requests freshly-signed upload URLs for the given part numbers.
     */
    protected Map<Integer, String> refreshUrls(String uploadId, long contentLength, List<Integer> partNumbers) throws ApiException {
        DataLinkMultiPartUploadRequest request = new DataLinkMultiPartUploadRequest();
        request.setUploadId(uploadId);
        request.setFileName(relativeKey);
        request.setContentLength(contentLength);
        request.setPartNumbers(partNumbers);

        DataLinkMultiPartUploadResponse response;
        try {
            response = outputDir != null
                    ? dataLinksApi.generateDataLinkUploadUrlWithPath(id, outputDir, request, credId, wspId, null)
                    : dataLinksApi.generateDataLinkUploadUrl(id, request, credId, wspId, null);
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new TowerRuntimeException("Token refresh is not supported for this Platform version.");
            }
            throw e;
        }

        // A Platform that predates re-signing ignores the uploadId/partNumbers fields and instead initiates a
        // brand-new multi-part upload, returning a different uploadId. Detect that by the echoed uploadId and
        // fail clearly rather than mixing URLs from a different upload into the in-progress one.
        if (!uploadId.equals(response.getUploadId())) {
            throw new TowerRuntimeException("Token refresh is not supported for this Platform version.");
        }

        List<String> urls = response.getUploadUrls();
        Map<Integer, String> map = new HashMap<>();
        if (urls != null) {
            if (urls.size() != partNumbers.size()) {
                throw new TowerRuntimeException("Platform returned " + urls.size()
                        + " refreshed upload URLs but " + partNumbers.size() + " were requested");
            }
            for (int i = 0; i < partNumbers.size(); i++) {
                map.put(partNumbers.get(i), urls.get(i));
            }
        }
        return map;
    }

    /**
     * Classifies a failed part upload from its HTTP status and provider error body:
     * <ul>
     *   <li>EXPIRY — the signing credentials expired; the URL must be refreshed before retrying</li>
     *   <li>TRANSIENT — a temporary error (5xx / throttling / network); retry the same URL with backoff</li>
     *   <li>HARD_FAIL — anything else; do not retry</li>
     * </ul>
     */
    protected UploadErrorType classify(int statusCode, String body) {
        String code = extractErrorCode(body);
        if (code != null) {
            switch (code) {
                case "ExpiredToken":            // S3
                case "SignatureDoesNotMatch":   // S3
                case "RequestTimeTooSkewed":    // S3
                    return UploadErrorType.EXPIRY;
                case "InternalError":           // S3
                case "SlowDown":                // S3 throttling
                    return UploadErrorType.TRANSIENT;
                default:
                    // fall through to status-based classification
            }
        }
        if (statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504) {
            return UploadErrorType.TRANSIENT;
        }
        return UploadErrorType.HARD_FAIL;
    }

    protected static String extractErrorCode(String body) {
        if (!isNotEmpty(body)) {
            return null;
        }
        Matcher m = ERROR_CODE.matcher(body);
        return m.find() ? m.group(1).trim() : null;
    }

    protected void backoff(int attempt) throws InterruptedException {
        long base = BACKOFF_BASE_MILLIS * (1L << (attempt - 1));
        long jitter = ThreadLocalRandom.current().nextLong(BACKOFF_BASE_MILLIS / 2);
        Thread.sleep(Math.min(base + jitter, BACKOFF_MAX_MILLIS));
    }
    
    private static boolean isNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}
