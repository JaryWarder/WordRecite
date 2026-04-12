package example.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import example.common.Result;
import example.dao.DailyMapper;
import example.dao.UserMapper;
import example.dao.WordBookMapper;
import example.dao.WordEntryMapper;
import example.dto.DailyWordItemDto;
import example.dto.DueWordDto;
import example.dto.ProgressDashboardDto;
import example.pojo.Daily;
import example.pojo.User;
import example.pojo.WordBook;
import example.service.ProgressAnalyticsService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {
    private static final Logger log = LoggerFactory.getLogger(ProgressController.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WordBookMapper wordBookMapper;

    @Autowired
    private DailyMapper dailyMapper;

    @Autowired
    private WordEntryMapper wordEntryMapper;

    @Autowired
    private ProgressAnalyticsService progressAnalyticsService;

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

    @GetMapping("/dashboard")
    public Result<ProgressDashboardDto> dashboard(HttpServletRequest request,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) Integer days) {
        Object authedUsername = request.getAttribute("username");
        if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
            return Result.error(401, "Unauthorized");
        }
        return progressAnalyticsService.dashboard((String) authedUsername, origin, days);
    }

    @GetMapping("/due")
    public Result<List<DueWordDto>> due(HttpServletRequest request,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) Integer limit) {
        Object authedUsername = request.getAttribute("username");
        if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
            return Result.error(401, "Unauthorized");
        }
        return progressAnalyticsService.due((String) authedUsername, origin, limit);
    }

    @GetMapping("/daily_list")
    public Result<List<DailyWordItemDto>> dailyList(HttpServletRequest request,
            @RequestParam String date) {
        Object authedUsername = request.getAttribute("username");
        if (!(authedUsername instanceof String) || ((String) authedUsername).isBlank()) {
            return Result.error(401, "Unauthorized");
        }
        if (date == null || date.isBlank() || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return Result.error(400, "Invalid date");
        }

        String username = (String) authedUsername;
        List<Daily> rows = dailyMapper.selectByUserAndDate(username, date);
        List<DailyWordItemDto> out = new ArrayList<>();
        if (rows == null || rows.isEmpty()) {
            return Result.success(out);
        }

        for (Daily d : rows) {
            if (d == null) {
                continue;
            }
            String origin = d.getOrigin();
            Integer wordId = d.getId();
            if (origin == null || origin.isBlank() || !origin.matches("^[a-zA-Z0-9_]+$") || wordId == null
                    || wordId <= 0) {
                continue;
            }
            String poses = wordEntryMapper.selectPosesByIdAndTable(wordId, origin);
            DailyWordItemDto dto = new DailyWordItemDto();
            dto.setWordId(wordId);
            dto.setWord(d.getWord());
            dto.setPoses(poses == null ? "" : poses);
            dto.setOrigin(origin);
            dto.setStatus(d.getStatus());
            out.add(dto);
        }
        return Result.success(out);
    }
}
