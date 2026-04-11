package example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import example.common.Result;
import example.dao.PrivateBooksMapper;
import example.pojo.PrivateBooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/private")
public class AddPrivateController {
    private static final Logger log = LoggerFactory.getLogger(AddPrivateController.class);

    @Autowired
    private PrivateBooksMapper privateBooksMapper;

    @GetMapping("/add_private")
    public Result<String> addPrivate(@RequestParam String obj) {
        log.info("addPrivate, received: {}", obj);
        try {
            JSONObject json = JSON.parseObject(obj);
            String studying = json.getString("studying");
            String word = json.getString("word");
            String username = json.getString("username");
            int id = json.getIntValue("id");

            QueryWrapper<PrivateBooks> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username).eq("word", word);
            
            PrivateBooks existEntry = privateBooksMapper.selectOne(queryWrapper);
            if (existEntry == null) {
                PrivateBooks newBook = new PrivateBooks();
                newBook.setUsername(username);
                newBook.setOrigin(studying);
                newBook.setId(id);
                newBook.setWord(word);
                
                privateBooksMapper.insert(newBook);
                return Result.success("success");
            } else {
                return Result.success("duplicate");
            }
        } catch (Exception e) {
            log.error("addPrivate error", e);
            return Result.error("Failed to add private word");
        }
    }
}
