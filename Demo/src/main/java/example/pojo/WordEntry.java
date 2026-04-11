package example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

// 实体名对应具体的词库表，由于数据库可能是动态表（如 CET6_new），这里不写死@TableName，使用动态表名机制或Mapper层指定
public class WordEntry {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String word;
    private String phonetic;
    private String pron;
    private String poses; // 词性和释义
    private String sentences;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getPron() {
        return pron;
    }

    public void setPron(String pron) {
        this.pron = pron;
    }

    public String getPoses() {
        return poses;
    }

    public void setPoses(String poses) {
        this.poses = poses;
    }

    public String getSentences() {
        return sentences;
    }

    public void setSentences(String sentences) {
        this.sentences = sentences;
    }
}
