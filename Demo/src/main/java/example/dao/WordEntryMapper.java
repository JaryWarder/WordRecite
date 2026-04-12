package example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import example.pojo.WordEntry;

public interface WordEntryMapper extends BaseMapper<WordEntry> {

    @Select("SELECT * FROM ${tableName} WHERE word = #{word}")
    WordEntry selectByWordAndTable(@Param("word") String word, @Param("tableName") String tableName);

    @Select("SELECT * FROM ${tableName} WHERE id = #{id}")
    WordEntry selectByIdAndTable(@Param("id") Integer id, @Param("tableName") String tableName);

    @Select("SELECT * FROM ${tableName} WHERE id >= #{start} AND id < #{end}")
    List<WordEntry> selectListByRangeAndTable(@Param("start") Integer start, @Param("end") Integer end,
            @Param("tableName") String tableName);

    @Select("SELECT poses FROM ${tableName} WHERE id = #{id}")
    String selectPosesByIdAndTable(@Param("id") Integer id, @Param("tableName") String tableName);

    @Select("SELECT word FROM ${tableName} WHERE id = #{id}")
    String selectWordByIdAndTable(@Param("id") Integer id, @Param("tableName") String tableName);

    @Select("SELECT phonetic FROM ${tableName} WHERE id = #{id}")
    String selectPhoneticByIdAndTable(@Param("id") Integer id, @Param("tableName") String tableName);

    @Select("SELECT COUNT(1) FROM ${tableName} WHERE id <= #{maxId}")
    int countByMaxIdAndTable(@Param("maxId") int maxId, @Param("tableName") String tableName);

    @Select("SELECT id, word, poses FROM ${tableName} WHERE id <= #{maxId} ORDER BY RAND() LIMIT #{limit}")
    List<WordEntry> selectRandomEntriesByMaxIdAndTable(@Param("maxId") int maxId,
            @Param("limit") int limit,
            @Param("tableName") String tableName);

    @Select("SELECT word FROM ${tableName} WHERE id <= #{maxId} AND id <> #{excludeId} ORDER BY RAND() LIMIT #{limit}")
    List<String> selectRandomWordsByMaxIdExcludeId(@Param("maxId") int maxId,
            @Param("excludeId") int excludeId,
            @Param("limit") int limit,
            @Param("tableName") String tableName);

    @Select("SELECT word FROM ${tableName} WHERE id <= #{maxId} ORDER BY RAND() LIMIT #{limit}")
    List<String> selectRandomWordsByMaxId(@Param("maxId") int maxId,
            @Param("limit") int limit,
            @Param("tableName") String tableName);
}
