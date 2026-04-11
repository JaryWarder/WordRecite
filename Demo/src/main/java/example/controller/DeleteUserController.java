package example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import example.common.Result;
import example.dao.DailyMapper;
import example.dao.PrivateBooksMapper;
import example.dao.UserMapper;
import example.pojo.PrivateBooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class DeleteUserController {
    private static final Logger log = LoggerFactory.getLogger(DeleteUserController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PrivateBooksMapper privateBooksMapper;

    @Autowired
    private DailyMapper dailyMapper;

    @GetMapping("/delete_user/{user}")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteUser(@PathVariable String user) {
        log.info("deleteUser, user: {}", user);
        try {
            userMapper.deleteById(user);

            QueryWrapper<PrivateBooks> privateQuery = new QueryWrapper<>();
            privateQuery.eq("username", user);
            privateBooksMapper.delete(privateQuery);

            dailyMapper.deleteByUsername(user);

            return Result.success("success");
        } catch (Exception e) {
            log.error("deleteUser error", e);
            return Result.error("Failed to delete user");
        }
    }
}
