package example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;

@RestController
@RequestMapping("/api/user")
public class SignupController {
    private static final Logger log = LoggerFactory.getLogger(SignupController.class);

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/submit_signup")
    public Result<String> signupUser(@RequestParam String obj) {
        try {
            JSONObject json = JSON.parseObject(obj);
            String username = json.getString("username");
            String password = json.getString("password");
            String email = json.getString("email");

            if (userMapper.selectById(username) != null) {
                return Result.error(400, "user_existed");
            }

            QueryWrapper<User> emailWrapper = new QueryWrapper<>();
            emailWrapper.eq("email", email);
            if (userMapper.selectCount(emailWrapper) > 0) {
                return Result.error(400, "email_existed");
            }

            Calendar now = Calendar.getInstance();
            String date = now.get(Calendar.YEAR) + "-" + (now.get(Calendar.MONTH) + 1) + "-"
                    + now.get(Calendar.DAY_OF_MONTH);

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPsw(password);
            newUser.setEmail(email);
            newUser.setPlan(20);
            newUser.setMyDate(date);
            newUser.setStudying("");
            newUser.setStudied(0);
            newUser.setFinished(0);

            userMapper.insert(newUser);

            return Result.success("success");
        } catch (Exception e) {
            log.error("Signup error", e);
            return Result.error("error");
        }
    }
}
