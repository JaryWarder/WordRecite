package example.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import example.pojo.UserWordProgress;

public interface UserWordProgressMapper extends BaseMapper<UserWordProgress> {

    @Select("SELECT stage, COUNT(1) AS cnt FROM user_word_progress WHERE username = #{username} AND origin = #{origin} GROUP BY stage ORDER BY stage")
    List<StageCountRow> selectStageCounts(@Param("username") String username, @Param("origin") String origin);

    @Select("SELECT COUNT(1) FROM user_word_progress WHERE username = #{username} AND origin = #{origin}")
    int countAll(@Param("username") String username, @Param("origin") String origin);

    @Select("SELECT COUNT(1) FROM user_word_progress WHERE username = #{username} AND origin = #{origin} AND stage >= #{minStage}")
    int countByMinStage(@Param("username") String username, @Param("origin") String origin,
            @Param("minStage") int minStage);

    @Select("SELECT COUNT(1) FROM user_word_progress WHERE username = #{username} AND origin = #{origin} AND next_review_at IS NOT NULL AND next_review_at <= #{now}")
    int countDue(@Param("username") String username, @Param("origin") String origin, @Param("now") LocalDateTime now);

    @Select("SELECT COUNT(1) FROM user_word_progress WHERE username = #{username} AND next_review_at IS NOT NULL AND next_review_at <= #{now}")
    int countDueAll(@Param("username") String username, @Param("now") LocalDateTime now);

    @Select("SELECT MIN(next_review_at) FROM user_word_progress WHERE username = #{username} AND origin = #{origin} AND next_review_at IS NOT NULL")
    LocalDateTime selectNextReviewAt(@Param("username") String username, @Param("origin") String origin);

    @Select("SELECT word_id, word, stage, next_review_at FROM user_word_progress WHERE username = #{username} AND origin = #{origin} AND next_review_at IS NOT NULL AND next_review_at <= #{now} ORDER BY next_review_at ASC LIMIT #{limit}")
    List<DueRow> selectDue(@Param("username") String username,
            @Param("origin") String origin,
            @Param("now") LocalDateTime now,
            @Param("limit") int limit);

    class StageCountRow {
        private Integer stage;
        private Integer cnt;

        public Integer getStage() {
            return stage;
        }

        public void setStage(Integer stage) {
            this.stage = stage;
        }

        public Integer getCnt() {
            return cnt;
        }

        public void setCnt(Integer cnt) {
            this.cnt = cnt;
        }
    }

    class DueRow {
        private Integer wordId;
        private String word;
        private Integer stage;
        private LocalDateTime nextReviewAt;

        public Integer getWordId() {
            return wordId;
        }

        public void setWordId(Integer wordId) {
            this.wordId = wordId;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public Integer getStage() {
            return stage;
        }

        public void setStage(Integer stage) {
            this.stage = stage;
        }

        public LocalDateTime getNextReviewAt() {
            return nextReviewAt;
        }

        public void setNextReviewAt(LocalDateTime nextReviewAt) {
            this.nextReviewAt = nextReviewAt;
        }
    }
}
