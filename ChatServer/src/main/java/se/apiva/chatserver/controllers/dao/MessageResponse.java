package se.apiva.chatserver.controllers.dao;

import java.util.ArrayList;
import java.util.List;

public class MessageResponse {

    private List<Message> messages = new ArrayList<Message>();

    public MessageResponse() { }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Integer to, Integer from, String toUsername, String fromUsername, String message){
        this.messages.add(
                new Message(to,from,toUsername,fromUsername,message)
        );
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "messages=" + messages +
                '}';
    }

    public class Message {

        private Integer to;
        private Integer from;
        private String toUsername;
        private String fromUsername;
        private String message;

        public Message() {
        }

        public Message(Integer to, Integer from, String toUsername, String fromUsername, String message) {
            this.to = to;
            this.from = from;
            this.toUsername = toUsername;
            this.fromUsername = fromUsername;
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

        public String getToUsername() {
            return toUsername;
        }

        public void setToUsername(String toUsername) {
            this.toUsername = toUsername;
        }

        public String getFromUsername() {
            return fromUsername;
        }

        public void setFromUsername(String fromUsername) {
            this.fromUsername = fromUsername;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "MessageResponse{" +
                    "to='" + to + '\'' +
                    ", from='" + from + '\'' +
                    ", toUsername='" + toUsername + '\'' +
                    ", fromUsername='" + fromUsername + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
