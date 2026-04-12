package example.service;

import example.common.Result;
import example.dao.UserMapper;
import example.dao.UserWordEventMapper;
import example.dao.UserWordProgressMapper;
import example.dao.WordBookMapper;
import example.dto.DueWordDto;
import example.dto.ProgressDashboardDto;
import example.pojo.User;
import example.pojo.WordBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProgressAnalyticsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WordBookMapper wordBookMapper;

    @Autowired
    private UserWordProgressMapper userWordProgressMapper;

    @Autowired
    private UserWordEventMapper userWordEventMapper;

    public Result<ProgressDashboardDto> dashboard(String username, String origin, Integer days) {
        User user = userMapper.selectById(username);
        if (user == null) {
            return Result.error(404, "User not found");
        }
        if (origin == null || origin.isBlank()) {
            origin = user.getStudying();
        }
        if (origin == null || origin.isBlank() || "none".equals(origin)) {
            return Result.error(400, "User is not studying any book");
        }
        if (!origin.matches("^[a-zA-Z0-9_]+$")) {
            return Result.error(400, "Invalid table name");
        }

        WordBook wordBook = wordBookMapper.selectById(origin);
        if (wordBook == null) {
            return Result.error(404, "Book not found");
        }

        int d = days == null ? 30 : days;
        d = Math.min(Math.max(d, 1), 180);

        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate = now.toLocalDate().minusDays(d - 1L);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = now.toLocalDate().plusDays(1).atStartOfDay();

        int learnedUnique = userWordProgressMapper.countAll(username, origin);
        int masteredCount = userWordProgressMapper.countByMinStage(username, origin, 4);
        int dueCount = userWordProgressMapper.countDue(username, origin, now);

        List<ProgressDashboardDto.StageDistItem> stageDist = new ArrayList<>();
        Map<Integer, Integer> stageMap = new HashMap<>();
        for (UserWordProgressMapper.StageCountRow row : userWordProgressMapper.selectStageCounts(username, origin)) {
            int stage = row.getStage() == null ? 0 : row.getStage();
            int cnt = row.getCnt() == null ? 0 : row.getCnt();
            stageMap.put(stage, cnt);
        }
        for (int s = 0; s <= 5; s++) {
            ProgressDashboardDto.StageDistItem item = new ProgressDashboardDto.StageDistItem();
            item.setStage(s);
            item.setCount(stageMap.getOrDefault(s, 0));
            stageDist.add(item);
        }

        Map<LocalDate, UserWordEventMapper.DailyAggRow> aggMap = new HashMap<>();
        for (UserWordEventMapper.DailyAggRow row : userWordEventMapper.selectDailyAgg(username, origin, start, end)) {
            if (row.getD() != null) {
                aggMap.put(row.getD(), row);
            }
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<ProgressDashboardDto.DailyCount> dailyLearned = new ArrayList<>();
        List<ProgressDashboardDto.DailyCount> dailyReviewed = new ArrayList<>();
        List<ProgressDashboardDto.DailyAccuracy> dailyAccuracy = new ArrayList<>();

        for (int i = 0; i < d; i++) {
            LocalDate day = startDate.plusDays(i);
            UserWordEventMapper.DailyAggRow row = aggMap.get(day);

            int learned = row == null || row.getLearned() == null ? 0 : row.getLearned();
            int reviewed = row == null || row.getReviewed() == null ? 0 : row.getReviewed();
            int correct = row == null || row.getCorrect() == null ? 0 : row.getCorrect();
            int total = row == null || row.getTotal() == null ? 0 : row.getTotal();

            ProgressDashboardDto.DailyCount dl = new ProgressDashboardDto.DailyCount();
            dl.setDate(day.format(df));
            dl.setCount(learned);
            dailyLearned.add(dl);

            ProgressDashboardDto.DailyCount dr = new ProgressDashboardDto.DailyCount();
            dr.setDate(day.format(df));
            dr.setCount(reviewed);
            dailyReviewed.add(dr);

            ProgressDashboardDto.DailyAccuracy da = new ProgressDashboardDto.DailyAccuracy();
            da.setDate(day.format(df));
            da.setAccuracy(total <= 0 ? 0.0 : (double) correct / (double) total);
            dailyAccuracy.add(da);
        }

        ProgressDashboardDto.Series series = new ProgressDashboardDto.Series();
        series.setDailyLearned(dailyLearned);
        series.setDailyReviewed(dailyReviewed);
        series.setDailyAccuracy(dailyAccuracy);

        ProgressDashboardDto dto = new ProgressDashboardDto();
        dto.setOrigin(origin);
        dto.setBookSize(wordBook.getNum());
        dto.setLearnedUnique(learnedUnique);
        dto.setMasteredCount(masteredCount);
        dto.setDueCount(dueCount);
        dto.setStageDist(stageDist);
        dto.setSeries(series);

        return Result.success(dto);
    }

    public Result<List<DueWordDto>> due(String username, String origin, Integer limit) {
        User user = userMapper.selectById(username);
        if (user == null) {
            return Result.error(404, "User not found");
        }
        if (origin == null || origin.isBlank()) {
            origin = user.getStudying();
        }
        if (origin == null || origin.isBlank() || "none".equals(origin)) {
            return Result.error(400, "User is not studying any book");
        }
        if (!origin.matches("^[a-zA-Z0-9_]+$")) {
            return Result.error(400, "Invalid table name");
        }
        if (wordBookMapper.selectById(origin) == null) {
            return Result.error(404, "Book not found");
        }

        int l = limit == null ? 50 : limit;
        l = Math.min(Math.max(l, 1), 200);
        LocalDateTime now = LocalDateTime.now();

        List<DueWordDto> out = new ArrayList<>();
        for (UserWordProgressMapper.DueRow r : userWordProgressMapper.selectDue(username, origin, now, l)) {
            DueWordDto dto = new DueWordDto();
            dto.setWordId(r.getWordId() == null ? 0 : r.getWordId());
            dto.setWord(r.getWord());
            dto.setStage(r.getStage() == null ? 0 : r.getStage());
            dto.setNextReviewAt(r.getNextReviewAt());
            out.add(dto);
        }
        return Result.success(out);
    }
}
