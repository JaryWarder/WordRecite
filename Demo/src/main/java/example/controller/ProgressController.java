package example.controller;

import example.common.Result;
import example.dao.UserMapper;
import example.dao.WordBookMapper;
import example.pojo.User;
import example.pojo.WordBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    private static final Logger log = LoggerFactory.getLogger(ProgressController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WordBookMapper wordBookMapper;

    @GetMapping("/get_progress/{user}")
    public Result<Map<String, String>> getProgress(@PathVariable String user) {
        log.info("getProgress, user: {}", user);
        Map<String, String> result = new HashMap<>();
        try {
            User u = userMapper.selectById(user);
            if (u == null) {
                return Result.error(404, "User not found");
            }

            String studying = u.getStudying();
            if (studying == null || studying.equals("none") || studying.isEmpty()) {
                return Result.error(404, "User is not studying any book");
            }

            int studied = u.getStudied();
            WordBook book = wordBookMapper.selectById(studying);
            if (book == null) {
                return Result.error(404, "Book not found");
            }
            
            int total = book.getNum();
            
            result.put("studied", String.valueOf(studied));
            result.put("toStudy", String.valueOf(total - studied));
            result.put("day1", String.valueOf(u.getDay1()));
            result.put("day2", String.valueOf(u.getDay2()));
            result.put("day3", String.valueOf(u.getDay3()));
            result.put("day4", String.valueOf(u.getDay4()));
            result.put("day5", String.valueOf(u.getDay5()));
            result.put("day6", String.valueOf(u.getDay6()));
            result.put("day7", String.valueOf(u.getDay7()));
            result.put("info", "success");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("getProgress error", e);
            return Result.error("Failed to get progress");
        }
    }
}
