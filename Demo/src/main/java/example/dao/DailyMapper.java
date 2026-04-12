package example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import example.pojo.Daily;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DailyMapper extends BaseMapper<Daily> {
    @Delete("DELETE FROM daily WHERE username = #{username}")
    void deleteByUsername(@Param("username") String username);

    @Select("SELECT DATE_FORMAT(review_date, '%Y-%m-%d') FROM daily WHERE username = #{username} GROUP BY review_date ORDER BY review_date DESC")
    List<String> selectReviewDatesDesc(@Param("username") String username);

    @Select("SELECT * FROM daily WHERE username = #{username} AND review_date = #{date} ORDER BY daily_id ASC")
    List<Daily> selectByUserAndDate(@Param("username") String username, @Param("date") String date);
}
