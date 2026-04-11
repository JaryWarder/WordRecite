package example.controller;

import example.common.Result;
import example.dao.DailyMapper;
import example.dao.UserMapper;
import example.dao.WordEntryMapper;
import example.pojo.Daily;
import example.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewTableController {
    private static final Logger log = LoggerFactory.getLogger(ReviewTableController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DailyMapper dailyMapper;

    @Autowired
    private WordEntryMapper wordEntryMapper;

    public static class TableEntry {
        private String word;
        private String poses;
        private String status;

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public String getPoses() {
            return poses;
        }

        public void setPoses(String poses) {
            this.poses = poses;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 获取用户今日打卡的复习表列表
     */
    @GetMapping("/get_daily/{user}")
    public Result<List<TableEntry>> getDaily(@PathVariable String user) {
        log.info("getDaily, user: {}", user);
        try {
            User u = userMapper.selectById(user);
            if (u == null || u.getStudying() == null || u.getStudying().equals("none")) {
                return Result.success(new ArrayList<>());
            }

            String studying = u.getStudying();
            QueryWrapper<Daily> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user);
            List<Daily> list = dailyMapper.selectList(queryWrapper);

            List<TableEntry> result = new ArrayList<>();
            for (Daily d : list) {
                String poses = wordEntryMapper.selectPosesByIdAndTable(d.getId(), studying);
                if (poses != null && poses.length() > 40) {
                    poses = poses.substring(0, 39) + "...";
                }
                
                TableEntry e = new TableEntry();
                e.setWord(d.getWord());
                e.setPoses(poses);
                e.setStatus(d.getStatus());
                result.add(e);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("getDaily error", e);
            return Result.error("Failed to get daily review table");
        }
    }
}
