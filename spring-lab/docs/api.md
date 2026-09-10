# 현재 API 계약

기본 주소는 `http://127.0.0.1:18080`이다. JSON 요청은 `Content-Type: application/json`을 사용한다. 상품 데이터의 가격 단위는 KRW 정수다.

| 메서드 | 경로 | 역할 | 인증 |
| --- | --- | --- | --- |
| GET | `/actuator/health` | 앱·DB 상태 | 불필요 |
| GET | `/api/v1/csrf` | 변경 요청용 토큰 발급 | 불필요, 세션 쿠키 유지 |
| GET | `/api/v1/products?page=0&size=20` | 공개 상품 목록 | 불필요 |
| GET | `/api/v1/products/{id}` | 공개 상품 상세 | 불필요 |
| POST | `/api/v1/admin/products` | 상품 등록 | 관리자 Basic 인증 + CSRF |
| GET | `/actuator/metrics` | 실행 지표 | 관리자 Basic 인증 |

## 상품 등록

```json
{"sku":"MY-FIRST-PRODUCT","name":"수분 크림","price":18000,"status":"ACTIVE"}
```

성공 시 201과 생성한 상품을 반환한다.

```json
{
  "id":"00000000-0000-0000-0000-000000000001",
  "sku":"MY-FIRST-PRODUCT",
  "name":"수분 크림",
  "price":18000,
  "status":"ACTIVE",
  "version":0
}
```

ID는 실제 생성 시 임의 UUID다. 이름은 1~120자이며 앞뒤 공백을 제거한다. 상품 코드는 대문자 영문·숫자·하이픈으로 1~40자다. 상태는 DRAFT·ACTIVE·SUSPENDED 중 하나다. 초안 등록도 가능하므로 공개 상세 주소를 Location 헤더로 약속하지 않는다.

## 상품 목록

```json
{
  "items":[],
  "page":0,
  "size":20,
  "totalElements":0,
  "totalPages":0
}
```

페이지는 0 이상, 크기는 1~50이다. 각 항목의 형식은 상품 응답과 같다. 정렬은 ID 오름차순이다. 초안과 중지 상품은 항목과 총건수에서 제외된다. 목록이 비어 있거나 마지막 페이지를 넘으면 200과 빈 항목을 반환한다.

## 오류

입력 및 업무 오류는 Problem Detail 형식으로 응답한다. `code`는 앱의 분류 값이다.

```json
{
  "type":"about:blank",
  "title":"Not Found",
  "status":404,
  "detail":"상품을 찾을 수 없습니다.",
  "code":"PRODUCT_NOT_FOUND"
}
```

- 400 `INVALID_REQUEST`: 요청 본문, UUID, 페이지 범위 등의 오류다. 본문 필드 검증 실패에는 `violations` 배열이 포함된다.
- 401 `UNAUTHORIZED`: 인증이 필요하거나 인증에 실패했다.
- 403 `FORBIDDEN`: 권한이 부족하거나 CSRF 검증이 실패했다. 변경 요청에서 인증과 토큰이 모두 없으면 CSRF 검사가 먼저 적용되어 403일 수 있다.
- 404 `PRODUCT_NOT_FOUND`: 상품이 없거나 고객에게 공개할 수 없는 상태다.
- 409 `DATA_CONFLICT`: 중복 상품 코드 등 DB 제약과 충돌했다.

정의하지 않은 경로나 HTTP 메서드는 프레임워크의 기본 오류 또는 보안 차단 응답을 사용할 수 있다. 전체 OpenAPI 계약과 모든 오류의 통일은 [API 설계 목표](../../09-API-설계-원칙/공부%20목표.md)의 실습으로 확장한다.

## 변경 요청의 인증과 토큰

1. `/api/v1/csrf`를 호출하고 응답 쿠키를 유지한다.
2. 응답의 `headerName` 이름으로 `token` 값을 헤더에 넣는다.
3. 같은 쿠키와 관리자 Basic 인증을 함께 보내 상품을 등록한다.

[HTTP 예제](../requests/products.http)는 IntelliJ HTTP 클라이언트의 쿠키 보관과 응답 스크립트를 사용한다. [실행 확인 도구](../scripts/smoke.py)는 같은 흐름을 Python으로 수행하므로 IDE 없이도 확인할 수 있다.
