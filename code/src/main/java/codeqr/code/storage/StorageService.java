package codeqr.code.storage;

import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    /**
     * Upload object to storage at given key (ex: "2025/CS101/level1/myfile1.pdf.gz").
     * Caller must not close data (implementation will read it).
     *
     * @param key object key (path-like)
     * @param data input stream containing the object content
     * @param size object size in bytes (or -1 if unknown)
     * @param contentType optional mime type
     */
    void put(String key, InputStream data, long size, String contentType) throws IOException;

    /**
     * Get object as InputStream (caller must close the stream).
     */
    InputStream get(String key) throws IOException;

    /**
     * Delete object (no-op if not exists).
     */
    void delete(String key) throws IOException;

    /**
     * Copy object inside same backend (sourceKey -> destKey).
     */
    void copy(String sourceKey, String destKey) throws IOException;

    /**
     * Return true if object exists.
     */
    boolean exists(String key) throws IOException;
}
