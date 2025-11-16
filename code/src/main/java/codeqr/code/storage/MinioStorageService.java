package codeqr.code.storage;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioStorageService(
            @Value("${minio.url}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket}") String bucket
    ) throws IOException {
        this.bucket = bucket;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new IOException("Impossible d'initialiser le bucket MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void put(String key, InputStream data, long size, String contentType) throws IOException {
        try {
            PutObjectArgs.Builder b = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(data, size, -1);
            if (contentType != null) b.contentType(contentType);
            minioClient.putObject(b.build());
        } catch (Exception e) {
            throw new IOException("Erreur put object MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream get(String key) throws IOException {
        try {
            return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new IOException("Erreur get object MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(key).build());
        } catch (Exception e) {
            throw new IOException("Erreur delete object MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public void copy(String sourceKey, String destKey) throws IOException {
        try {
            CopySource source = CopySource.builder().bucket(bucket).object(sourceKey).build();
            minioClient.copyObject(CopyObjectArgs.builder().bucket(bucket).object(destKey).source(source).build());
        } catch (Exception e) {
            throw new IOException("Erreur copy object MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) throws IOException {
        try {
            minioClient.statObject(io.minio.StatObjectArgs.builder().bucket(bucket).object(key).build());
            return true;
        } catch (ErrorResponseException err) {
            // StatObject may throw 404
            return false;
        } catch (Exception e) {
            throw new IOException("Erreur exists MinIO: " + e.getMessage(), e);
        }
    }
}
