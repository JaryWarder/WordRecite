package example.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import example.common.Result;
import example.dao.UserMapper;
import example.dao.WordBookMapper;
import example.dao.WordEntryMapper;
import example.dto.TestQuestionDto;
import example.pojo.User;
import example.pojo.WordBook;
import example.pojo.WordEntry;

@Service
public class TestService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WordBookMapper wordBookMapper;

    @Autowired
    private WordEntryMapper wordEntryMapper;

    public Result<List<TestQuestionDto>> generatePaper(String username, String bookTitle, int requestedCount) {
        User user = userMapper.selectById(username);
        if (user == null) {
            return Result.error(404, "User not found");
        }

        if (user.getStudying() == null || !bookTitle.equals(user.getStudying())) {
            return Result.error(400, "请先切换到该词书再进行测试");
        }

        int studied = Math.max(user.getStudied(), 0);
        if (studied < 4) {
            return Result.error(400, studied == 0 ? "您还未学习任何单词" : "已学单词不足4个，无法生成测试");
        }

        WordBook wordBook = wordBookMapper.selectById(bookTitle);
        if (wordBook == null) {
            return Result.error(404, "Word book not found");
        }

        int bookSize = Math.max(wordBook.getNum(), 0);
        int maxLearnedId = Math.min(studied, bookSize);
        if (maxLearnedId <= 0) {
            return Result.error(400, "您还未学习任何单词");
        }

        int available = wordEntryMapper.countByMaxIdAndTable(maxLearnedId, bookTitle);
        int effectiveCount = Math.min(Math.max(requestedCount, 1), Math.min(maxLearnedId, available));
        if (effectiveCount <= 0) {
            return Result.error(400, "您还未学习任何单词");
        }

        int poolLimit = Math.min(Math.max(maxLearnedId, 4), 100);
        List<String> poolRaw = wordEntryMapper.selectRandomWordsByMaxId(maxLearnedId, poolLimit, bookTitle);
        List<String> pool = normalizePool(poolRaw);
        if (pool.size() < 4) {
            return Result.error(400, "已学单词不足4个，无法生成测试");
        }

        List<WordEntry> correctEntries = wordEntryMapper.selectRandomEntriesByMaxIdAndTable(maxLearnedId,
                effectiveCount, bookTitle);
        if (correctEntries == null || correctEntries.isEmpty()) {
            return Result.error(500, "Failed to generate test");
        }

        List<TestQuestionDto> questions = new ArrayList<>(correctEntries.size());
        for (int i = 0; i < correctEntries.size(); i++) {
            WordEntry entry = correctEntries.get(i);
            if (entry == null || entry.getWord() == null || entry.getWord().isBlank()) {
                continue;
            }

            String poses = entry.getPoses();
            String correctWord = entry.getWord();

            Set<String> optionsSet = new HashSet<>();
            optionsSet.add(correctWord);

            fillDistractorsFromPool(optionsSet, pool, 3);
            if (optionsSet.size() < 4) {
                return Result.error(500, "Failed to generate test");
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

        if (questions.isEmpty()) {
            return Result.error(500, "Failed to generate test");
        }

        return Result.success(questions);
    }

    private static List<String> normalizePool(List<String> words) {
        if (words == null || words.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> set = new HashSet<>();
        for (String w : words) {
            if (w == null)
                continue;
            String s = w.trim();
            if (s.isEmpty())
                continue;
            set.add(s);
        }
        return new ArrayList<>(set);
    }

    private static void fillDistractorsFromPool(Set<String> optionsSet, List<String> pool, int need) {
        if (pool == null || pool.isEmpty())
            return;
        int attempts = 0;
        int maxAttempts = Math.max(50, need * 50);
        while (optionsSet.size() < 1 + need && attempts < maxAttempts) {
            attempts++;
            String candidate = pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
            if (candidate == null || candidate.isBlank())
                continue;
            optionsSet.add(candidate);
        }
    }
}
