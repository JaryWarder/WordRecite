import base64
import hashlib
import hmac
import json
import os
import time
import urllib.parse
import urllib.request
import urllib.error

BASE = "http://127.0.0.1:8080"
SECRET = os.environ.get("JWT_SECRET", "change-me")


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode("ascii")


def make_jwt(username: str) -> str:
    now = int(time.time())
    header = {"alg": "HS256", "typ": "JWT"}
    payload = {
        "username": username,
        "sub": username,
        "iat": now,
        "exp": now + 86400,
    }
    h = b64url(json.dumps(header, separators=(",", ":")).encode())
    p = b64url(json.dumps(payload, separators=(",", ":")).encode())
    signing_input = f"{h}.{p}".encode()
    sig = hmac.new(SECRET.encode(), signing_input, hashlib.sha256).digest()
    return f"{h}.{p}.{b64url(sig)}"


def req(method: str, path: str, data: str | None = None, headers: dict | None = None):
    url = BASE + path
    body = None
    hdr = {"Accept": "application/json"}
    if headers:
        hdr.update(headers)
    if data is not None:
        body = data.encode("utf-8")
        hdr.setdefault("Content-Type", "application/json")
    request = urllib.request.Request(url, data=body, headers=hdr, method=method)
    try:
        with urllib.request.urlopen(request, timeout=40) as resp:
            text = resp.read().decode("utf-8")
            return resp.status, text, None
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8", errors="ignore")
        return e.code, body, None
    except Exception as e:
        return None, None, str(e)


def main():
    report = []

    status, text, err = req("GET", "/api/user/login_request")
    if err:
        report.append(("GET /api/user/login_request", status, None, "FAIL", {"error": err}))
    else:
        obj = json.loads(text)
        ok = status == 200 and obj.get("code") == 200 and isinstance(obj.get("data"), dict) and "pub_exp" in obj["data"] and "pub_mod" in obj["data"]
        report.append(
            (
                "GET /api/user/login_request",
                status,
                obj.get("code"),
                "PASS" if ok else "FAIL",
                {"has_pub_exp": isinstance(obj.get("data"), dict) and "pub_exp" in obj["data"], "has_pub_mod": isinstance(obj.get("data"), dict) and "pub_mod" in obj["data"]},
            )
        )

    username = f"autotest_{int(time.time())}"
    password = "Aa123456"
    obj_payload = json.dumps({"username": username, "password": password, "email": f"{username}@example.com"})
    query = urllib.parse.urlencode({"obj": obj_payload})
    status, text, err = req("GET", "/api/user/submit_signup?" + query)
    if err:
        report.append(("GET /api/user/submit_signup", status, None, "FAIL", {"username": username, "error": err}))
    else:
        obj = json.loads(text)
        ok = status == 200 and obj.get("code") == 200 and obj.get("msg") == "success"
        report.append(
            (
                "GET /api/user/submit_signup",
                status,
                obj.get("code"),
                "PASS" if ok else "FAIL",
                {"username": username, "msg": obj.get("msg"), "data": obj.get("data")},
            )
        )

    token = make_jwt(username)
    auth = {"Authorization": "Bearer " + token}

    status, text, err = req("GET", f"/api/start/start_info/{username}", headers=auth)
    if err:
        report.append((f"GET /api/start/start_info/{username}", status, None, "FAIL", {"error": err}))
    else:
        obj = json.loads(text)
        data = obj.get("data") if isinstance(obj.get("data"), dict) else {}
        ok = status == 200 and obj.get("code") == 200 and data.get("info") in ("no_book", "success")
        report.append(
            (
                f"GET /api/start/start_info/{username}",
                status,
                obj.get("code"),
                "PASS" if ok else "FAIL",
                {"info": data.get("info"), "msg": obj.get("msg")},
            )
        )

    status, text, err = req("GET", f"/api/user/get_userInfo/{username}", headers=auth)
    if err:
        report.append((f"GET /api/user/get_userInfo/{username}", status, None, "FAIL", {"error": err}))
    else:
        obj = json.loads(text)
        user_data = obj.get("data") if isinstance(obj.get("data"), dict) else {}
        ok = status == 200 and obj.get("code") == 200 and user_data.get("username") == username and user_data.get("psw") is None
        report.append(
            (
                f"GET /api/user/get_userInfo/{username}",
                status,
                obj.get("code"),
                "PASS" if ok else "FAIL",
                {"username": user_data.get("username"), "psw_is_null": user_data.get("psw") is None, "msg": obj.get("msg")},
            )
        )

    status, text, err = req("GET", f"/api/books/get_books/{username}", headers=auth)
    if err:
        report.append((f"GET /api/books/get_books/{username}", status, None, "FAIL", {"error": err}))
    else:
        obj = json.loads(text)
        book_list = obj.get("data") if isinstance(obj.get("data"), list) else []
        has_private = any(isinstance(x, dict) and x.get("title") == "Private" for x in book_list)
        ok = status == 200 and obj.get("code") == 200 and has_private
        report.append(
            (
                f"GET /api/books/get_books/{username}",
                status,
                obj.get("code"),
                "PASS" if ok else "FAIL",
                {"book_count": len(book_list), "has_private": has_private, "msg": obj.get("msg")},
            )
        )

    print("=== FUNCTIONAL TEST REPORT ===")
    for idx, row in enumerate(report, 1):
        api, http_status, biz_code, result, detail = row
        print(f"[{idx}] {api}")
        print(f"    http_status={http_status}, biz_code={biz_code}, result={result}")
        print(f"    detail={json.dumps(detail, ensure_ascii=False)}")
    pass_count = len([r for r in report if r[3] == "PASS"])
    fail_count = len(report) - pass_count
    print(f"SUMMARY: PASS={pass_count}, FAIL={fail_count}")


if __name__ == "__main__":
    main()
