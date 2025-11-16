package codeqr.code.dto;

public class AppVersionDTO {
    private String version;        // version courante côté serveur
    private String url;            // url correspondant à la plateforme demandée
    private boolean mandatory;     // si la mise à jour est obligatoire
    private boolean updateAvailable; // true si serverVersion > clientVersion
    private String message;        // message d'affichage explicatif
    private String platform;       // plateforme demandée (renvoyée pour clarté)

    public AppVersionDTO() {}

    public AppVersionDTO(String version, String url, boolean mandatory,
                         boolean updateAvailable, String message, String platform) {
        this.version = version;
        this.url = url;
        this.mandatory = mandatory;
        this.updateAvailable = updateAvailable;
        this.message = message;
        this.platform = platform;
    }

    // getters / setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public boolean isUpdateAvailable() { return updateAvailable; }
    public void setUpdateAvailable(boolean updateAvailable) { this.updateAvailable = updateAvailable; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
}
