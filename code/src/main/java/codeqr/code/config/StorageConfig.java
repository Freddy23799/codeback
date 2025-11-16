package codeqr.code.config;

// import codeqr.code.storage.LocalStorageService;
import codeqr.code.storage.MinioStorageService;
import codeqr.code.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.storage.mode", havingValue = "minio")
    public StorageService minioStorageService(MinioStorageService minio) {
        return minio;
    }

    // @Bean
    // @ConditionalOnProperty(name = "app.storage.mode", havingValue = "local", matchIfMissing = true)
    // public StorageService localStorageService(LocalStorageService local) {
    //     return local;
    // }
}
