import requests
import rsa
import json

BASE_URL = "http://localhost:8080/api"
session = requests.Session()

def test_login_request():
    print("Testing GET /user/login_request...")
    resp = session.get(f"{BASE_URL}/user/login_request")
    assert resp.status_code == 200
    data = resp.json()
    assert data["code"] == 200
    return data["data"]

def test_login_check(pub_exp, pub_mod, username, password):
    print("Testing POST /user/login_check...")
    # In java: new String(RSAUtil.decrypt(pri_key, byteEncrypted)).reverse()
    # So we encrypt reversed password
    rev_password = password[::-1]
    
    e = int(pub_exp, 16)
    n = int(pub_mod, 16)
    pub_key = rsa.PublicKey(n, e)
    
    encrypted = rsa.encrypt(rev_password.encode('utf-8'), pub_key)
    encrypted_hex = encrypted.hex().upper()
    
    payload = {
        "username": username,
        "encrypted": encrypted_hex
    }
    
    resp = session.post(f"{BASE_URL}/user/login_check", json=payload)
    print("Login Response Status:", resp.status_code)
    print("Login Response Body:", resp.json())
    return resp

def test_get_user_info(username, token):
    print(f"\nTesting GET /user/get_userInfo/{username}...")
    headers = {"Authorization": token} if token else {}
    resp = session.get(f"{BASE_URL}/user/get_userInfo/{username}", headers=headers)
    print("UserInfo Response Status:", resp.status_code)
    print("UserInfo Response Body:", resp.json())
    return resp

def test_get_word_info(token):
    print("\nTesting GET /word/get_wordInfo...")
    headers = {"Authorization": token} if token else {}
    # Provide a valid table name pattern like some_table
    obj = json.dumps({"title": "word_book_1", "id": 1})
    resp = session.get(f"{BASE_URL}/word/get_wordInfo?obj={obj}", headers=headers)
    print("WordInfo Response Status:", resp.status_code)
    print("WordInfo Response Body:", resp.json())
    return resp

if __name__ == "__main__":
    try:
        keys = test_login_request()
        pub_exp = keys["pub_exp"]
        pub_mod = keys["pub_mod"]
        print(f"Got public key: exp={pub_exp[:10]}... mod={pub_mod[:10]}...\n")
        
        # Test with 'admin'/'123456' which is typical or just a dummy one
        login_resp = test_login_check(pub_exp, pub_mod, "admin", "123456")
        
        token = ""
        username = "admin"
        if login_resp.json().get("code") == 200:
            token = login_resp.json()["data"]["token"]
            print(f"Token obtained: {token[:20]}...")
        else:
            print("Login failed (maybe user not found or wrong password), continuing with empty token to test auth interception")
        
        test_get_user_info(username, token)
        test_get_word_info(token)
        
        print("\nAll API tests finished executing.")
    except Exception as e:
        import traceback
        traceback.print_exc()
