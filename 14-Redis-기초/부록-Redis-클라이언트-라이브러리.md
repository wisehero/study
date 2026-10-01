# 부록 Redis 클라이언트 라이브러리

Java·Kotlin 앱에서 Redis를 사용할 때 등장하는 라이브러리의 역할과 호출 방식을 정리한다. Redis 자체의 개념을 공부하는 본문과 함께 참고하는 부록이다.

## 라이브러리의 역할

클라이언트 라이브러리는 앱의 메서드 호출을 Redis 명령으로 전송하고, 응답을 앱에서 사용할 수 있는 결과로 돌려준다. Java·Kotlin/JVM에서 사용하는 구조는 다음과 같다.

```text
서비스 코드 → Spring Data Redis → Lettuce → Redis 서버
```

| 라이브러리 | 역할 |
| --- | --- |
| Lettuce | Redis 명령 호출과 연결·통신 처리. 동기·비동기·리액티브 API 제공 |
| Jedis | Redis 클라이언트. 동기 호출로 시작하기 쉬운 API 제공 |
| Spring Data Redis | 클라이언트 위에서 RedisTemplate 같은 Spring용 API와 직렬화 기능 제공 |
| Redisson | Redis 클라이언트이면서 RMap·RLock 같은 분산 객체와 기능 제공 |

