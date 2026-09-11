# Spring @Transactional 기본 동작 원리

작성일: 2026-09-11

## 목적과 핵심 개념

`@Transactional`은 메서드에 적용할 트랜잭션 설정을 Spring에 전달하는 표시다. 기본 프록시 방식에서 Spring은 서비스 호출을 중개하고, 호출 전후에 트랜잭션을 시작하거나 기존 트랜잭션에 참여한 뒤 종료를 처리한다. 실제 DB 변경의 확정과 취소는 DB가 수행한다.

이 문서는 주문 저장·재고 차감 실습을 읽기 전에 필요한 기초를 설명한다. 일반적인 동기 Java 호출, Spring의 기본 프록시 방식, 하나의 DB를 대상으로 한다. 비동기·리액티브 처리와 여러 DB를 묶는 트랜잭션은 이 설명의 범위에 포함하지 않는다.

**트랜잭션**은 여러 DB 변경을 하나의 작업 단위로 묶는 장치다. 변경을 확정하는 것을 **커밋(commit)**, 취소하는 것을 **롤백(rollback)**이라고 한다. 주문 저장과 재고 차감이 함께 성공해야 한다면 두 변경을 하나의 트랜잭션에서 처리한다.

## 1. 메서드 호출의 전체 흐름

실습의 [OrderService](../spring-lab/src/test/java/dev/study/commerce/transaction/OrderService.java)는 주문을 처리하는 서비스다. 기본 주문 메서드는 다음과 같다.

```java
@Transactional
public void order(String productCode, int quantity) {
    saveOrder(productCode, quantity);
    reduceStock(productCode, quantity);
}
```

본문에는 트랜잭션 시작·커밋·롤백 코드가 없다. Spring이 서비스 바깥에 두는 **프록시(proxy)**, 즉 실제 객체로 호출을 전달하는 중개 객체가 그 처리를 연결한다. 기존 트랜잭션이 없는 첫 실습의 흐름은 다음과 같다.

```text
테스트가 Spring에서 주입받은 서비스의 order() 호출
    ↓
프록시의 트랜잭션 처리
    ├─ 설정 확인 → 트랜잭션 시작
    ├─ 실제 OrderService.order() 호출
    │      ├─ 주문 INSERT 실행
    │      ├─ 재고 SELECT 실행
    │      └─ 재고가 충분하면 UPDATE 실행
    ├─ 정상 반환 또는 예외에 따라 트랜잭션 종료 판단
    └─ 커밋 또는 롤백 → 자원 정리
    ↓
테스트로 결과 반환 또는 예외 전달
```

서비스 본문의 마지막 줄이 실행된 순간과 DB 커밋이 끝난 순간은 다르다. 커밋 처리 중에도 실패할 수 있으므로 호출자는 프록시의 종료 처리까지 거친 결과를 받는다.

## 2. 어노테이션을 실제 동작으로 만드는 구성 요소

Spring이 생성과 의존성 연결을 관리하는 객체를 **빈(bean)**이라고 한다. 트랜잭션 관리 기능이 활성화되어 있으면 Spring은 적용 대상 빈의 설정을 바탕으로 프록시를 준비한다. `@Transactional` 표시만 있는 객체를 Spring 밖에서 생성하고 호출하는 것으로는 이 관리가 적용되지 않는다.

서비스 호출 전후에 공통 처리를 적용하는 방식을 **AOP(Aspect-Oriented Programming, 관점 지향 프로그래밍)**라고 한다. 트랜잭션 관리가 이 방식을 사용하는 한 사례다.

| 구성 요소 | 역할 |
| --- | --- |
| `@Transactional` | 적용할 트랜잭션 설정을 표현한다. |
| 프록시 | 실제 서비스로 들어가는 호출을 중개한다. |
| `TransactionInterceptor` | 프록시 호출 경로에서 설정을 읽고 트랜잭션 처리를 수행하는 구성 요소다. |
| `PlatformTransactionManager` | 일반적인 동기 호출에서 트랜잭션 시작·참여·커밋·롤백을 관리하는 공통 인터페이스다. DB 접근 기술에 맞는 구현을 사용한다. |
| DB | 트랜잭션에 속한 변경을 실제로 확정하거나 취소한다. |

