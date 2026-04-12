package example.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import example.common.Result;
import example.dao.DailyMapper;
import example.dao.UserMapper;
import example.dao.UserWordProgressMapper;
import example.dao.WordBookMapper;
import example.dao.WordEntryMapper;
import example.dto.TestQuestionDto;
import example.dto.TestSubmitItemDto;
import example.dto.TestSubmitRequestDto;
import example.dto.TestSubmitResponseDto;
import example.dto.TestSubmitResultItemDto;
import example.pojo.Daily;
import example.pojo.User;
import example.pojo.WordBook;
import example.pojo.WordEntry;
import example.service.UserWordProgressService;
import jakarta.servlet.http.HttpServletRequest;

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

    @Autowired
    private WordBookMapper wordBookMapper;

    @Autowired
    private UserWordProgressMapper userWordProgressMapper;

    @Autowired
    private UserWordProgressService userWordProgressService;

    public static class ReviewMetaDto {
        private String origin;
        private Integer dueCount;
        private String nextReviewAt;

        public String getOrigin() {
            return origin;
        }

        public void setOrigin(String origin) {
            this.origin = origin;
        }

        public Integer getDueCount() {
            return dueCount;
        }

        public void setDueCount(Integer dueCount) {
            this.dueCount = dueCount;
        }

        public String getNextReviewAt() {
            return nextReviewAt;
        }

        public void setNextReviewAt(String nextReviewAt) {
            this.nextReviewAt = nextReviewAt;
        }
    }

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
    public Result<List<TableEntry>> getDaily(@PathVariable String user, @RequestParam(required = false) String date) {
        log.info("getDaily, user: {}, date: {}", user, date);
        try {
            User u = userMapper.selectById(user);
            if (u == null) {
                return Result.error(404, "User not found");
            }

            String queryDate = date;
            if (queryDate == null || queryDate.isEmpty()) {
                TimeZone timeZone = TimeZone.getTimeZone("Asia/Shanghai");
                Calendar now = Calendar.getInstance(timeZone);
                queryDate = String.format(
                        "%04d-%02d-%02d",
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH) + 1,
                        now.get(Calendar.DAY_OF_MONTH));
            }

            QueryWrapper<Daily> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user).eq("review_date", queryDate);
            List<Daily> list = dailyMapper.selectList(queryWrapper);

            List<TableEntry> result = new ArrayList<>();
            for (Daily d : list) {
                String origin = d.getOrigin();
                String poses = origin == null || origin.isEmpty() ? null
                        : wordEntryMapper.selectPosesByIdAndTable(d.getId(), origin);
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

    @GetMapping("/get_dates/{user}")
    public Result<List<String>> getDates(@PathVariable String user) {
        log.info("getDates, user: {}", user);
        try {
            User u = userMapper.selectById(user);
            if (u == null) {
                return Result.error(404, "User not found");
            }
            return Result.success(dailyMapper.selectReviewDatesDesc(user));
        } catch (Exception e) {
            log.error("getDates error", e);
            return Result.error("Failed to get review dates");
        }
    }

    @GetMapping("/generate")
    public Result<List<TestQuestionDto>> generate(HttpServletRequest request,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) Integer count) {
        try {
            Object authedUsername = request.getAttribute("username");
            if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
                return Result.error(401, "Unauthorized");
            }
            String username = (String) authedUsername;

            User user = userMapper.selectById(username);
            if (user == null) {
                return Result.error(404, "User not found");
            }

            String studying = user.getStudying();
            if (origin == null || origin.isBlank()) {
                origin = studying;
            }
            if (origin == null || origin.isBlank() || "none".equals(origin)) {
                return Result.error(400, "User is not studying any book");
            }
            if (!origin.matches("^[a-zA-Z0-9_]+$")) {
                return Result.error(400, "Invalid table name");
            }
            if (studying == null || studying.isBlank() || !origin.equals(studying)) {
                return Result.error(400, "请先切换到该词书再进行复习");
            }

            int requestedCount = count == null ? 20 : count;
            int effectiveCount = Math.min(Math.max(requestedCount, 1), 50);

            WordBook wordBook = wordBookMapper.selectById(origin);
            if (wordBook == null) {
                return Result.error(404, "Book not found");
            }

            int studied = Math.max(user.getStudied(), 0);
            int bookSize = Math.max(wordBook.getNum(), 0);
            int maxLearnedId = Math.min(studied, bookSize);
            if (maxLearnedId < 4) {
                return Result.error(400, maxLearnedId == 0 ? "您还未学习任何单词" : "已学单词不足4个，无法生成复习");
            }

            LocalDateTime now = LocalDateTime.now();
            List<UserWordProgressMapper.DueRow> dueRows = userWordProgressMapper.selectDue(username, origin, now,
                    effectiveCount);
            if (dueRows == null || dueRows.isEmpty()) {
                return Result.success(new ArrayList<>());
            }

            int poolLimit = Math.min(Math.max(maxLearnedId, 4), 100);
            List<String> poolRaw = wordEntryMapper.selectRandomWordsByMaxId(maxLearnedId, poolLimit, origin);
            List<String> pool = normalizePool(poolRaw);
            if (pool.size() < 4) {
                return Result.error(400, "已学单词不足4个，无法生成复习");
            }

            List<TestQuestionDto> questions = new ArrayList<>(dueRows.size());
            for (int i = 0; i < dueRows.size(); i++) {
                UserWordProgressMapper.DueRow r = dueRows.get(i);
                if (r == null || r.getWordId() == null || r.getWordId() <= 0) {
                    continue;
                }
                WordEntry entry = wordEntryMapper.selectByIdAndTable(r.getWordId(), origin);
                if (entry == null || entry.getWord() == null || entry.getWord().isBlank()) {
                    continue;
                }

                String poses = entry.getPoses();
                String correctWord = entry.getWord();

                Set<String> optionsSet = new HashSet<>();
                optionsSet.add(correctWord);
                fillDistractorsFromPool(optionsSet, pool, 3);
                if (optionsSet.size() < 4) {
                    return Result.error(500, "Failed to generate review");
                }

                List<String> options = new ArrayList<>(optionsSet);
                Collections.shuffle(options);

                TestQuestionDto dto = new TestQuestionDto();
                dto.setId(entry.getId());
                dto.setPoses(poses == null ? "" : poses);
                dto.setOptions(options);
                dto.setAnswer(correctWord);
                questions.add(dto);
            }

            return Result.success(questions);
        } catch (Exception e) {
            log.error("generate review error", e);
            return Result.error("Failed to generate review");
        }
    }

    @GetMapping("/meta")
    public Result<ReviewMetaDto> meta(HttpServletRequest request,
            @RequestParam(required = false) String origin) {
        try {
            Object authedUsername = request.getAttribute("username");
            if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
                return Result.error(401, "Unauthorized");
            }
            String username = (String) authedUsername;

            User user = userMapper.selectById(username);
            if (user == null) {
                return Result.error(404, "User not found");
            }

            String studying = user.getStudying();
            if (origin == null || origin.isBlank()) {
                origin = studying;
            }
            if (origin == null || origin.isBlank() || "none".equals(origin)) {
                return Result.error(400, "User is not studying any book");
            }
            if (!origin.matches("^[a-zA-Z0-9_]+$")) {
                return Result.error(400, "Invalid table name");
            }
            if (studying == null || studying.isBlank() || !origin.equals(studying)) {
                return Result.error(400, "请先切换到该词书再进行复习");
            }

            LocalDateTime now = LocalDateTime.now();
            int dueCount = userWordProgressMapper.countDue(username, origin, now);
            LocalDateTime next = userWordProgressMapper.selectNextReviewAt(username, origin);

            ReviewMetaDto dto = new ReviewMetaDto();
            dto.setOrigin(origin);
            dto.setDueCount(dueCount);
            if (next != null) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                dto.setNextReviewAt(next.format(df));
            } else {
                dto.setNextReviewAt(null);
            }
            return Result.success(dto);
        } catch (Exception e) {
            log.error("review meta error", e);
            return Result.error("Failed to load review meta");
        }
    }

    @GetMapping("/pending_count")
    public Result<Map<String, Integer>> pendingCount(HttpServletRequest request) {
        try {
            Object authedUsername = request.getAttribute("username");
            if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
                return Result.error(401, "Unauthorized");
            }
            String username = (String) authedUsername;

            LocalDateTime now = LocalDateTime.now();
            int cnt = userWordProgressMapper.countDueAll(username, now);
            Map<String, Integer> out = new LinkedHashMap<>();
            out.put("count", cnt);
            return Result.success(out);
        } catch (Exception e) {
            log.error("pending count error", e);
            return Result.error("Failed to load pending count");
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
                return Result.error(400, "请先切换到该词书再进行复习");
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
                userWordProgressService.recordResult(username, origin, wordId, answer, correct, "review");

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
            log.error("submit review error", e);
            return Result.error("Failed to submit review");
        }
    }

    private static List<String> normalizePool(List<String> words) {
        if (words == null || words.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> set = new HashSet<>();
        for (String w : words) {
            if (w == null) {
                continue;
            }
            String s = w.trim();
            if (s.isEmpty()) {
                continue;
            }
            set.add(s);
        }
        return new ArrayList<>(set);
    }

    private static void fillDistractorsFromPool(Set<String> optionsSet, List<String> pool, int need) {
        if (pool == null || pool.isEmpty()) {
            return;
        }
        int attempts = 0;
        int maxAttempts = Math.max(50, need * 50);
        while (optionsSet.size() < 1 + need && attempts < maxAttempts) {
            attempts++;
            String candidate = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            optionsSet.add(candidate);
        }
    }
}
