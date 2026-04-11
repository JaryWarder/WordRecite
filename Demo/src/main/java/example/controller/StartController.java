package example.controller;

import example.common.Result;
import example.dao.UserMapper;
import example.dao.WordBookMapper;
import example.pojo.User;
import example.pojo.WordBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/start")
public class StartController {
    private static final Logger log = LoggerFactory.getLogger(StartController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WordBookMapper wordBookMapper;

    @GetMapping("/start_info/{user}")
    public Result<Map<String, String>> startInfo(@PathVariable String user) {
        log.info("startInfo, user: {}", user);
        Map<String, String> resultMap = new HashMap<>();
        try {
            User u = userMapper.selectById(user);
            if (u == null) {
                return Result.error(404, "User not found");
            }
            String studying = u.getStudying();
            if (studying == null || studying.equals("none") || studying.isEmpty()) {
                resultMap.put("info", "no_book");
                return Result.success(resultMap);
            }
            WordBook book = wordBookMapper.selectById(studying);
            if (book == null) {
                return Result.error(404, "Book not found");
            }
            int num = book.getNum();
            int plan = u.getPlan();
            int studied = u.getStudied();
            resultMap.put("num", String.valueOf(num));
            resultMap.put("plan", String.valueOf(plan));
            resultMap.put("studying", studying);
            resultMap.put("studied", String.valueOf(studied));
            resultMap.put("info", "success");
            return Result.success(resultMap);
        } catch (Exception e) {
            log.error("startInfo error", e);
            return Result.error("Failed to get start info");
        }
    }
}
