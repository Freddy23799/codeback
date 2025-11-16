package codeqr.code.dto;



public class NotificationRequest {

    private String token;
    private String title;
    private String body;
    private String notifId;

    // Constructeurs
    public NotificationRequest() {}

    public NotificationRequest(String token, String title, String body, String notifId) {
        this.token = token;
        this.title = title;
        this.body = body;
        this.notifId = notifId;
    }

    // Getters et Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getNotifId() { return notifId; }
    public void setNotifId(String notifId) { this.notifId = notifId; }
}
