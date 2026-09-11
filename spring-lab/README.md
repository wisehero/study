# Spring Lab — Commerce Study

Java·Spring 학습 목표를 실제 코드로 연습하는 로컬 백엔드 API 앱이다. 상품 등록·조회 예제를 출발점으로 삼아 주문·재고·전시·랭킹·검색을 직접 구현한다.

## 현재 제공하는 기능

- Java 21, Spring Boot 4.1.1, Gradle Wrapper 9.7.1을 사용하는 단일 애플리케이션이다.
- PostgreSQL과 JPA를 연결하고, DB 변경 이력을 실행하는 Flyway로 테이블을 생성한다.
- 관리자 상품 등록, 공개 상품 목록·상세 조회, 입력 검증, DB 중복 제약, 공통 오류 응답을 제공한다.
- 관리자 인증과 권한 검사, 변경 요청 위조를 방지하는 CSRF 검증을 적용했다.
- 로컬 상품 샘플, 실제 PostgreSQL 통합 테스트, HTTP 요청 예제와 실행 확인 도구를 포함한다.
- 주문 저장·재고 차감의 정상 처리, 예외 전달, 예외를 잡는 경우를 비교하는 [트랜잭션 실습](../03-트랜잭션과-데이터-정합성/학습%20노트.md)을 테스트 코드로 제공한다.
- 주문·재고·전시·랭킹·검색은 확장할 패키지와 과제를 준비한 상태다. 고객 화면, 검색 엔진, 랭킹 집계는 아직 구현하지 않았다.

순수 Java 개념 실습은 [Java Lab](../java-lab/README.md)에서 진행한다. 이 프로젝트는 Spring·HTTP·DB 연동에 집중하며 Java Lab과 독립적으로 빌드한다.

기존 [전체 공부 목표](../공부%20목표.md)는 개념과 완료 기준을 관리하고, 이 앱은 실습 코드와 실행 방법을 관리한다.

## 빠른 실행

JDK 21과 Docker가 필요하다. Gradle은 별도로 설치하지 않아도 된다. 최초 실행은 Gradle·의존성·컨테이너 이미지를 내려받으므로 인터넷 연결이 필요하다.

```sh
cd ~/Desktop/study/spring-lab

# macOS에 설치된 Java 21 선택
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 학습 전용 PostgreSQL 시작
docker compose up -d --wait

# 애플리케이션 실행
./gradlew bootRun
```

앱은 `http://127.0.0.1:18080`, PostgreSQL은 `localhost:15432`를 사용한다. 기본 프로파일은 `local`이며 최초 시작 시 공개 상품 2개와 초안 상품 1개가 등록된다. 다른 PC에서는 운영체제에 맞게 JDK 21을 선택한다. Windows는 `gradlew.bat`를 사용한다.

다른 터미널에서 다음 요청을 확인한다.

```sh
curl http://127.0.0.1:18080/actuator/health
curl 'http://127.0.0.1:18080/api/v1/products?page=0&size=20'

# 상품 1개를 생성하면서 저장·조회·중복·입력 검증까지 확인
python3 scripts/smoke.py
```

`smoke.py`는 실행할 때마다 고유 상품 코드로 상품 하나를 추가한다. Python 3 표준 라이브러리만 사용한다. 생성된 ID를 출력하므로 해당 상품의 처리 결과를 확인할 수 있다.

IntelliJ에서는 이 폴더를 Gradle 프로젝트로 열고 Gradle JVM을 21로 선택한다. [HTTP 요청 예제](requests/products.http)를 위에서 아래 순서로 실행하면 API를 탐색할 수 있다.

## 테스트와 빌드

```sh
./gradlew test
./gradlew bootJar
```

트랜잭션 첫 실습만 실행하려면 `./gradlew test --tests 'dev.study.commerce.OrderTransactionTest'`를 사용한다.

테스트는 Testcontainers가 만드는 별도 PostgreSQL에서 실행한다. 로컬 Compose DB의 데이터를 지우거나 사용하지 않는다. Docker가 동작해야 하며 연결 실패 시 테스트를 건너뛰지 않고 실패한다. 테스트가 끝나면 테스트 컨테이너가 정리된다.

- 결과 보고서: `build/reports/tests/test/index.html`
- 실행 파일: `build/libs/commerce-study-0.0.1-SNAPSHOT.jar`
- 직접 실행: `java -jar build/libs/commerce-study-0.0.1-SNAPSHOT.jar`

