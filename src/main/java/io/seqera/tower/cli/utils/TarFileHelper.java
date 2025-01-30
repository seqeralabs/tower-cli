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

import io.seqera.tower.cli.exceptions.TowerException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TarFileHelper {

    public enum CompressionType {
        XZ_COMPRESSION,
        GZIP_COMPRESSION
    }

    private Path tarFilePath;
    private CompressionType compressionType;

    public TarFileHelper() {}

    public TarFileHelper withFilepath(Path tarFilePath) {

        this.tarFilePath = tarFilePath;

        String fileName = tarFilePath.getFileName().toString();
        if(fileName.endsWith(".xz")) {
            this.compressionType = CompressionType.XZ_COMPRESSION;
        }
        if(fileName.endsWith(".gz")) {
            this.compressionType = CompressionType.GZIP_COMPRESSION;
        }

        return this;
    }

    public TarFileHelper withCompressionType(CompressionType compressionType) {
        this.compressionType = compressionType;
        return this;
    }

    public TarFileAppender buildAppender() throws IOException {

        FileOutputStream fileOut = new FileOutputStream(this.tarFilePath.toFile());
        BufferedOutputStream buffOut = new BufferedOutputStream(fileOut);
        OutputStream compressStream = makeCompressionStream(buffOut);

        TarArchiveOutputStream tarStream = new TarArchiveOutputStream(compressStream);

        return new TarFileAppender(tarStream);
    }

    private OutputStream makeCompressionStream(OutputStream stream) throws IOException {
        switch (this.compressionType) {
            case XZ_COMPRESSION:
                return new XZCompressorOutputStream(stream);
            case GZIP_COMPRESSION:
                return new GzipCompressorOutputStream(stream);
            default:
                return stream;
        }
    }

    public static class TarFileAppender implements AutoCloseable {

        private final TarArchiveOutputStream tarStream;
        private final ExecutorService executor;

        protected TarFileAppender(TarArchiveOutputStream tarStream) {
            this.tarStream = tarStream;
            this.executor = Executors.newSingleThreadExecutor();
        }

        public void add(String fileName, byte[] data) throws IOException {
            executor.execute(() -> {
                try {
                    syncAdd(fileName, data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public void add(String fileName, String data) throws IOException {
            executor.execute(() -> {
                try {
                    syncAdd(fileName, data.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public void add(String fileName, File file) throws IOException {
            executor.execute(() -> {
                try {
                    syncAdd(fileName, file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public void add(String fileName, InputStream fileContent) throws IOException {
            executor.execute(() -> {
                try {
                    syncAdd(fileName, fileContent.readAllBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public void syncAdd(String fileName, byte[] data) throws IOException {
            if (data == null) {
                return;
            }
            TarArchiveEntry entry = new TarArchiveEntry(fileName);
            entry.setSize(data.length);
            tarStream.putArchiveEntry(entry);
            tarStream.write(data);
            tarStream.closeArchiveEntry();
        }

        public void syncAdd(String fileName, File contentFile) throws IOException {
            TarArchiveEntry entry = new TarArchiveEntry(contentFile, fileName);
            tarStream.putArchiveEntry(entry);
            Files.copy(contentFile.toPath(), tarStream);
            tarStream.closeArchiveEntry();
        }

        @Override
        public void close() throws IOException, TowerException {

            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    throw new TowerException("Timeout compressing logs");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

            tarStream.close();
        }
    }

    public static Optional<byte[]> readContentFile(Path tarFilePath, String contentPath) throws IOException {
        try(
                FileInputStream fileInputStream = new FileInputStream(tarFilePath.toFile());
                InputStream decompressionStream = wrapWithCompressionStream(tarFilePath.getFileName().toString(), fileInputStream);
                TarArchiveInputStream tarStream = new TarArchiveInputStream(decompressionStream);
        ) {

            TarArchiveEntry entry = tarStream.getNextTarEntry();
            while (entry != null) {

                if (entry.getName().equals(contentPath)) {
                    return Optional.of(tarStream.readAllBytes());
                }

                entry = tarStream.getNextTarEntry();
            }

            return Optional.empty();
        }
    }

    public static void writeTarFile(Path tarFilePath, Map<String, byte[]> contents) throws IOException {
        try(
                FileOutputStream fileOutputStream = new FileOutputStream(tarFilePath.toFile());
                OutputStream buffOutputStream = new BufferedOutputStream(fileOutputStream);
                OutputStream compressionStream = wrapWithCompressionStream(tarFilePath.getFileName().toString(), buffOutputStream);
                TarArchiveOutputStream tarStream = new TarArchiveOutputStream(compressionStream);
        ) {
            for (var kv : contents.entrySet()) {

                String contentPath = kv.getKey();
                byte[] contentBytes = kv.getValue();

                if (contentBytes == null) continue;

                var entry = new TarArchiveEntry(contentPath);
                entry.setSize(contentBytes.length);
                tarStream.putArchiveEntry(entry);
                tarStream.write(contentBytes);
                tarStream.closeArchiveEntry();
            }
        }
    }

    private static OutputStream wrapWithCompressionStream(String fileName, OutputStream stream) throws IOException {
        if(fileName.endsWith(".xz")) {
            return new XZCompressorOutputStream(stream);
        }
        if(fileName.endsWith(".gz")) {
            return new GzipCompressorOutputStream(stream);
        }
        return stream;
    }

    private static InputStream wrapWithCompressionStream(String fileName, InputStream stream) throws IOException {
        if(fileName.endsWith(".xz")) {
            return new XZCompressorInputStream(stream);
        }
        if(fileName.endsWith(".gz")) {
            return new GzipCompressorInputStream(stream);
        }
        return stream;
    }
}
