package example.controller;

import com.alibaba.fastjson.JSONObject;
import example.RSAUtil;
import example.common.Result;
import example.controller.EmailController.VerifyCode;
import example.dao.UserMapper;
import example.pojo.User;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;

@RestController
@RequestMapping("/api/user")
public class ResetPasswordController {
    private static final Logger log = LoggerFactory.getLogger(ResetPasswordController.class);

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/reset_password")
    public Result<String> resetPassword(HttpServletRequest request, @RequestBody JSONObject json) {
        String username = json.getString("username");
        String code = json.getString("code");
        String newEncrypted = json.getString("newEncrypted");

        if (username == null || username.isBlank() || code == null || code.isBlank() || newEncrypted == null || newEncrypted.isBlank()) {
            return Result.error(400, "参数不完整");
        }

        // 1. 校验验证码
        VerifyCode cached = EmailController.codeCache.get(username);
        if (cached == null) {
            return Result.error(400, "验证码不存在，请先发送验证码");
        }
        if (System.currentTimeMillis() > cached.expireTime) {
            EmailController.codeCache.remove(username);
            return Result.error(400, "验证码已过期，请重新发送");
        }
        if (!cached.code.equals(code)) {
            return Result.error(400, "验证码错误");
        }

        // 2. 获取 RSA 私钥并解密新密码
        RSAPrivateKey priKey = (RSAPrivateKey) request.getSession().getAttribute("pri_key");
        if (priKey == null) {
            return Result.error(400, "请先调用 /api/user/login_request 获取公钥");
        }

        String newPassword;
        try {
            byte[] byteEncrypted = LoginController.hexStringToBytes(newEncrypted);
            if (byteEncrypted == null) {
                return Result.error(400, "加密密码格式错误");
            }
            byte[] decryptedBytes;
            try {
                decryptedBytes = RSAUtil.decrypt(priKey, byteEncrypted);
            } catch (Exception ex) {
                decryptedBytes = RSAUtil.decryptNoPadding(priKey, byteEncrypted);
            }
            String decrypted = rstripNullChars(new String(decryptedBytes, StandardCharsets.UTF_8));
            // 与登录逻辑一致：取 decrypted 和 reversed 中非空的那个
            String reversed = new StringBuilder(decrypted).reverse().toString();
            newPassword = decrypted.isEmpty() ? reversed : decrypted;
        } catch (Exception e) {
            log.error("RSA decrypt error", e);
            return Result.error(500, "密码解密失败");
        }

        // 3. 更新数据库
        User user = userMapper.selectById(username);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPsw(newPassword);
        userMapper.updateById(user);

        // 4. 销毁验证码
        EmailController.codeCache.remove(username);
        log.info("Password reset successfully for user: {}", username);

        return Result.success("密码重置成功，请重新登录");
    }

    private static String rstripNullChars(String s) {
        if (s == null || s.isEmpty()) return s;
        int end = s.length();
        while (end > 0 && s.charAt(end - 1) == '\u0000') end--;
        return end == s.length() ? s : s.substring(0, end);
    }
}
