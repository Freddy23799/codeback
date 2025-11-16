// package codeqr.code.storage;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Primary;
// import org.springframework.stereotype.Service;
// // import codeqr.code.storage.StorageService;
// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.file.*;

// @Service
// @Primary
// public class LocalStorageService implements StorageService {

//     private final Path root;

//     public LocalStorageService(@Value("${app.storage.path:backend/uploads/exams}") String storageRoot) {
//         this.root = Paths.get(storageRoot).toAbsolutePath().normalize();
//         try {
//             Files.createDirectories(this.root);
//         } catch (IOException e) {
//             throw new RuntimeException("Impossible de créer le dossier de stockage local: " + this.root, e);
//         }
//     }

//     private Path resolvePath(String key) {
//         // Protect against path traversal by normalizing, then ensure it is below root
//         Path p = root.resolve(key).normalize();
//         if (!p.startsWith(root)) {
//             throw new IllegalArgumentException("Key resolves outside storage root");
//         }
//         return p;
//     }

//     @Override
//     public void put(String key, InputStream data, long size, String contentType) throws IOException {
//         Path p = resolvePath(key);
//         Files.createDirectories(p.getParent());
//         // Overwrite if exists
//         try (InputStream in = data) {
//             Files.copy(in, p, StandardCopyOption.REPLACE_EXISTING);
//         }
//     }

//     @Override
//     public InputStream get(String key) throws IOException {
//         Path p = resolvePath(key);
//         if (!Files.exists(p)) throw new IOException("Local object not found: " + p);
//         return Files.newInputStream(p, StandardOpenOption.READ);
//     }

//     @Override
//     public void delete(String key) throws IOException {
//         Path p = resolvePath(key);
//         Files.deleteIfExists(p);
//     }

//     @Override
//     public void copy(String sourceKey, String destKey) throws IOException {
//         Path src = resolvePath(sourceKey);
//         Path dst = resolvePath(destKey);
//         if (!Files.exists(src)) throw new IOException("Source not found: " + src);
//         Files.createDirectories(dst.getParent());
//         Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
//     }

//     @Override
//     public boolean exists(String key) throws IOException {
//         Path p = resolvePath(key);
//         return Files.exists(p);
//     }
// }
