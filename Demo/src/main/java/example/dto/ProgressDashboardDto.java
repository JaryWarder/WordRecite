package example.dto;

import java.util.List;

public class ProgressDashboardDto {
    private String origin;
    private int bookSize;
    private int learnedUnique;
    private int masteredCount;
    private int dueCount;
    private List<StageDistItem> stageDist;
    private Series series;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getBookSize() {
        return bookSize;
    }

    public void setBookSize(int bookSize) {
        this.bookSize = bookSize;
    }

    public int getLearnedUnique() {
        return learnedUnique;
    }

    public void setLearnedUnique(int learnedUnique) {
        this.learnedUnique = learnedUnique;
    }

    public int getMasteredCount() {
        return masteredCount;
    }

    public void setMasteredCount(int masteredCount) {
        this.masteredCount = masteredCount;
    }

    public int getDueCount() {
        return dueCount;
    }

    public void setDueCount(int dueCount) {
        this.dueCount = dueCount;
    }

    public List<StageDistItem> getStageDist() {
        return stageDist;
    }

    public void setStageDist(List<StageDistItem> stageDist) {
        this.stageDist = stageDist;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public static class StageDistItem {
        private int stage;
        private int count;

        public int getStage() {
            return stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class Series {
        private List<DailyCount> dailyLearned;
        private List<DailyCount> dailyReviewed;
        private List<DailyAccuracy> dailyAccuracy;

        public List<DailyCount> getDailyLearned() {
            return dailyLearned;
        }

        public void setDailyLearned(List<DailyCount> dailyLearned) {
            this.dailyLearned = dailyLearned;
        }

        public List<DailyCount> getDailyReviewed() {
            return dailyReviewed;
        }

        public void setDailyReviewed(List<DailyCount> dailyReviewed) {
            this.dailyReviewed = dailyReviewed;
        }

        public List<DailyAccuracy> getDailyAccuracy() {
            return dailyAccuracy;
        }

        public void setDailyAccuracy(List<DailyAccuracy> dailyAccuracy) {
            this.dailyAccuracy = dailyAccuracy;
        }
    }

    public static class DailyCount {
        private String date;
        private int count;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public static class DailyAccuracy {
        private String date;
        private double accuracy;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public void setAccuracy(double accuracy) {
            this.accuracy = accuracy;
        }
    }
}
