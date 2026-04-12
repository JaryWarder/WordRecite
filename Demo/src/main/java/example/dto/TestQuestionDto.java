package example.dto;

import java.util.List;

public class TestQuestionDto {
    private int id;
    private String poses;
    private List<String> options;
    private String answer;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPoses() {
        return poses;
    }

    public void setPoses(String poses) {
        this.poses = poses;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
