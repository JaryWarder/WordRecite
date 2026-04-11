package example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import example.common.Result;
import example.dao.WordEntryMapper;
import example.pojo.WordEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/word")
public class WordInfoController {
    private static final Logger log = LoggerFactory.getLogger(WordInfoController.class);

    @Autowired
    private WordEntryMapper wordEntryMapper;

    @GetMapping("/get_wordInfo")
    public Result<WordEntry> getWordInfo(@RequestParam String obj) {
        try {
            JSONObject json = JSON.parseObject(obj);
            String title = json.getString("title");
            Integer id = json.getInteger("id");

            if (title == null || id == null) {
                return Result.error(400, "Missing title or id");
            }

            // 保护措施：防止SQL注入，表名只能是英文字母和数字下划线
            if (!title.matches("^[a-zA-Z0-9_]+$")) {
                return Result.error(400, "Invalid table name");
            }

            WordEntry entry = wordEntryMapper.selectByIdAndTable(id, title);
            if (entry != null) {
                return Result.success(entry);
            } else {
                return Result.error(404, "Word not found");
            }
        } catch (Exception e) {
            log.error("getWordInfo error", e);
            return Result.error("Failed to get word info");
        }
    }
}
