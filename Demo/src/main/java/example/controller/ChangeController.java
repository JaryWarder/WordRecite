package example.controller;

import example.common.Result;
import example.dao.DailyMapper;
import example.dao.UserMapper;
import example.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/change")
public class ChangeController {
    private static final Logger log = LoggerFactory.getLogger(ChangeController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DailyMapper dailyMapper;

    @GetMapping("/change_book/{newTitle}")
    @Transactional(rollbackFor = Exception.class)
    public Result<String> changeStudying(@RequestParam String username, @PathVariable String newTitle) {
        log.info("changeStudying, user: {}, newTitle: {}", username, newTitle);
        try {
            User u = userMapper.selectById(username);
            if (u == null) return Result.error("User not found");

            if (newTitle.equals(u.getStudying())) {
                return Result.error("Already studying this book");
            }

            u.setStudying(newTitle);
            u.setStudied(0); // 切换词库时，已学进度清零
            userMapper.updateById(u);

            // 清理旧词库的今日打卡任务
            dailyMapper.deleteByUsername(username);

            return Result.success("success");
        } catch (Exception e) {
            log.error("changeStudying error", e);
            return Result.error("Failed to change book");
        }
    }
}
