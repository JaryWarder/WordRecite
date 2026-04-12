package example.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import example.common.Result;
import example.dao.UserMapper;
import example.dao.WordEntryMapper;
import example.dto.TestQuestionDto;
import example.dto.TestSubmitItemDto;
import example.dto.TestSubmitRequestDto;
import example.dto.TestSubmitResponseDto;
import example.dto.TestSubmitResultItemDto;
import example.pojo.User;
import example.service.TestService;
import example.service.UserWordProgressService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/test")
public class TestController {
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestService testService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WordEntryMapper wordEntryMapper;

    @Autowired
    private UserWordProgressService userWordProgressService;

    @GetMapping("/generate")
    public Result<List<TestQuestionDto>> generate(@RequestParam String username,
            @RequestParam String bookTitle,
            @RequestParam Integer count,
            HttpServletRequest request) {
        try {
            if (username == null || username.isBlank()) {
                return Result.error(400, "Missing username");
            }
            if (bookTitle == null || bookTitle.isBlank()) {
                return Result.error(400, "Missing bookTitle");
            }
            if (count == null) {
                return Result.error(400, "Missing count");
            }
            if (!(count == 10 || count == 20 || count == 30)) {
                return Result.error(400, "Invalid count, only supports 10/20/30");
            }
            if (!bookTitle.matches("^[a-zA-Z0-9_]+$")) {
                return Result.error(400, "Invalid table name");
            }

            Object authedUsername = request.getAttribute("username");
            if (authedUsername instanceof String && !username.equals(authedUsername)) {
                return Result.error(403, "Forbidden");
            }

            return testService.generatePaper(username, bookTitle, count);
        } catch (Exception e) {
            log.error("generate test error", e);
            return Result.error("Failed to generate test");
        }
    }

    @PostMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    public Result<TestSubmitResponseDto> submit(@RequestBody TestSubmitRequestDto body, HttpServletRequest request) {
        try {
            Object authedUsername = request.getAttribute("username");
            if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
                return Result.error(401, "Unauthorized");
            }
            String username = (String) authedUsername;

            if (body == null) {
                return Result.error(400, "Missing request body");
            }
            String origin = body.getBookTitle();
            if (origin == null || origin.isBlank()) {
                return Result.error(400, "Missing bookTitle");
            }
            if (!origin.matches("^[a-zA-Z0-9_]+$")) {
                return Result.error(400, "Invalid table name");
            }

            User user = userMapper.selectById(username);
            if (user == null) {
                return Result.error(404, "User not found");
            }
            if (user.getStudying() == null || user.getStudying().isBlank() || !origin.equals(user.getStudying())) {
                return Result.error(400, "请先切换到该词书再进行测试");
            }

            List<TestSubmitItemDto> rawHistory = body.getHistory();
            if (rawHistory == null || rawHistory.isEmpty()) {
                return Result.error(400, "Missing history");
            }

            Map<Integer, String> chosenByWordId = new LinkedHashMap<>();
            for (int i = 0; i < rawHistory.size(); i++) {
                TestSubmitItemDto item = rawHistory.get(i);
                if (item == null) {
                    continue;
                }
                Integer wordId = item.getWordId();
                if (wordId == null || wordId <= 0) {
                    return Result.error(400, "Invalid wordId");
                }
                String chosen = item.getChosen();
                if (chosen == null || chosen.isBlank()) {
                    return Result.error(400, "Missing chosen");
                }
                chosenByWordId.put(wordId, chosen);
            }

            List<TestSubmitResultItemDto> results = new ArrayList<>(chosenByWordId.size());
            int correctCount = 0;
            for (Map.Entry<Integer, String> e : chosenByWordId.entrySet()) {
                Integer wordId = e.getKey();
                String chosen = e.getValue();
                String answer = wordEntryMapper.selectWordByIdAndTable(wordId, origin);
                if (answer == null || answer.isBlank()) {
                    return Result.error(400, "Invalid wordId");
                }

                boolean correct = chosen.equals(answer);
                userWordProgressService.recordTestResult(username, origin, wordId, answer, correct);

                TestSubmitResultItemDto dto = new TestSubmitResultItemDto();
                dto.setWordId(wordId);
                dto.setChosen(chosen);
                dto.setAnswer(answer);
                dto.setCorrect(correct);
                results.add(dto);

                if (correct) {
                    correctCount++;
                }
            }

            TestSubmitResponseDto resp = new TestSubmitResponseDto();
            resp.setOrigin(origin);
            resp.setTotal(results.size());
            resp.setCorrectCount(correctCount);
            resp.setWrongCount(Math.max(0, results.size() - correctCount));
            resp.setHistory(results);
            return Result.success(resp);
        } catch (Exception e) {
            log.error("submit test error", e);
            return Result.error("Failed to submit test");
        }
    }
}
