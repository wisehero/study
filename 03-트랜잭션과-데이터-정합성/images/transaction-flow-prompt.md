# 트랜잭션 흐름 그림 생성 기록

- 생성일: 2026-09-11
- 생성 방식: Codex 내장 이미지 생성 도구
- 이미지: [transaction-flow.png](transaction-flow.png)
- 사용 문서: [Spring @Transactional 기본 동작 원리](../Transactional%20기본%20동작%20원리.md)
- 확인 내용: 한국어 문구, 호출 순서, 세 케이스의 주문·재고 수치, 기존 트랜잭션이 없는 실습이라는 범위, catch와 커밋의 관계에 대한 주의 문구를 시각적으로 확인했다.

## 최종 생성 프롬프트

```text
Use case: infographic-diagram
Asset type: one polished educational infographic embedded in a Korean Markdown document about Spring @Transactional.
Primary request: Explain the call lifecycle and the three EXISTING order/stock exercises. Accurate technical meaning and readable Korean text are more important than decoration.
Style: premium technical textbook infographic, clean flat editorial illustration, warm white background, navy typography, crisp fine connector arrows, restrained teal/blue for normal commit, coral for rollback, amber for the caught-error problem case. Generous spacing. Wide landscape about 1536x1024 or higher. Korean sans-serif typography, large legible headings. No characters, no logos, no watermark, no decorative code.
Composition: title at top, numbered lifecycle strip in the middle top, three equal result cards in bottom half, one short caution footer.
Title verbatim: "@Transactional은 호출 전후에 동작한다"
Subtitle verbatim: "주문 저장과 재고 차감을 하나의 DB 트랜잭션으로"
Lifecycle: exactly four boxes left-to-right joined by forward arrows:
1. "호출자" with smaller line "order() 호출"
2. "Spring 프록시" with smaller line "트랜잭션 시작"
3. "OrderService" with smaller two lines "주문 저장" and "재고 확인·차감"
4. "Spring 프록시" with smaller line "커밋 또는 롤백"
Boxes 2 and 4 are the SAME proxy at different moments, not two separately stacked proxies. Indicate with a subtle shared bracket labeled "호출 전후의 트랜잭션 관리". Show a small simple database cylinder below the service with label "같은 DB 연결", joined to service only. Do not suggest every method creates a separate connection.
Above three cards small line verbatim: "매 케이스: 주문 0건 · 재고 10개에서 시작"
Card 1 teal:
"01 정상 처리"
"3개 주문 → 정상 반환"
"COMMIT"
"주문 1건 · 재고 7개"
Card 2 coral:
"02 예외 전달"
"11개 주문 → 런타임 예외 전달"
"ROLLBACK"
"주문 0건 · 재고 10개"
Card 3 amber, subtly visually identified as an intentional problem:
"03 예외를 잡고 반환"
"11개 주문 → catch 후 false 반환"
"COMMIT"
"주문 1건 · 재고 10개"
small badge "문제 예제"
Footer exact two lines:
"기존 트랜잭션이 없는 이번 실습의 흐름"
"예외를 잡으면 항상 커밋되는 것은 아니다."
Constraints: output a single finished image. All Korean text spelled accurately. Clear hierarchy, no tiny dense paragraphs. Keep arrows unambiguous, each outcome belongs only to its own card. No claim that catching an exception ALWAYS commits: case 3 is specifically a locally caught Java runtime exception without any rollback-only marking. The failure occurs before any stock UPDATE, so stock stays 10; never illustrate stock changing back from -1. Do not add additional technical claims or extra labels.
```
