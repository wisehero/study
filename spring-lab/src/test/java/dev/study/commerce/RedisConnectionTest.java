package dev.study.commerce;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RedisConnectionTest {
    @Autowired StringRedisTemplate redis;

    @Test
    void canSaveAndReadWithTtl() {
        String key = "study:connection:" + UUID.randomUUID();
        try {
            redis.opsForValue().set(key, "안녕 Redis", Duration.ofMinutes(1));

            assertThat(redis.opsForValue().get(key)).isEqualTo("안녕 Redis");
            assertThat(redis.getExpire(key)).isBetween(1L, 60L);
        } finally {
            redis.delete(key);
        }
    }
}
