package example.controller;

import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
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
}