DB 스키마 자동 생성은 사용하지 않는다. Flyway가 변경 이력을 적용하고 JPA는 매핑과 스키마가 맞는지 검증한다. 테스트도 동일한 기본 마이그레이션을 사용한다.

## 첫 번째 실습

1. [구조와 설계 선택](docs/architecture.md)을 읽고 상품 등록 요청의 경로를 따라간다.
2. `ProductApiIntegrationTest`에서 중복 저장과 초안 노출 차단을 확인한다.
3. 상품 수정 API를 직접 추가한다. 읽은 버전이 오래된 수정 요청의 충돌 처리를 설계한다.
4. 정상 수정·입력 오류·동시 수정에 대한 테스트와 변경 이유를 기록한다.
5. [실습 순서와 연결 문서](docs/exercises.md)에서 다음 주제를 선택한다.

## 로컬 설정

| 설정 | 기본값 또는 역할 |
| --- | --- |
| 앱 포트 | `STUDY_APP_PORT=18080` |
| Compose DB 포트 | `STUDY_DB_PORT=15432` |
| 앱 DB URL | `STUDY_DB_URL=jdbc:postgresql://localhost:15432/commerce_study` |
| 앱 DB 계정 | `STUDY_DB_USER=study`, `STUDY_DB_PASSWORD=study_local` |
| 관리자 계정 | local 프로파일에서 `study-admin` / `study-local-password` |
| 관리자 계정 변경 | `STUDY_ADMIN_USERNAME`, `STUDY_ADMIN_PASSWORD` |

DB 외부 포트를 바꾸면 앱의 `STUDY_DB_URL`도 함께 변경한다. Compose 내부 DB 계정은 학습용으로 고정되어 있으며, 다른 DB를 쓰면 앱의 연결 정보를 해당 DB에 맞춘다. `.env`는 Docker Compose가 읽는 설정이며, Gradle이나 Java 프로세스에는 환경 변수를 직접 전달해야 한다.

로컬 기본 계정은 코드에 공개된 실습용 값이다. 앱과 DB 포트는 기본적으로 로컬 주소에 바인딩된다. 인증 방식은 HTTP Basic이며 CSRF 토큰과 쿠키를 함께 사용하는 API 예제다. 브라우저 기반 인증이나 외부 배포는 별도 학습 범위로 둔다. local 이외의 프로파일은 관리자 계정 환경 변수를 명시해야 한다.

## 중지와 데이터 유지

애플리케이션은 실행 터미널에서 Ctrl+C로 중지한다.

```sh
docker compose down
```

이 명령은 DB 컨테이너를 내리지만 학습 데이터 볼륨은 유지한다. 완전 초기화가 필요할 때에만 `docker compose down -v`를 사용한다. 이 경우 해당 Compose 프로젝트의 DB 데이터가 삭제된다.

Flyway의 이미 적용된 파일을 고치면 검사 실패가 날 수 있다. 다음 변경은 `V3__...sql`부터 새 파일로 추가한다. `V2`는 local 샘플 데이터용으로 예약되어 있다.

## 문제 해결

- **Java 버전 오류:** `java -version`과 `./gradlew -version`을 확인하고 JDK 21을 선택한다.
- **DB 연결 실패:** `docker compose ps`에서 건강 상태를 확인하고 포트·연결 URL이 맞는지 확인한다.
- **포트 충돌:** Compose DB 포트와 앱 URL을 함께 바꾸거나 앱 포트를 바꾼다.
- **관리자 호출 401:** 사용자 이름과 비밀번호를 확인한다.
- **변경 요청 403:** 관리자 권한과 CSRF 토큰을 확인한다. 토큰을 발급받은 세션 쿠키도 함께 보내야 한다.
- **상품 생성 409:** 같은 상품 코드가 이미 있는지 확인한다.
- **Docker 테스트 연결 실패:** Docker를 실행한다. 환경에서 소켓을 찾지 못하면 해당 환경의 Docker 연결 설정을 확인한다. 테스트를 성공으로 위장하기 위해 건너뛰지 않는다.

## 참고 자료

- [Spring Boot 공식 실행 요구사항](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Initializr](https://start.spring.io/): 프로젝트와 Wrapper 생성 출처
- [API 계약](docs/api.md)
- [구조와 설계 선택](docs/architecture.md)
- [실습 순서](docs/exercises.md)
