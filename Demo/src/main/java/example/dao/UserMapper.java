package example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import example.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    @Update("UPDATE user SET studied = studied + #{amount} WHERE username = #{username}")
    void updateStudied(@Param("username") String username, @Param("amount") int amount);

    @Update("UPDATE user SET lastDate = #{date} WHERE username = #{username}")
    void updateDate(@Param("username") String username, @Param("date") String date);

    @Update("UPDATE user SET day1=0, day2=0, day3=0, day4=0, day5=0, day6=0, day7=0 WHERE username = #{username}")
    void clearDays(@Param("username") String username);

    @Update("UPDATE user SET day7 = day7 + #{amount} WHERE username = #{username}")
    void updateDay7(@Param("username") String username, @Param("amount") int amount);

    // TODO: 完整的 leftShift 逻辑可以留在 Service/Controller 中组装，或使用动态 SQL
}
