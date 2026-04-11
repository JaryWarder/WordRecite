package example.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import example.pojo.PrivateBooks;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PrivateBooksMapper extends BaseMapper<PrivateBooks> {

    @Select("SELECT * FROM private_books WHERE username = #{username} LIMIT #{limit} OFFSET #{offset}")
    List<PrivateBooks> selectPageByUsername(@Param("username") String username, @Param("offset") int offset, @Param("limit") int limit);
}
