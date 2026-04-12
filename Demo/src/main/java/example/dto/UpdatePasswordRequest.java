package example.dto;

public class UpdatePasswordRequest {
    private String oldEncrypted;
    private String newEncrypted;

    public String getOldEncrypted() {
        return oldEncrypted;
    }

    public void setOldEncrypted(String oldEncrypted) {
        this.oldEncrypted = oldEncrypted;
    }

    public String getNewEncrypted() {
        return newEncrypted;
    }

    public void setNewEncrypted(String newEncrypted) {
        this.newEncrypted = newEncrypted;
    }
}
