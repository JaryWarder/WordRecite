package example.controller;

import example.dto.UpdatePasswordRequest;
import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
import example.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.security.interfaces.RSAPrivateKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserInfoController {
    private static final Logger log = LoggerFactory.getLogger(UserInfoController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @GetMapping("/get_userInfo/{username}")
    public Result<User> getUserInfo(@PathVariable String username) {
        log.info("getUserInfo, user: {}", username);
        try {
            User u = userMapper.selectById(username);
            if (u != null) {
                // 安全起见，不返回密码
                u.setPsw(null);
                return Result.success(u);
            } else {
                return Result.error(404, "User not found");
            }
        } catch (Exception e) {
            log.error("getUserInfo error", e);
            return Result.error("Failed to get user info");
        }
    }

    @PostMapping("/updatePassword")
    public Result<String> updatePassword(HttpServletRequest request, @RequestBody UpdatePasswordRequest body) {
        try {
            Object authedUsername = request.getAttribute("username");
            if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
                return Result.error(401, "Unauthorized");
            }
            String username = (String) authedUsername;

            Object priKey = request.getSession().getAttribute("pri_key");
            RSAPrivateKey privateKey = priKey instanceof RSAPrivateKey ? (RSAPrivateKey) priKey : null;

            String oldEncrypted = body == null ? null : body.getOldEncrypted();
            String newEncrypted = body == null ? null : body.getNewEncrypted();
            return userService.updatePassword(username, oldEncrypted, newEncrypted, privateKey);
        } catch (Exception e) {
            log.error("updatePassword error", e);
            return Result.error("Failed to update password");
        }
    }
}
