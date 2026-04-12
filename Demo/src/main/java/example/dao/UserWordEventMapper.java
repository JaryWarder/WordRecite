package example.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import example.pojo.UserWordEvent;

public interface UserWordEventMapper extends BaseMapper<UserWordEvent> {

    @Select("""
            SELECT DATE(created_at) AS d,
                   SUM(CASE WHEN event_type = 'learn' THEN 1 ELSE 0 END) AS learned,
                   SUM(CASE WHEN event_type = 'review' THEN 1 ELSE 0 END) AS reviewed,
                   SUM(CASE WHEN result = 1 THEN 1 ELSE 0 END) AS correct,
                   COUNT(1) AS total
            FROM user_word_event
            WHERE username = #{username}
              AND origin = #{origin}
              AND created_at >= #{start}
              AND created_at < #{end}
            GROUP BY DATE(created_at)
            ORDER BY d
            """)
    List<DailyAggRow> selectDailyAgg(@Param("username") String username,
            @Param("origin") String origin,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    class DailyAggRow {
        private LocalDate d;
        private Integer learned;
        private Integer reviewed;
        private Integer correct;
        private Integer total;

        public LocalDate getD() {
            return d;
        }

        public void setD(LocalDate d) {
            this.d = d;
        }

        public Integer getLearned() {
            return learned;
        }

        public void setLearned(Integer learned) {
            this.learned = learned;
        }

        public Integer getReviewed() {
            return reviewed;
        }

        public void setReviewed(Integer reviewed) {
            this.reviewed = reviewed;
        }

        public Integer getCorrect() {
            return correct;
        }

        public void setCorrect(Integer correct) {
            this.correct = correct;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }
    }
}
