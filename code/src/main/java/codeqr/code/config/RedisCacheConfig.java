package codeqr.code.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(cfg);
    }

    @Bean
    public RedisCacheManager cacheManager(LettuceConnectionFactory lcf) {
        // ObjectMapper configuré pour java.time + typing polymorphe (pour garder l'info de type)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // IMPORTANT: activer le typing polymorphe pour que GenericJackson2JsonRedisSerializer
        // stocke l'info de type dans le JSON (évite LinkedHashMap au retour)
        BasicPolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer(mapper);
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        RedisCacheConfiguration defaultCfg = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        // ... tes TTLs existants ...
        cacheConfigs.put("teachers", defaultCfg.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("teacherSearch", defaultCfg.entryTtl(Duration.ofSeconds(30)));
        cacheConfigs.put("stats", defaultCfg.entryTtl(Duration.ofSeconds(60)));
        cacheConfigs.put("enrollmentsByLevel", defaultCfg.entryTtl(Duration.ofMinutes(1)));
        cacheConfigs.put("academicYears", defaultCfg.entryTtl(Duration.ofMinutes(100)));
        cacheConfigs.put("campuses", defaultCfg.entryTtl(Duration.ofMinutes(100)));
        cacheConfigs.put("courses", defaultCfg.entryTtl(Duration.ofMinutes(100)));
        cacheConfigs.put("levels", defaultCfg.entryTtl(Duration.ofMinutes(100)));
        cacheConfigs.put("rooms", defaultCfg.entryTtl(Duration.ofMinutes(100)));
        cacheConfigs.put("sexes", defaultCfg.entryTtl(Duration.ofMinutes(100)));
        cacheConfigs.put("specialties", defaultCfg.entryTtl(Duration.ofMinutes(100)));
        cacheConfigs.put("teacherSessions", defaultCfg.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("sessionStudents", defaultCfg.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("studentDashboard", defaultCfg.entryTtl(Duration.ofSeconds(30)));
        cacheConfigs.put("adminsList", defaultCfg.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("adminDetails", defaultCfg.entryTtl(Duration.ofMinutes(1)));
        cacheConfigs.put("teacherReports", defaultCfg.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("pdfs", defaultCfg.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("sessionById", defaultCfg.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("sessionsBySurveillant", defaultCfg.entryTtl(Duration.ofMinutes(12)));
        cacheConfigs.put("sessionsByUser", defaultCfg.entryTtl(Duration.ofMinutes(12)));
        cacheConfigs.put("profiles", defaultCfg.entryTtl(Duration.ofHours(30)));
        cacheConfigs.put("dashboards", defaultCfg.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("attendancesBySession", defaultCfg.entryTtl(Duration.ofDays(3)));
           cacheConfigs.put("professeurTimetables", defaultCfg.entryTtl(Duration.ofMinutes(12)));
        cacheConfigs.put("studentTimetables", defaultCfg.entryTtl(Duration.ofMinutes(12)));

        return RedisCacheManager.builder(lcf)
                .cacheDefaults(defaultCfg)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