실습의 [OrderTransactionTest](../spring-lab/src/test/java/dev/study/commerce/OrderTransactionTest.java)는 `@Import`로 실습 서비스를 등록하고 `@Autowired`로 주입받는다. 따라서 테스트의 호출이 Spring 프록시를 거친다.

근거: [Spring 공식 문서 — 선언적 트랜잭션 구현](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-decl-explained.html).

## 3. 서로 다른 SQL이 하나의 트랜잭션에 참여하는 이유

**커넥션(connection)**은 DB와 통신하는 연결이다. 현재 실습에서는 트랜잭션에 사용하는 연결을 현재 실행 스레드에 연결해 관리한다. SQL 실행을 돕는 Spring 도구인 **JdbcTemplate**은 같은 데이터 소스의 트랜잭션에 연결된 커넥션을 찾아 사용한다.

그래서 주문 INSERT와 재고 UPDATE가 같은 DB 트랜잭션에서 실행된다. 연결 정보를 각 메서드의 인자로 직접 전달할 필요가 없다. 임의로 별도 DB 연결을 열었다면 동일한 트랜잭션에 자동 참여한다고 가정할 수 없다.

`saveOrder()`와 `reduceStock()`는 private 메서드이며 `@Transactional`이 없다. 그래도 바깥의 `order()`에서 이미 시작한 트랜잭션 안에서 실행되므로 해당 SQL이 함께 처리된다. 메서드마다 어노테이션을 붙여야 하는 것은 아니다.

**SQL 실행과 커밋은 다르다.** 주문 INSERT가 실행됐어도 커밋 전이라면 이후의 실패로 롤백할 수 있다. 이번 예제는 SQL을 바로 실행하는 JdbcTemplate을 사용한다. 객체와 테이블을 연결하는 JPA의 `save()` 호출 시점과 SQL 실행 시점까지 동일하다고 일반화하지 않는다.

