# 1장 UNION

조인이 테이블을 옆으로 이어 붙인다면, UNION은 여러 조회 결과를 **위아래로 이어 붙인다.** 이번 장에서는 UNION과 UNION ALL의 차이, 그리고 합친 결과를 정렬하는 규칙을 정리한다. 강의 섹션 5에 해당한다. [집합 연산](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html)

예시는 다음 두 테이블을 쓴다. `orders`는 올해 주문, `orders_archive`는 지난해까지의 주문을 옮겨 둔 보관 테이블이다. 둘의 구조는 같다.

```sql
CREATE TABLE orders (
    order_id    BIGINT PRIMARY KEY,
    member_id   BIGINT NOT NULL,
    total_price INT NOT NULL,
    ordered_at  DATETIME NOT NULL
);

CREATE TABLE orders_archive LIKE orders;
```

## 1. UNION: 여러 조회 결과를 하나로

**UNION은 두 개 이상의 SELECT 결과를 하나의 결과로 합치고, 중복 행은 하나만 남긴다.** 회원 한 명의 주문을 올해와 지난해 구분 없이 보고 싶다면 두 테이블을 각각 조회해 합친다. [집합 연산](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html)

```sql
SELECT order_id, total_price, ordered_at FROM orders         WHERE member_id = 7
UNION
SELECT order_id, total_price, ordered_at FROM orders_archive WHERE member_id = 7;
```

합치려면 지켜야 할 규칙이 있다.

