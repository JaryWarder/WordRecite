package example.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
@TableName("daily")
public class Daily {
    // 假设数据库有自增主键（如果没有，可能需要调整）
    @TableId(type = IdType.AUTO)
    private Integer dailyId; 
    
    private String username;
    private String reviewDate;
    private String origin;
    private String status;
    private String word;
    private Integer id; // 原本的单词ID

    public Integer getDailyId() {
        return dailyId;
    }

    public void setDailyId(Integer dailyId) {
        this.dailyId = dailyId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
