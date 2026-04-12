package example.dto;

import java.util.List;

public class TestSubmitRequestDto {
    private String bookTitle;
    private List<TestSubmitItemDto> history;

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public List<TestSubmitItemDto> getHistory() {
        return history;
    }

    public void setHistory(List<TestSubmitItemDto> history) {
        this.history = history;
    }
}
