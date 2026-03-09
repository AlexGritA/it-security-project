package se.apiva.chatserver.controllers.dao;

public class ApiResponse {

    private Status status;
    private String message;

    public enum Status {
        SUCCESS,
        ERROR,
        UNAUTHORIZED
    }

    public ApiResponse() { }

    public ApiResponse(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