Spring Boot의 Redis 스타터는 기본적으로 Lettuce를 사용한다. Kotlin/JVM에서도 Java 라이브러리를 사용할 수 있으므로, 연결 구조를 언어별로 따로 익힐 필요는 없다. [Spring Boot Redis 지원](https://docs.spring.io/spring-boot/reference/data/nosql.html#data.nosql.redis), [Lettuce](https://redis.github.io/lettuce/overview/), [Jedis](https://redis.io/docs/latest/clients/jedis/), [Kotlin의 Java 연동](https://kotlinlang.org/docs/java-interop.html)

## Lettuce의 연결 공유

연결은 앱과 Redis 사이에 열어둔 통신 통로다. 단일 Redis 서버에 접속하는 일반적인 경우, 여러 앱 스레드가 하나의 Lettuce TCP 연결을 공유할 수 있다.

```text
앱 스레드 A ── GET product:100 ──┐
                                 ├── 공유 연결 하나 ──→ Redis
앱 스레드 B ── GET product:200 ──┘
```

스레드마다 풀에서 연결 하나를 빌려 독점하는 방식과 다르다. A가 명령을 보내고 응답을 기다리는 동안 B도 같은 연결로 명령을 보낼 수 있다. Lettuce가 각 응답을 해당 호출에 전달한다. **공유 연결은 여러 요청의 전송에 쓰이고, Redis 서버는 일반 명령을 하나씩 실행한다.** [Lettuce 공유 연결과 호출 방식](https://redis.github.io/lettuce/user-guide/async-api/#impact-of-asynchronicity-to-the-synchronous-api)

이 공유는 해당 앱 안에서 이뤄진다. 앱 서버가 여러 대라면 각 앱이 자기 연결을 가진다. 같은 연결을 쓰더라도 각 스레드의 여러 명령이 하나의 원자적 작업으로 묶이는 것은 아니다.

Lettuce도 연결 풀을 사용할 수 있다. `BLPOP` 같은 Redis 대기 명령이나 `MULTI/EXEC` 트랜잭션은 전용 연결이 필요한 경우다. 일반 명령에서는 스레드 안전한 연결 공유 덕분에 연결 풀 없이 사용할 수 있는 경우가 많다. [Lettuce 연결 풀](https://redis.github.io/lettuce/advanced-usage/connection-pooling/)

학습 관점에서는 연결 공유와 다양한 호출 방식을 Lettuce의 주요 장점으로 이해하면 된다.

## 동기와 비동기 호출

**차이는 호출한 앱 스레드가 응답을 기다리며 멈추는지에 있다.** 두 방식 모두 Lettuce가 Redis와 통신한다. 아래 코드는 문자열 키·값을 사용하는 연결이 이미 만들어져 있다고 가정한 설명용 예제다. [Lettuce 호출 방식](https://redis.github.io/lettuce/user-guide/async-api/)

동기 호출은 결과를 받을 때까지 기다린 뒤 다음 줄을 실행한다.

```java
String value = connection.sync().get("product:100");
System.out.println(value);
System.out.println("다음 작업");
```

비동기 호출은 실제 값 대신 나중에 결과가 채워질 `RedisFuture`를 반환한다. `thenAccept`는 정상적으로 결과가 준비됐을 때 실행할 작업을 등록한다.

```java
RedisFuture<String> future = connection.async().get("product:100");
future.thenAccept(value -> System.out.println(value));
System.out.println("다른 작업");
```

호출 스레드는 Redis 응답을 기다리며 멈추지 않는다. 응답이 빨리 올 수 있으므로 값 출력과 다른 작업의 출력 순서는 고정되지 않는다. 등록한 작업이 반드시 원래 호출 스레드에서 실행되는 것도 아니다. 오래 걸리거나 블로킹하는 후속 작업은 Lettuce의 I/O 이벤트 루프를 막지 않도록 별도 실행 스레드로 옮겨야 한다. [Future의 후속 처리](https://redis.github.io/lettuce/user-guide/async-api/#consuming-futures)

다만 비동기로 호출한 뒤 `future.get()`을 사용하면 결과가 준비될 때까지 호출 스레드가 다시 기다린다.

| 호출 | 의미 |
| --- | --- |
| `connection.async().get(key)` | Redis GET 명령을 보내고 Future 반환 |
| `future.get()` | Future의 결과를 기다려 꺼냄 |

**비동기 API 사용 여부와 실제로 스레드를 붙잡아두는지는 구분해야 한다.** Redis 서버의 일반 명령이 병렬로 실행된다는 뜻도 아니다. [Future 결과 대기](https://redis.github.io/lettuce/user-guide/async-api/#consuming-futures)

## Lettuce와 Redisson의 차이

Lettuce는 Redis 명령을 호출하는 데 중심을 두고, Redisson은 Redis를 이용하는 분산 객체와 기능을 제공하는 데 중심을 둔다. 둘 다 동기·비동기·리액티브 API를 지원하므로, 비동기 지원 자체가 핵심 차이는 아니다. [Lettuce API](https://redis.github.io/lettuce/overview/), [Redisson API](https://redisson.pro/docs/api-models/)

| 작업 | Lettuce | Redisson |
| --- | --- | --- |
| 상품 필드 저장 | `HSET` 같은 명령 호출 | `RMap`의 `put` 같은 메서드 호출 |
| 여러 서버에서 공유할 자료구조 | 필요한 명령을 선택해 사용 | RMap·RQueue 같은 객체 API 사용 |
| 분산 락 | Redis 명령·스크립트로 동작 구현 | 준비된 RLock API 사용 |

Redisson의 `RMap`은 앱 메모리에만 있는 일반 `HashMap`과 다르다. Redis에 저장된 데이터를 객체 API로 다루며, 내부 Redis 접근은 Redisson이 처리한다. [Redisson Map](https://redisson.pro/docs/data-and-services/collections/)

`RLock`은 락 획득·해제·대기와 설정에 따른 만료 연장을 제공한다. 여러 앱 서버에서 같은 락을 사용하는 작업을 조율할 수 있다. 해당 락을 사용하지 않는 코드의 데이터 접근까지 자동으로 막는 것은 아니다. [Redisson 락](https://redisson.pro/docs/data-and-services/locks-and-synchronizers/)

## Spring Boot의 자동 설정

Redis 스타터와 `spring.data.redis.*` 설정이 있으면 Spring Boot가 기본 연결과 템플릿을 자동 구성한다. **단일 Redis에 연결해 기본 명령을 사용하는 데는 별도 Configuration 클래스가 필요하지 않다.** [Spring Boot 자동 설정](https://docs.spring.io/spring-boot/reference/data/nosql.html#data.nosql.redis)

| 빈 | 역할 |
| --- | --- |
| LettuceConnectionFactory | Lettuce 연결 생성·관리 |
| StringRedisTemplate | 문자열 키·값으로 Redis 사용 |
| RedisTemplate | 설정한 직렬화 방식으로 다양한 타입 사용 |

Spring의 `LettuceConnectionFactory`는 기본적으로 일반 명령에 같은 네이티브 연결을 공유한다. 다만 Spring의 `RedisConnection` 객체 자체를 여러 스레드에 공유하면 안 된다. 앱에서는 연결을 관리해주는 `RedisTemplate`이나 `StringRedisTemplate`을 사용한다. [Spring 연결 관리](https://docs.spring.io/spring-data/redis/reference/redis/drivers.html)

호스트·포트·비밀번호·타임아웃은 우선 YAML로 지정하면 된다. 객체의 JSON 직렬화, 여러 Redis에 대한 연결, 기본 속성으로 표현하기 어려운 동작을 설정할 때 추가 빈이나 Configuration을 검토한다.

현재 Spring Lab의 코드는 [스타터 의존성](../spring-lab/build.gradle), [연결 설정](../spring-lab/src/main/resources/application.yaml), [Redis 컨테이너](../spring-lab/compose.yaml), [저장·조회·TTL 연결 테스트](../spring-lab/src/test/java/dev/study/commerce/RedisConnectionTest.java)에서 확인할 수 있다.

관련 문서: [Redis 기본 개념](학습%20노트.md), [1장 Redis의 정체](01-Redis의-정체.md).
