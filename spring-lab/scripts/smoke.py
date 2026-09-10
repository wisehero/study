#!/usr/bin/env python3
"""Run against the local app. Creates one product and checks the public API contract."""
import base64
import http.cookiejar
import json
import os
import urllib.error
import urllib.request
import uuid

base = os.environ.get("STUDY_BASE_URL", "http://127.0.0.1:18080")
username = os.environ.get("STUDY_ADMIN_USERNAME", "study-admin")
password = os.environ.get("STUDY_ADMIN_PASSWORD", "study-local-password")
auth = "Basic " + base64.b64encode(f"{username}:{password}".encode()).decode()
client = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))


def request(path, expected, body=None, headers=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(base + path, data=data, headers=headers or {})
    try:
        response = client.open(req, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        result = json.loads(response.read())
        assert response.status == expected, (path, response.status, result)
        return result


assert request("/actuator/health", 200)["status"] == "UP"
token = request("/api/v1/csrf", 200)
headers = {"Authorization": auth, "Content-Type": "application/json", token["headerName"]: token["token"]}
product = {"sku": "SMOKE-" + uuid.uuid4().hex[:12].upper(), "name": "실행 확인 상품", "price": 1000, "status": "ACTIVE"}
created = request("/api/v1/admin/products", 201, product, headers)
assert request("/api/v1/products/" + created["id"], 200)["sku"] == product["sku"]
assert request("/api/v1/admin/products", 409, product, headers)["code"] == "DATA_CONFLICT"
assert request("/api/v1/products?size=0", 400)["code"] == "INVALID_REQUEST"
print("기동·인증·CSRF·상품 저장/조회·중복 방지·입력 검증 확인 완료")
print("이번 실행에서 생성한 상품 ID:", created["id"])