근거: [Spring 공식 문서 — 트랜잭션과 DB 자원 연결](https://docs.spring.io/spring-framework/reference/data-access/transaction/tx-resource-synchronization.html).

## 4. 기본 동작: 기존 트랜잭션에 참여하거나 새로 시작한다

메서드를 호출했을 때 기존 트랜잭션을 어떻게 사용할지 정하는 규칙을 **전파(propagation)**라고 한다. 기본값인 `REQUIRED`는 사용할 기존 트랜잭션이 있으면 참여하고, 없으면 새로 시작한다.

따라서 `@Transactional` 메서드를 여러 번 거친다고 DB 트랜잭션이 매번 별도로 생기는 것은 아니다. 같은 트랜잭션에 참여한 내부 메서드가 끝났다고 그 부분만 독립적으로 커밋되는 것도 아니다. 실제 커밋은 바깥에서 시작한 DB 트랜잭션의 종료 시점에 결정된다.

현재 테스트에는 `@Transactional`을 붙이지 않았다. 서비스 호출 시 새 트랜잭션이 시작되고 호출 종료 후 DB를 다시 조회하므로, 실제 커밋·롤백 결과를 관찰할 수 있다.

근거: [Spring 공식 문서 — REQUIRED 전파](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html).

## 5. 커밋과 롤백을 판단하는 기준

별도 롤백 설정이 없는 일반적인 동기 메서드에서는 다음 기준을 적용한다.

| 트랜잭션을 적용한 메서드의 종료 상태 | 기본 처리 |
| --- | --- |
| 정상 반환 | 커밋을 시도한다. 이미 롤백 전용 상태이거나 커밋 중 실패하면 확정되지 않을 수 있다. |
| `RuntimeException` 또는 `Error`가 밖으로 전달됨 | 롤백 대상이다. |
| 체크 예외가 밖으로 전달됨 | 그 예외만으로는 기본 롤백 대상이 아니다. |

**체크 예외**는 Java에서 호출자가 처리하거나 메서드 선언에 명시해야 하는 예외다. `RuntimeException`과 그 하위 예외는 이 의무가 없는 런타임 예외다. 실습의 `IllegalStateException`은 런타임 예외에 해당한다. `rollbackFor` 등 메서드 설정이나 전역 설정을 바꾸면 기본 롤백 기준도 달라진다.

프록시는 메서드 내부에서 발생했다가 잡힌 예외를 모두 감시하지 않는다. 실습의 `orderWithCaughtError()`는 재고 부족 예외를 같은 서비스 안에서 잡고 `false`를 반환한다. 이 반환값은 업무상 실패를 표현하지만, 그 자체가 롤백 요청은 아니다.

| 실습 조건: 초기 주문 0건, 재고 10개 | 프록시에 전달된 결과 | 확인된 DB 결과 |
| --- | --- | --- |
| 3개 주문 성공 | 정상 반환 | 주문 1건, 재고 7개 |
| 11개 주문 중 재고 부족 예외 전달 | `IllegalStateException` | 주문 0건, 재고 10개 |
| 11개 주문 중 재고 부족 예외를 잡음 | `false` 반환 | 주문 1건, 재고 10개 |

마지막 경우는 의도적으로 잘못된 주문이 남게 만든 예제다. 주문과 재고 차감이 함께 성공해야 하는 이 업무에서는 재고 부족 예외를 트랜잭션 경계 밖으로 전달하고, 사용자용 오류 응답으로 바꾸는 처리를 그 경계 밖에서 수행할 수 있다.

두 실패 케이스 모두 재고 UPDATE 전에 예외가 발생한다. 두 번째 케이스에서 주문은 INSERT 후 롤백됐고, 재고는 애초에 바뀌지 않았다. 상세 검증 결과는 [실습 기록](실습%20기록.md)에 있다.

근거: [Spring 공식 문서 — 롤백 규칙](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/rolling-back.html).

## 6. 기본 원리에서 이어지는 주의점

**같은 객체 안의 호출은 프록시를 다시 거치지 않는다.** 기본 프록시 방식에서 `this.otherMethod()`로 호출하면 해당 메서드에 붙은 트랜잭션 설정이 새로 적용되지 않는다. 바깥 메서드에서 시작한 트랜잭션이 있다면 그 트랜잭션은 계속 사용할 수 있다. 이는 private 보조 메서드의 SQL이 바깥 트랜잭션에 참여하는 것과 모순되지 않는다. [공식 문서 — 프록시를 통한 호출](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)

**예외를 잡아도 항상 커밋되는 것은 아니다.** 같은 트랜잭션에 참여한 다른 서비스가 실패해 **롤백 전용(rollback-only)** 상태, 즉 커밋할 수 없는 상태로 표시할 수 있다. 바깥 서비스에서 예외를 잡아도 이 표시가 없어지지 않는다. 바깥에서 커밋하려는 순간 예상과 달리 롤백됐음을 알리는 `UnexpectedRollbackException`이 발생할 수 있다. 현재 실습의 같은 서비스 안에서 직접 던지고 잡는 예외와 구분한다. [공식 문서 — 참여한 트랜잭션의 롤백](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html)

**DB 트랜잭션의 범위와 업무 전체의 범위는 다를 수 있다.** 현재처럼 스레드에 연결된 트랜잭션이 새 스레드로 자동 전달되지는 않는다. 로컬 DB의 롤백은 이미 성공한 외부 결제를 취소하지 않으며, Java 객체의 필드 값을 이전 값으로 자동 복구하지도 않는다. 또한 `@Transactional`만으로 동시 구매의 초과 판매가 모두 방지되는 것은 아니다. 이 경계들은 후속 실습에서 각각 검증한다.

## 읽은 뒤 확인할 내용

1. 서비스 본문에 없는 트랜잭션 시작·종료 처리는 어디에서 수행되는가?
2. private 메서드에 `@Transactional`이 없어도 그 안의 SQL이 함께 롤백되는 이유는 무엇인가?
3. INSERT 실행과 커밋 완료는 어떻게 다른가?
4. 재고 부족 예외를 던지는 경우와 잡고 `false`를 반환하는 경우에 결과가 다른 이유는 무엇인가?

다음 순서는 [첫 실습 학습 노트](학습%20노트.md) → [실습 테스트](../spring-lab/src/test/java/dev/study/commerce/OrderTransactionTest.java)다. 이 문서는 개념 설명이며, 문서 작성만으로 직접 실습과 학습 확인을 완료 처리하지 않는다.

[트랜잭션 공부 목표로 돌아가기](공부%20목표.md)