- **각 SELECT의 컬럼 수가 같아야 한다.** 다르면 1222 오류(`The used SELECT statements have a different number of columns`)가 난다. [서버 오류 목록](https://dev.mysql.com/doc/mysql-errors/8.4/en/server-error-reference.html)
- **결과의 컬럼 이름은 첫 번째 SELECT의 컬럼 이름을 따른다.** 두 번째 SELECT에 별칭을 붙여도 결과 이름은 바뀌지 않는다.
- **같은 위치의 컬럼은 같은 타입이어야 한다.** 타입이 달라도 실행은 되지만, 결과 컬럼의 타입과 길이는 모든 SELECT의 값을 고려해 정해진다. 의도와 다른 변환을 막으려면 타입을 맞춘다. [결과 컬럼 이름과 타입](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html#set-operations-result-set-columns)

컬럼을 이름이 아니라 **위치로** 맞춘다는 점이 중요하다. 첫 SELECT가 `order_id, total_price`를 고르고 두 번째가 `total_price, order_id`를 고르면, 오류 없이 주문 번호와 금액이 섞인 결과가 나온다.

MySQL 8.4는 UNION 말고도 두 결과에 모두 있는 행만 남기는 INTERSECT와, 첫 결과에서 두 번째 결과를 빼는 EXCEPT를 지원한다. 오라클의 MINUS는 지원하지 않는다. [집합 연산](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html)

## 2. UNION ALL: 중복을 지우지 않고 그대로

**UNION ALL은 중복을 지우지 않고 모든 행을 그대로 이어 붙인다.** UNION 뒤에 아무것도 쓰지 않으면 `UNION DISTINCT`와 같아서 중복이 지워진다. [DISTINCT와 ALL](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html#set-operations-distinct-all)

```sql
-- 같은 회원이 두 번 응모하면 이메일이 두 번 나온다
SELECT email FROM coupon_event_entry
UNION ALL
SELECT email FROM review_event_entry;

-- 이메일이 한 번씩만 나온다
SELECT email FROM coupon_event_entry
UNION
SELECT email FROM review_event_entry;
```

어느 쪽을 고를지는 결과에 중복이 있어야 하는지로 정한다.

- **중복이 있으면 안 되는 경우**: 이벤트 두 개에 모두 응모한 회원에게 메일을 한 번만 보내야 한다면 UNION.
- **중복이 생길 수 없거나, 있어도 되는 경우**: `orders`와 `orders_archive`는 같은 주문을 동시에 담지 않는다. 이럴 때는 UNION ALL을 쓴다.

**UNION은 중복을 찾기 위해 합친 결과 전체를 서로 비교해야 하므로, 중복 제거가 필요 없다면 UNION ALL이 일을 덜 한다.** 실행 계획에서 UNION은 각 SELECT 아래에 결과를 모으는 `UNION RESULT` 단계가 붙는다. 실행 계획을 읽는 방법은 [성능 최적화 강의](../02-성능-최적화/공부%20목표.md)에서 다룬다. [EXPLAIN 출력 형식](https://dev.mysql.com/doc/refman/8.4/en/explain-output.html)

한 문장에 UNION과 UNION ALL을 섞으면, **DISTINCT 쪽 UNION이 자기 왼쪽에 있는 UNION ALL의 효과를 덮어쓴다.** 섞어 쓰면 결과를 예상하기 어려우므로 한 문장에서는 한 가지로 통일하는 편이 낫다. [DISTINCT와 ALL](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html#set-operations-distinct-all)

## 3. UNION 정렬

**UNION 결과 전체를 정렬하려면 ORDER BY를 마지막 SELECT 뒤에 한 번만 쓴다.** 이 ORDER BY는 마지막 SELECT가 아니라 합친 결과 전체에 적용된다. [ORDER BY와 LIMIT](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html#set-operations-order-by-limit)

```sql
SELECT order_id, total_price, ordered_at FROM orders         WHERE member_id = 7
UNION ALL
SELECT order_id, total_price, ordered_at FROM orders_archive WHERE member_id = 7
ORDER BY ordered_at DESC
LIMIT 20;
```

정렬에는 몇 가지 규칙이 있다. [ORDER BY와 LIMIT](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html#set-operations-order-by-limit)

- **`orders.ordered_at`처럼 테이블 이름을 붙인 컬럼으로는 정렬할 수 없다.** 첫 SELECT에서 별칭을 주고 그 별칭으로 정렬한다.
- 별칭을 줬다면 원래 컬럼 이름이 아니라 **별칭으로** 정렬해야 한다. 원래 이름을 쓰면 `Unknown column` 오류가 난다.
- `ORDER BY 3`처럼 위치 번호로 정렬하는 방식은 동작하지만 폐기 예정(deprecated)이다.

### 각 SELECT 안에서 정렬하기

각 SELECT에 ORDER BY를 따로 쓰려면 SELECT를 괄호로 감싸야 한다. 그런데 **UNION의 결과는 기본적으로 순서가 정해지지 않으므로, 괄호 안의 ORDER BY는 최종 결과의 순서를 보장하지 않는다.** LIMIT 없이 괄호 안에만 쓴 ORDER BY는 효과가 없어 옵티마이저가 아예 지워 버린다. 괄호 안의 ORDER BY는 **"각 테이블에서 최근 10건씩"처럼 LIMIT과 함께 일부를 고를 때** 쓴다. [ORDER BY와 LIMIT](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html#set-operations-order-by-limit)

```sql
-- 올해 최근 10건과 지난해 최근 10건을 고른 뒤, 합친 20건을 다시 정렬
(SELECT order_id, ordered_at FROM orders         ORDER BY ordered_at DESC LIMIT 10)
UNION ALL
(SELECT order_id, ordered_at FROM orders_archive ORDER BY ordered_at DESC LIMIT 10)
ORDER BY ordered_at DESC;
```

### 어느 SELECT에서 온 행인지 표시하기

결과를 "올해 주문 먼저, 그다음 지난 주문" 순서로 보여 주고 싶다면, 각 SELECT에 출처를 나타내는 컬럼을 하나 더 두고 그 컬럼으로 정렬한다. 같은 컬럼으로 각 행의 출처도 알 수 있다. [ORDER BY와 LIMIT](https://dev.mysql.com/doc/refman/8.4/en/set-operations.html#set-operations-order-by-limit)

```sql
SELECT 1 AS source, order_id, ordered_at FROM orders         WHERE member_id = 7
UNION ALL
SELECT 2,           order_id, ordered_at FROM orders_archive WHERE member_id = 7
ORDER BY source, ordered_at DESC;
```

## 이해를 확인할 질문

1. UNION과 UNION ALL의 결과는 언제 같고, 언제 다른가?
2. `orders`와 `orders_archive`를 합칠 때 UNION보다 UNION ALL이 맞는 이유는 무엇인가?
3. 두 SELECT의 컬럼 순서를 다르게 쓰면 어떤 일이 생기는가?
4. 괄호 안의 ORDER BY만으로 최종 결과의 순서가 보장되지 않는 이유는 무엇인가?

관련 문서: [2장 뷰](02-뷰.md), [공부 목표](공부%20목표.md).
