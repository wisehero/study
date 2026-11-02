# Spring @Transactional 심화 동작 원리

작성일: 2026-11-02 · 기준: Spring Boot 4.1.1, Spring Framework 7.0.9, Hibernate 7.4.5, PostgreSQL 17.6

## 목적과 범위

[기본 동작 원리](Transactional%20기본%20동작%20원리.md)는 "프록시가 트랜잭션을 시작하고 끝낸다"까지 설명한다. 이 문서는 그 안쪽의 세 가지를 다룬다.

1. 프록시 안에서 트랜잭션 처리가 어떤 순서로 진행되는가
2. 전파 설정이 실제 DB 트랜잭션과 연결을 어떻게 바꾸는가
3. 커밋과 롤백은 어떤 판단을 거쳐 결정되는가

일반적인 동기 호출, 기본 프록시 방식, 하나의 DB를 대상으로 한다. 프록시 생성 조건, 스레드에 연결을 묶는 세부 구조, 격리 수준과 잠금은 다루지 않는다.

코드와 동작은 위 버전의 소스와 [실습 테스트](#실습-실행)로 확인했다. 소스만 확인한 내용은 해당 문장에 표시했다.

## 1. 트랜잭션 처리 내부 흐름

### 인터셉터는 순서를 정하고, 매니저는 실제 처리를 맡는다

프록시가 호출하는 `TransactionInterceptor`는 [`TransactionAspectSupport.invokeWithinTransaction`](https://github.com/spring-projects/spring-framework/blob/v7.0.9/spring-tx/src/main/java/org/springframework/transaction/interceptor/TransactionAspectSupport.java)을 실행한다. 핵심만 줄이면 다음과 같다.

```java
TransactionInfo txInfo = createTransactionIfNecessary(manager, attribute, name); // getTransaction 호출

Object result;
try {
    result = invocation.proceedWithInvocation();         // 실제 서비스 메서드 실행
} catch (Throwable error) {
    completeTransactionAfterThrowing(txInfo, invocation, error); // 규칙에 따라 rollback 또는 commit
    throw error;
} finally {
    cleanupTransactionInfo(txInfo);                      // 현재 트랜잭션 정보를 이전 값으로 복원
}

commitTransactionAfterReturning(txInfo);                 // commit 호출
return result;
```

읽을 때 확인할 점은 두 가지다.

- **예외가 프록시까지 와야 `catch`에 들어간다.** 서비스 안에서 잡힌 예외는 이 코드가 알 수 없다. 첫 실습의 `orderWithCaughtError()`가 커밋된 이유다.
- **예외가 나도 `commit`을 호출할 수 있다.** 롤백 규칙에 해당하지 않으면 `completeTransactionAfterThrowing`이 `commit`을 호출한 뒤 예외를 그대로 다시 던진다. 호출자는 예외를 받지만 DB에는 변경이 확정된다.

### 매니저의 공통 처리와 기술별 처리

[`AbstractPlatformTransactionManager`](https://github.com/spring-projects/spring-framework/blob/v7.0.9/spring-tx/src/main/java/org/springframework/transaction/support/AbstractPlatformTransactionManager.java)가 전파와 커밋·롤백 판단을 공통으로 처리하고, 실제 DB 작업은 하위 클래스의 `do...` 메서드에 맡긴다.

| 공통 메서드 | 판단 내용 | 기술별 메서드 |
| --- | --- | --- |
| `getTransaction` | 기존 트랜잭션이 있는지 확인하고 전파 설정에 따라 분기한다. | `doGetTransaction`, `isExistingTransaction`, `doBegin` |
| `commit` → `processCommit` | rollback-only를 확인하고 실제 커밋 대상인지 판단한다. | `doCommit` |
| `rollback` → `processRollback` | 실제 롤백 대상인지, 표시만 남길지 판단한다. | `doRollback`, `doSetRollbackOnly` |
| `suspend`, `resume` | 현재 스레드의 자원을 떼어 두었다가 복원한다. | `doSuspend`, `doResume` |

### 모든 `@Transactional` 호출이 커밋을 요청하지만, DB 커밋은 한 번이다

`getTransaction`이 돌려주는 `TransactionStatus`에는 "이 호출이 DB 트랜잭션을 시작했는가"를 뜻하는 `newTransaction` 값이 있다. 안쪽 메서드가 끝날 때도 `commit`이 호출되지만, `processCommit`은 상태에 따라 다르게 처리한다.

| 상태 | `processCommit` | `processRollback` |
| --- | --- | --- |
| 저장점 보유(`NESTED`) | 저장점 해제 | 저장점까지 되돌림 |
| `newTransaction = true` | `doCommit` 실행 | `doRollback` 실행 |
| 기존 트랜잭션에 참여 | DB 작업 없음 | rollback-only 표시 |

이것이 [기본 문서](Transactional%20기본%20동작%20원리.md)의 "안쪽 메서드가 끝났다고 그 부분만 커밋되지 않는다"의 구현이다.

### 이 프로젝트의 매니저는 `JpaTransactionManager`다

첫 실습은 `JdbcTemplate`만 사용하지만, Spring Lab에 JPA 스타터가 있으므로 자동 설정되는 매니저는 `JpaTransactionManager`다(`OrderPropagationTest` 6번에서 확인). 이 매니저는 트랜잭션을 시작할 때 Hibernate가 쓰는 JDBC 연결을 `DataSource` 기준으로 스레드에 연결해 둔다. 그래서 `JdbcTemplate`이 같은 연결을 찾아 같은 트랜잭션에 참여한다.

매니저 종류에 따라 동작이 달라지는 부분이 있으며, [NESTED](#nested-저장점을-쓰지만-이-프로젝트의-기본-설정에서는-쓸-수-없다)와 [rollback-only](#매니저에-따라-커밋-경로가-다르다)에서 다룬다.

## 2. 전파의 실제 동작

### 논리 트랜잭션과 물리 트랜잭션

`@Transactional` 메서드 호출 하나의 범위를 **논리 트랜잭션**, DB에서 실제로 `BEGIN`부터 `COMMIT`/`ROLLBACK`까지 진행되는 단위를 **물리 트랜잭션**이라고 한다. 전파는 논리 트랜잭션을 물리 트랜잭션에 어떻게 대응시킬지 정하는 규칙이다.

실습에서는 바깥과 안쪽 메서드에서 PostgreSQL의 `pg_backend_pid()`(DB 연결 번호)와 `pg_current_xact_id()`(트랜잭션 번호)를 조회해 비교했다.

| 전파 | DB 연결 | 트랜잭션 번호 | 안쪽 실패를 바깥에서 잡음 | 안쪽 성공 후 바깥 실패 |
| --- | --- | --- | --- | --- |
| `REQUIRED` | 같음 | 같음 | `UnexpectedRollbackException`, 주문 0건·로그 0건 | 소스 확인: 같은 트랜잭션이므로 함께 롤백 |
| `REQUIRES_NEW` | 다름 | 다름 | 정상 반환, 주문 1건·로그 0건 | 예외 전달, 주문 0건·로그 1건 |
| `NESTED` | 같음 | 같음 | 정상 반환, 주문 1건·로그 0건 | 예외 전달, 주문 0건·로그 0건 |

`NESTED` 행은 `DataSourceTransactionManager`에서 확인한 결과다. 나머지 두 행은 프로젝트 기본 매니저인 `JpaTransactionManager` 결과다.

전파 값별 분기는 다음과 같다. `getTransaction`과 `handleExistingTransaction`의 소스 기준이며, 위 표의 세 값 외에는 실습하지 않았다.

| 전파 | 기존 트랜잭션 없음 | 기존 트랜잭션 있음 |
| --- | --- | --- |
| `REQUIRED` | 새로 시작 | 참여 |
| `REQUIRES_NEW` | 새로 시작 | 일시 중단 후 새로 시작 |
| `NESTED` | 새로 시작 | 저장점 생성 |
| `SUPPORTS` | 트랜잭션 없이 실행 | 참여 |
| `MANDATORY` | 예외 | 참여 |
| `NOT_SUPPORTED` | 트랜잭션 없이 실행 | 일시 중단 후 트랜잭션 없이 실행 |
| `NEVER` | 트랜잭션 없이 실행 | 예외 |

전파 설정은 프록시를 거쳐야 적용된다. 같은 객체 안에서 `this.saveWithRequiresNew()`로 호출하면 새 트랜잭션이 시작되지 않는다. 실습에서 `OrderLogService`를 별도 빈으로 둔 이유다.

### REQUIRES_NEW: 바깥을 끝내지 않고 옆으로 치워 둔다

DEBUG 로그로 본 순서는 다음과 같다.

```text
Creating new transaction with name [...OrderFlowService.orderAndFail]
Suspending current transaction, creating new transaction with name [...OrderLogService.saveWithRequiresNew]
Initiating transaction commit                                  ← 안쪽 커밋
Resuming suspended transaction after completion of inner transaction
Initiating transaction rollback                                ← 바깥 롤백
```

`suspend`는 스레드에 연결된 바깥 자원을 떼어 안쪽 `TransactionStatus`에 보관한다. 스레드에는 자원이 없는 상태가 되므로, 안쪽은 풀에서 새 연결을 받아 트랜잭션을 시작한다. 안쪽이 끝나면 `cleanupAfterCompletion`이 보관한 자원을 다시 스레드에 연결한다.

여기서 세 가지 결과가 나온다.

- **안쪽 커밋은 되돌릴 수 없다.** 바깥이 나중에 실패해도 안쪽 변경은 남는다(`OrderPropagationTest` 5번). 실패 이력처럼 남아야 하는 기록에는 맞지만, 주문과 함께 성공해야 하는 데이터에는 맞지 않는다.
- **안쪽 예외도 바깥으로 전달된다.** 안쪽이 독립적으로 롤백되어도 예외는 바깥 메서드로 올라간다. 바깥까지 롤백하지 않으려면 잡아야 한다(`OrderPropagationTest` 4번).
- **한 요청이 연결을 두 개 사용한다.** 바깥은 연결을 반납하지 않고 쥐고 있다. 풀 크기를 1로 줄이면 안쪽이 연결을 얻지 못하고 `CannotCreateTransactionException`이 발생한다(`RequiresNewConnectionPoolTest`).

마지막 항목은 동시 요청에서 더 위험하다. 풀 크기만큼의 요청이 동시에 바깥 연결을 잡으면 모두 두 번째 연결을 기다릴 수 있다. 이 동시 상황은 풀 크기 1 결과에서 추론한 내용이며, 동시 요청으로 재현하지는 않았다.

### NESTED: 저장점을 쓰지만, 이 프로젝트의 기본 설정에서는 쓸 수 없다

**저장점**(savepoint)은 트랜잭션 안에서 돌아갈 지점을 표시하는 DB 기능이다. `NESTED`는 새 물리 트랜잭션을 만들지 않고 저장점을 만든다. 그래서 안쪽 실패는 저장점까지만 되돌리고, 안쪽 성공은 저장점을 해제할 뿐 바깥 커밋을 기다린다.

```text
Creating nested transaction with name [...OrderLogService.failWithNested]
Rolling back transaction to savepoint
Initiating transaction commit                                  ← 바깥 커밋, 주문만 남음
```

`REQUIRES_NEW`와의 차이는 안쪽 성공의 의미다. `REQUIRES_NEW`의 안쪽 커밋은 바깥 실패와 무관하게 남고, `NESTED`의 안쪽 성공은 바깥과 함께 롤백된다.

다만 [`JpaTransactionManager`](https://github.com/spring-projects/spring-framework/blob/v7.0.9/spring-orm/src/main/java/org/springframework/orm/jpa/JpaTransactionManager.java)에서는 다음 결과가 나왔다.

| 설정 | 결과 |
| --- | --- |
| 기본값(`nestedTransactionAllowed = false`) | `NestedTransactionNotSupportedException` |
| `nestedTransactionAllowed = true` | `NestedTransactionNotSupportedException`: `JpaDialect does not support savepoints` |

클래스 설명에는 설정을 켜면 JDBC 접근 코드에서 저장점을 쓸 수 있다고 되어 있지만, 이 프로젝트의 Hibernate 조합에서는 설정을 켜도 저장점을 만들지 못했다. 그래서 `NESTED` 실습은 `JdbcManagerPropagationTest`에서만 매니저를 `DataSourceTransactionManager`로 바꿔 확인했다.

## 3. 롤백 판단과 rollback-only

최종 결과는 두 단계로 결정된다. 먼저 인터셉터가 예외를 보고 `rollback`과 `commit` 중 무엇을 호출할지 정한다. 그다음 매니저가 rollback-only 표시와 트랜잭션 상태를 보고 실제 동작을 정한다. `commit`을 호출했다고 커밋되는 것은 아니다.

### 1단계: 롤백 규칙 매칭

[`RuleBasedTransactionAttribute.rollbackOn`](https://github.com/spring-projects/spring-framework/blob/v7.0.9/spring-tx/src/main/java/org/springframework/transaction/interceptor/RuleBasedTransactionAttribute.java)은 다음 순서로 판단한다.

1. `rollbackFor`, `noRollbackFor`로 만든 규칙마다 던져진 예외에서 규칙의 타입까지 상속 단계를 센다.
2. 가장 적은 단계로 일치한 규칙을 고른다. 고른 규칙에 따라 롤백 여부가 정해진다.
3. 일치하는 규칙이 없으면 기본 규칙(`RuntimeException` 또는 `Error`면 롤백)을 따른다.

| 설정과 예외 | 결과 | 이유 |
| --- | --- | --- |
| 설정 없음, `Exception` | 커밋 | 기본 규칙에서 체크 예외는 롤백 대상이 아니다. |
| `rollbackFor = Exception.class`, `Exception` | 롤백 | 규칙과 일치한다. |
| `rollbackFor = Exception.class`, `noRollbackFor = IllegalStateException.class`, `IllegalStateException` | 커밋 | `noRollbackFor`는 0단계, `rollbackFor`는 2단계에서 일치한다. |

세 경우 모두 호출자는 예외를 받는다. 예외 전달 여부와 롤백 여부는 별개다.

규칙은 프록시 경계마다 각 메서드의 설정으로 따로 평가한다. 안쪽 `noRollbackFor`에 해당하는 예외를 바깥에서 잡으면, 안쪽이 표시를 남기지 않으므로 주문과 로그가 모두 커밋된다(`OrderRollbackRuleTest` 6번). 잡지 않고 바깥 경계까지 전달하면 바깥 메서드의 규칙으로 다시 판단한다(소스 확인).

`rollbackForClassName`은 타입 비교가 아니라 예외 클래스 이름에 문자열이 포함되는지 비교한다. 비슷한 이름의 예외까지 일치할 수 있으므로 클래스를 직접 지정하는 `rollbackFor`가 더 안전하다(소스 확인).

### 2단계: 두 종류의 rollback-only 표시

| 표시 | 저장 위치 | 범위 | 설정 주체 |
| --- | --- | --- | --- |
| 지역 표시 | 해당 호출의 `TransactionStatus` | 그 논리 트랜잭션 | `status.setRollbackOnly()` |
| 전역 표시 | 트랜잭션 객체(JPA는 `EntityTransaction`과 `ConnectionHolder`) | 물리 트랜잭션 전체 | 참여 중 롤백될 때 매니저의 `doSetRollbackOnly` |

전역 표시는 예외와 달리 잡아서 없앨 수 없다. 참여한 메서드가 예외로 끝나는 순간 이미 트랜잭션 객체에 기록되기 때문이다.

### 안쪽 실패를 잡았는데 왜 `UnexpectedRollbackException`이 나는가

`orders.orderAndCatch(logs::failWithRequired)`의 실제 로그다.

```text
Creating new transaction with name [...OrderFlowService.orderAndCatch]
Participating in existing transaction
Participating transaction failed - marking existing transaction as rollback-only
Setting JPA transaction on EntityManager [...] rollback-only
Initiating transaction commit
Committing JPA transaction on EntityManager [...]
```

1. 바깥 `orderAndCatch`가 물리 트랜잭션을 시작한다.
2. 안쪽 `failWithRequired`는 참여한다. 이 호출의 `newTransaction`은 `false`다.
3. 안쪽에서 런타임 예외가 나와 안쪽 프록시가 `rollback`을 호출한다.
4. 참여 중이므로 실제 롤백 대신 전역 rollback-only를 표시하고, 예외는 바깥으로 전달한다.
5. 바깥 메서드가 예외를 잡고 정상 반환하므로 바깥 프록시는 `commit`을 호출한다.
6. 매니저가 전역 표시 때문에 커밋하지 못하고 롤백한 뒤, 호출자에게 `UnexpectedRollbackException`을 던진다.

마지막 예외는 호출자를 보호한다. 바깥 메서드는 정상 반환했으므로, 이 예외가 없으면 호출자는 저장되지 않은 주문을 성공으로 알게 된다.

### 매니저에 따라 커밋 경로가 다르다

결과는 같지만 도달 경로가 다르다. 위 로그에 `Committing JPA transaction`이 찍혀도 실제로는 롤백된다는 점에 주의한다.

| 매니저 | 경로 | 예외 메시지 |
| --- | --- | --- |
| `DataSourceTransactionManager` | `commit`이 전역 표시를 먼저 확인해 `processRollback`으로 간다. | `Transaction rolled back because it has been marked as rollback-only` |
| `JpaTransactionManager` | `shouldCommitOnGlobalRollbackOnly()`가 `true`라 `doCommit`까지 간다. 예외 없이 롤백된 뒤 `processCommit`이 전역 표시를 보고 예외를 던진다. | `Transaction silently rolled back because it has been marked as rollback-only` |

### 예외 없이 `setRollbackOnly`를 호출하면

| 호출 위치 | 결과 | 경로 |
| --- | --- | --- |
| 트랜잭션을 시작한 메서드 | 예외 없이 롤백 | `commit`이 지역 표시를 보고 `processRollback(status, false)`를 호출한다. |
| 참여한 메서드 | 바깥 `commit`에서 `UnexpectedRollbackException` | 안쪽 `commit`이 지역 표시를 보고 `processRollback`을 호출하고, 참여 중이므로 전역 표시로 옮긴다. |

앞의 경우 호출자는 예외 없이 정상 반환을 받는다. 롤백 사실을 호출자에게 알리려면 반환값이나 예외로 별도로 전달해야 한다.

매니저에는 `globalRollbackOnParticipationFailure`(기본 `true`) 설정도 있다. `false`로 바꾸면 참여 메서드가 예외로 끝나도 전역 표시를 남기지 않는다. 트랜잭션 전체에 부분 실패를 허용하게 되므로, 이 문서의 실습에서는 기본값만 사용했다(소스 확인).

## 적용 기준

| 요구 사항 | 선택 | 근거 |
| --- | --- | --- |
| 안쪽과 바깥이 함께 성공하거나 함께 실패 | `REQUIRED`, 예외를 경계 밖으로 전달 | 같은 물리 트랜잭션이다. |
| 바깥이 실패해도 안쪽 기록을 남김 | `REQUIRES_NEW` | 안쪽 커밋이 독립적이다. 연결 2개를 쓰므로 풀 크기와 호출 빈도를 함께 확인한다. |
| 안쪽 실패만 되돌리고 바깥을 계속 진행 | JPA 환경은 `REQUIRES_NEW` 또는 안쪽을 실행하기 전 검증 | `NESTED`는 현재 설정에서 사용할 수 없다. |
| 체크 예외에도 롤백 | `rollbackFor` 지정 | 기본 규칙은 체크 예외를 커밋한다. |
| 참여 메서드의 실패를 바깥에서 잡고 계속 진행 | 현재 구조는 불가, 설계를 변경 | 전역 표시를 지울 수 없다. |

## 실습 실행

| 테스트 | 확인 내용 |
| --- | --- |
| [OrderPropagationTest](../spring-lab/src/test/java/dev/study/commerce/OrderPropagationTest.java) | `REQUIRED`, `REQUIRES_NEW`, JPA 매니저의 `NESTED` 거부 |
| [JdbcManagerPropagationTest](../spring-lab/src/test/java/dev/study/commerce/JdbcManagerPropagationTest.java) | JDBC 매니저의 `NESTED`와 rollback-only 경로 |
| [OrderRollbackRuleTest](../spring-lab/src/test/java/dev/study/commerce/OrderRollbackRuleTest.java) | 롤백 규칙과 `setRollbackOnly` |
| [RequiresNewConnectionPoolTest](../spring-lab/src/test/java/dev/study/commerce/RequiresNewConnectionPoolTest.java) | 연결 부족 시 `REQUIRES_NEW` 실패 |

서비스는 [OrderFlowService](../spring-lab/src/test/java/dev/study/commerce/transaction/OrderFlowService.java)(바깥)와 [OrderLogService](../spring-lab/src/test/java/dev/study/commerce/transaction/OrderLogService.java)(안쪽)다. Spring Lab 폴더에서 Docker가 실행 중인 상태로 다음 명령을 실행한다.

```sh
./gradlew test --tests '*OrderPropagationTest' --tests '*JdbcManagerPropagationTest' --tests '*OrderRollbackRuleTest' --tests '*RequiresNewConnectionPoolTest'
```

매니저 로그를 보려면 앞에 환경 변수 `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_ORM_JPA=DEBUG`를 붙인다. `DataSourceTransactionManager` 로그는 `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_JDBC_DATASOURCE=DEBUG`다. 환경 변수 이름은 소문자로 변환되어 처리되므로 클래스 이름까지 지정하면 적용되지 않고, 패키지까지만 지정해야 한다. 결과는 `build/test-results/test/`의 XML에서 확인한다.

실행 결과는 [실습 기록](실습%20기록.md)에 있다.

## 읽은 뒤 확인할 내용

1. 안쪽 `@Transactional` 메서드가 정상 종료할 때 `commit`이 호출되는데도 DB 커밋이 일어나지 않는 이유는 무엇인가?
2. `REQUIRES_NEW` 안쪽의 예외를 잡지 않으면 주문과 로그는 각각 어떻게 되는가?
3. 풀 크기가 1일 때 `REQUIRES_NEW`가 실패하는 이유를 `suspend` 기준으로 설명할 수 있는가?
4. 체크 예외를 던지는 메서드가 커밋됐을 때 호출자는 무엇을 받는가?
5. 로그에 `Committing JPA transaction`이 찍혔는데 데이터가 없다면 무엇을 확인해야 하는가?

근거: [Spring 공식 문서 — 트랜잭션 전파](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html), [Spring 공식 문서 — 롤백 규칙](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html).

[트랜잭션 공부 목표로 돌아가기](공부%20목표.md)
