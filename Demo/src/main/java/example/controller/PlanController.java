package example.controller;

import example.common.Result;
import example.dao.UserMapper;
import example.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plan")
public class PlanController {
    private static final Logger log = LoggerFactory.getLogger(PlanController.class);

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/submit_plan")
    public Result<String> submitPlan(@RequestParam String user, @RequestParam int numWords) {
        log.info("submitPlan, user: {}, plan: {}", user, numWords);
        try {
            User u = userMapper.selectById(user);
            if (u == null) {
                return Result.error(404, "User not found");
            }
            
            u.setPlan(numWords);
            userMapper.updateById(u);
            
            return Result.success("success");
        } catch (Exception e) {
            log.error("submitPlan error", e);
            return Result.error("Failed to submit plan");
        }
    }
}
