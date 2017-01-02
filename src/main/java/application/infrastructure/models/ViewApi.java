package application.infrastructure.models;

public class ViewApi {
    private String displayName;
    private String downloadLink;
    private String specificationType;

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setSpecificationType(String specificationType) {
        this.specificationType = specificationType;
    }

    public String getSpecificationType() {
        return specificationType;
    }
}
