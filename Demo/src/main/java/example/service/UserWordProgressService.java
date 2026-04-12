package example.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import example.dao.UserWordEventMapper;
import example.dao.UserWordProgressMapper;
import example.pojo.UserWordEvent;
import example.pojo.UserWordProgress;

@Service
public class UserWordProgressService {

    @Autowired
    private UserWordProgressMapper userWordProgressMapper;

    @Autowired
    private UserWordEventMapper userWordEventMapper;

    public void recordLearnResult(String username, String origin, Integer wordId, String word, boolean correct) {
        recordResult(username, origin, wordId, word, correct, "learn");
    }

    public void recordTestResult(String username, String origin, Integer wordId, String word, boolean correct) {
        recordResult(username, origin, wordId, word, correct, "test");
    }

    public void recordResult(String username, String origin, Integer wordId, String word, boolean correct,
            String eventType) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (origin == null || origin.isBlank() || "none".equals(origin)) {
            return;
        }
        if (wordId == null || wordId <= 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        QueryWrapper<UserWordProgress> qw = new QueryWrapper<>();
        qw.eq("username", username).eq("origin", origin).eq("word_id", wordId);
        UserWordProgress p = userWordProgressMapper.selectOne(qw);
        if (p == null) {
            p = new UserWordProgress();
            p.setUsername(username);
            p.setOrigin(origin);
            p.setWordId(wordId);
            p.setWord(word == null ? "" : word);
            p.setStage(0);
            p.setCorrectCnt(0);
            p.setWrongCnt(0);
            p.setStreak(0);
            p.setLastResult(null);
            p.setFirstSeenAt(now);
            p.setLastSeenAt(now);
            p.setNextReviewAt(null);
            userWordProgressMapper.insert(p);
        }

        Integer prevStage = p.getStage() == null ? 0 : p.getStage();
        Integer prevCorrect = p.getCorrectCnt() == null ? 0 : p.getCorrectCnt();
        Integer prevWrong = p.getWrongCnt() == null ? 0 : p.getWrongCnt();
        Integer prevStreak = p.getStreak() == null ? 0 : p.getStreak();

        if (correct) {
            int newStage = Math.min(prevStage + 1, 5);
            p.setStage(newStage);
            p.setCorrectCnt(prevCorrect + 1);
            p.setStreak(prevStreak + 1);
            p.setLastResult(1);
            p.setNextReviewAt(now.plusSeconds(intervalSecondsForStage(newStage)));
        } else {
            p.setStage(0);
            p.setWrongCnt(prevWrong + 1);
            p.setStreak(0);
            p.setLastResult(0);
            p.setNextReviewAt(now.plusMinutes(10));
        }

        if (word != null && !word.isBlank() && (p.getWord() == null || p.getWord().isBlank())) {
            p.setWord(word);
        }
        p.setLastSeenAt(now);
        userWordProgressMapper.updateById(p);

        UserWordEvent event = new UserWordEvent();
        event.setUsername(username);
        event.setOrigin(origin);
        event.setWordId(wordId);
        event.setResult(correct ? 1 : 0);
        event.setEventType(eventType == null || eventType.isBlank() ? "learn" : eventType);
        event.setCreatedAt(now);
        userWordEventMapper.insert(event);
    }

    private static long intervalSecondsForStage(int stage) {
        if (stage <= 0)
            return 10L * 60;
        if (stage == 1)
            return 1L * 24 * 60 * 60;
        if (stage == 2)
            return 2L * 24 * 60 * 60;
        if (stage == 3)
            return 4L * 24 * 60 * 60;
        if (stage == 4)
            return 7L * 24 * 60 * 60;
        return 15L * 24 * 60 * 60;
    }
}
