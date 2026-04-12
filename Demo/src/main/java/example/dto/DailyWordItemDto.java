package example.dto;

public class DailyWordItemDto {
    private Integer wordId;
    private String word;
    private String poses;
    private String origin;
    private String status;

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

    public String getPoses() {
        return poses;
    }

    public void setPoses(String poses) {
        this.poses = poses;
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
}
