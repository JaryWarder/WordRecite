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

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class LoadBooksController {
    private static final Logger log = LoggerFactory.getLogger(LoadBooksController.class);

    @Autowired
    private WordBookMapper wordBookMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取所有可用的词库列表
     */
    @GetMapping("/get_books/{user}")
    public Result<List<WordBook>> getWordBooks(@PathVariable String user) {
        try {
            List<WordBook> list = wordBookMapper.selectList(null);
            
            // TODO: 未来补充生词本数量逻辑，目前暂返回基础列表
            WordBook privateBook = new WordBook();
            privateBook.setTitle("Private");
            privateBook.setNum(0); // 暂定为 0
            list.add(privateBook);
            
            return Result.success(list);
        } catch (Exception e) {
            log.error("getWordBooks error", e);
            return Result.error("Failed to get word books");
        }
    }

    /**
     * 获取当前用户正在学习的词库详情
     */
    @GetMapping("/get_book/{user}")
    public Result<WordBook> getBook(@PathVariable String user) {
        try {
            User u = userMapper.selectById(user);
            if (u == null || u.getStudying() == null || u.getStudying().equals("none") || u.getStudying().isEmpty()) {
                return Result.error(404, "No studying book");
            }
            WordBook book = wordBookMapper.selectById(u.getStudying());
            return Result.success(book);
        } catch (Exception e) {
            log.error("getBook error", e);
            return Result.error("Failed to get studying book");
        }
    }
}
