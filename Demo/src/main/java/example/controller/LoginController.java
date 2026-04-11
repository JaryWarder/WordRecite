package example.controller;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import example.RSAUtil;
import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
import example.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/login_request")
    public Result<Map<String, String>> loginRequest(HttpServletRequest request) {
        try {
            KeyPair kp = RSAUtil.generateKeyPair();
            RSAPublicKey pub_key = (RSAPublicKey) kp.getPublic();
            RSAPrivateKey pri_key = (RSAPrivateKey) kp.getPrivate();

            String publicKeyExponent = pub_key.getPublicExponent().toString(16);
            String publicKeyModulus = pub_key.getModulus().toString(16);

            request.getSession().setAttribute("pri_key", pri_key);

            Map<String, String> data = new HashMap<>();
            data.put("pub_exp", publicKeyExponent);
            data.put("pub_mod", publicKeyModulus);
            return Result.success(data);
        } catch (Exception e) {
            log.error("Login request error", e);
            return Result.error("Failed to generate RSA keys");
        }
    }

    @PostMapping("/login_check")
    public Result<Map<String, String>> loginCheck(HttpServletRequest request, @RequestBody JSONObject json) {
        try {
            String username = json.getString("username");
            String encryptedPwd = json.getString("encrypted");

            RSAPrivateKey pri_key = (RSAPrivateKey) request.getSession().getAttribute("pri_key");
            if (pri_key == null) {
                return Result.error(400, "Please request public key first");
            }

            byte[] byteEncrypted = hexStringToBytes(encryptedPwd);
            if (byteEncrypted == null) {
                return Result.error(400, "Missing encrypted password");
            }

            byte[] decryptedBytes;
            try {
                decryptedBytes = RSAUtil.decrypt(pri_key, byteEncrypted);
            } catch (Exception ex) {
                decryptedBytes = RSAUtil.decryptNoPadding(pri_key, byteEncrypted);
            }

            String decrypted = rstripNullChars(new String(decryptedBytes, StandardCharsets.UTF_8));
            String reversed = new StringBuilder(decrypted).reverse().toString();

            User user = userMapper.selectById(username);
            if (user == null) {
                return Result.error(404, "User not found");
            }

            String storedPwd = user.getPsw();
            if (storedPwd != null && (storedPwd.equals(decrypted) || storedPwd.equals(reversed))) {
                String token = jwtUtils.generateToken(username);
                Map<String, String> data = new HashMap<>();
                data.put("token", token);
                data.put("username", username);
                return Result.success(data);
            } else {
                return Result.error(401, "Wrong password");
            }
        } catch (Exception e) {
            log.error("Login check error", e);
            return Result.error("Login error");
        }
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private static String rstripNullChars(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '\u0000') {
            end--;
        }
        return end == s.length() ? s : s.substring(0, end);
    }
}
