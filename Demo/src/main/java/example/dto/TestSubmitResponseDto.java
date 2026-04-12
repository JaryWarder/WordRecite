package example.dto;

import java.util.List;

public class TestSubmitResponseDto {
    private String origin;
    private Integer total;
    private Integer correctCount;
    private Integer wrongCount;
    private List<TestSubmitResultItemDto> history;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Integer getWrongCount() {
        return wrongCount;
    }

    public void setWrongCount(Integer wrongCount) {
        this.wrongCount = wrongCount;
    }

    public List<TestSubmitResultItemDto> getHistory() {
        return history;
    }

    public void setHistory(List<TestSubmitResultItemDto> history) {
        this.history = history;
    }
}
