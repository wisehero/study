# Java Lab

Java 언어와 표준 라이브러리를 프레임워크 없이 연습하는 프로젝트다. 객체 동등성, 컬렉션, 예외, 제네릭, Stream, 동시성, JVM 진단을 작은 코드와 테스트로 확인한다.

Java 21과 Gradle Wrapper를 사용한다. 실행 코드에는 외부 라이브러리 의존성이 없고 테스트에만 JUnit을 사용한다. Spring·DB·Docker를 실행하지 않아도 된다. 최초 실행 시 Gradle과 테스트 의존성 다운로드에는 인터넷 연결이 필요하다.

## 실행과 테스트

```sh
cd ~/Desktop/study/java-lab
./gradlew run
./gradlew test
```

JDK 21이 필요하다. macOS에서는 필요에 따라 `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`로 선택한다. IntelliJ에서 이 폴더를 독립 Gradle 프로젝트로 열고 Gradle JVM을 21로 지정한다.

## 현재 제공하는 예제

`collections/CollectionExample`은 값이 같은 상품 코드를 중복 제거하고 최초 등장 순서를 유지한다. `ProductCode`는 값 동등성을 제공하는 Java record다. Spring 앱의 상품 엔티티와 공유하는 모델이 아니라 언어 실습용 예제다.

- `./gradlew run`으로 입력과 결과를 확인한다.
- `CollectionExampleTest`로 값 동등성, 입력 목록과 결과의 분리, 빈 입력과 잘못된 코드 처리를 확인한다.
- 테스트 결과는 `build/reports/tests/test/index.html`에 생성된다.

## 첫 번째 실습

1. `ProductCode`를 일반 클래스로 바꾸고 테스트 결과를 예상한다.
2. equals와 hashCode를 직접 구현해 중복 판정이 어떤 계약에 의존하는지 확인한다.
3. LinkedHashSet을 HashSet으로 바꾸고 순서에 관한 보장이 어떻게 달라지는지 설명한다. 우연히 같은 출력이 나오는 것을 순서 보장으로 판단하지 않는다.
4. 입력 목록과 결과가 같은 가변 객체를 공유하는 예제로 확장하고, 얕은 복사와 깊은 복사의 차이를 기록한다.

## 이펙티브 Java 학습

[이펙티브 Java 30선](../01-Java-기본기와-실행-원리/이펙티브-Java/공부%20목표.md)에 항목별 원칙·주의점·실습·완료 기준을 정리했다. 기존 컬렉션 예제로 아이템 10·11부터 시작하고, 구현한 코드와 테스트를 해당 아이템의 기록에 연결한다.

## 확장할 주제

| 패키지 | 학습 내용 | 현재 상태 |
| --- | --- | --- |
| collections | equals·hashCode, 컬렉션 선택과 불변성 | 실행 예제와 테스트 |
| exceptions | 예외 전파와 자원 해제 | 학습 시작 위치 |
| generics | 타입 안전성과 와일드카드 | 학습 시작 위치 |
| streams | 지연 실행과 부수 효과 | 학습 시작 위치 |
| concurrency | 공유 상태·원자성·가시성·스레드 풀 | 학습 시작 위치 |
| jvm | 메모리·GC·스레드 덤프 | 학습 시작 위치 |

아직 구현하지 않은 패키지에는 역할만 적어 두었다. 해당 주제를 시작할 때 예제와 테스트를 추가한다. 스레드 수나 반복 횟수를 크게 늘리는 실험은 작은 입력부터 시작하고 종료 조건을 명시한다.

## Spring 학습과 연결

순수 Java에서 원리를 확인한 뒤 [Spring Lab](../spring-lab/README.md)에서 실제 요청·빈·트랜잭션 환경에 적용한다. 두 프로젝트는 독립 빌드이며 서로의 코드나 테스트를 자동 실행하지 않는다.

예를 들어 Java에서 공유 상태의 경쟁을 재현한 다음 Spring 싱글턴 빈에 요청별 데이터를 저장했을 때의 문제를 비교한다. 코드 공유 라이브러리가 필요한 시점에 의존성 추출을 별도 설계한다.

- [Java 공부 목표](../01-Java-기본기와-실행-원리/공부%20목표.md)
- [전체 공부 목표](../공부%20목표.md)

## 이펙티브 Java 학습 01·02·03 실행

`dev.study.javalab.effectiveJava` 아래에서 우리 인덱스 순서대로 실습한다. `chapter01`과 `chapter02`는 책의 장 번호가 아니라 학습 순서이며, 각각 아이템 49와 54에 해당한다. `chapter03`은 아이템 61이다.

- `chapter01/Product`: 가격과 노출 기간을 모두 검증한 후 변경한다.
- `chapter01/PageSize`: 페이지 크기의 허용 범위 1~100을 검사한다.
- `chapter02/ProductCatalog`: 빈 결과, 조회 실패, 목록 복사와 수정 가능 여부를 구분한다. 외부 저장소 대신 ProductSource 인터페이스로 조회를 모의한다.
- `chapter03/StockRequest`, `BoxingExample`: 입력 누락과 0, 언박싱 오류, 값 비교, 기본형 합산을 확인한다. `BoxingExample.main`으로 3번만 실행할 수도 있다.
- 같은 패키지의 테스트는 경계값과 실패 후 상태, 빈 결과의 수정 가능 여부, 원소 공유를 검증한다.

```sh
./gradlew runEffectiveJava
./gradlew test --tests 'dev.study.javalab.effectiveJava.*'
```

`run`은 기존 컬렉션 예제를 실행한다. 새 코드는 순수 Java 실습이며 HTTP·DB·트랜잭션 검증은 포함하지 않는다.

## 학습 05: 접근 범위와 내부 상태 보호

`effectiveJava.chapter05`의 Product와 PriceValidator 두 파일을 읽고 AccessExampleTest의 두 테스트로 검증한다. private 필드, package-private 보조 클래스, 수정 불가능한 목록 스냅샷이 핵심이다. 다른 chapter의 Product와는 독립된 예제다.

```sh
./gradlew test --tests 'dev.study.javalab.effectiveJava.chapter05.*'
```

별도 main은 추가하지 않았다. 테스트의 private 필드 접근 주석을 해제하면 컴파일 오류를 직접 확인할 수 있다.
