package example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import example.pojo.Daily;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface DailyMapper extends BaseMapper<Daily> {
    @Delete("DELETE FROM daily WHERE username = #{username}")
    void deleteByUsername(@Param("username") String username);
}
