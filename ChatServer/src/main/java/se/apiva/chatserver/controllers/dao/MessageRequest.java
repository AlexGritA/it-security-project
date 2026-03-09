package se.apiva.chatserver.controllers.dao;

public class MessageRequest {

    private Integer to;
    private Integer from;
    private String message;

    public MessageRequest() { }

    public MessageRequest(Integer from, Integer to, String message) {
        this.to = to;
        this.message = message;
    }

    public Integer getTo() {
        return to;
    }

    public void setTo(Integer to) {
        this.to = to;
    }

    public Integer getFrom() {
        return from;
    }

    public void setFrom(Integer from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageRequest{" +
                "from=" + from +
                ", to=" + to +
                ", message='" + message + '\'' +
                '}';
    }
}
