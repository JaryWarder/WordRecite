package example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import example.common.Result;
import example.dao.PrivateBooksMapper;
import example.dao.WordEntryMapper;
import example.pojo.PrivateBooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/private")
public class PrivateTableController {
    private static final Logger log = LoggerFactory.getLogger(PrivateTableController.class);

    @Autowired
    private PrivateBooksMapper privateBooksMapper;

    @Autowired
    private WordEntryMapper wordEntryMapper;

    public static class MyEntry {
        private String word;
        private String poses;

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
    }

    @GetMapping("/get_private/{user}")
    public Result<List<MyEntry>> getPrivate(@PathVariable String user) {
        log.info("getPrivate, user: {}", user);
        try {
            QueryWrapper<PrivateBooks> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user);
            List<PrivateBooks> list = privateBooksMapper.selectList(queryWrapper);

            List<MyEntry> result = new ArrayList<>();
            for (PrivateBooks w : list) {
                String poses = wordEntryMapper.selectPosesByIdAndTable(w.getId(), w.getOrigin());
                if (poses != null && poses.length() > 50) {
                    poses = poses.substring(0, 49) + "...";
                }
                
                MyEntry e = new MyEntry();
                e.setWord(w.getWord());
                e.setPoses(poses);
                result.add(e);
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("getPrivate error", e);
            return Result.error("Failed to get private books");
        }
    }

    @GetMapping("/delete_private")
    public Result<String> deletePrivate(@RequestParam String obj) {
        log.info("deletePrivate, obj: {}", obj);
        try {
            JSONObject json = JSON.parseObject(obj);
            String word = json.getString("word");
            String username = json.getString("username");

            QueryWrapper<PrivateBooks> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username).eq("word", word);
            
            privateBooksMapper.delete(queryWrapper);
            
            return Result.success("success");
        } catch (Exception e) {
            log.error("deletePrivate error", e);
            return Result.error("Failed to delete private word");
        }
    }
}
