# Java·Spring Study

Java 기본기와 Spring 서비스 개발을 각각 독립된 프로젝트에서 실습한다. 공부 목표와 Git 이력은 이 저장소에서 함께 관리한다.

| 프로젝트 | 목적 | 실행 환경 |
| --- | --- | --- |
| [Java Lab](java-lab/README.md) | 객체·컬렉션·예외·동시성·JVM | Java 21, Docker 불필요 |
| [Spring Lab](spring-lab/README.md) | HTTP·트랜잭션·DB·전시·랭킹·검색 | Java 21, PostgreSQL·Docker |

각 폴더에 자체 Gradle 설정과 Wrapper가 있다. 하나의 프로젝트를 실행하거나 테스트해도 다른 프로젝트는 실행되지 않는다. 처음에는 독립 프로젝트로 유지하며 공통 모듈은 필요가 생겼을 때 분리한다.

## 시작하기

```sh
# 순수 Java 예제
cd ~/Desktop/study/java-lab
./gradlew run
./gradlew test
```

```sh
# Spring 앱
cd ~/Desktop/study/spring-lab
docker compose up -d --wait
./gradlew bootRun
```

상세 실행 방법과 종료 방법은 각 프로젝트의 README에 있다. 기존 Spring 앱 폴더 `app`은 `spring-lab`으로 이동했다. Compose 프로젝트 이름은 유지되어 기존 학습 DB 볼륨을 그대로 사용한다.

[전체 공부 목표](공부%20목표.md)에서 주제별 목표·실습·완료 기준을 확인한다. 각 주제의 노트와 실습 기록에는 실제 사용한 프로젝트와 코드를 연결한다.
