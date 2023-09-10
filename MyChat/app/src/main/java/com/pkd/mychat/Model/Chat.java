package com.pkd.mychat.Model;

public class Chat {

    private String id;
    private String sender;
    private String receiver;
    private String message;
    private boolean isSeen;
    private Long lastSeenAt;

    public Chat(String id, String sender, String receiver, String message, boolean isSeen, Long lastSeenAt) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.lastSeenAt = lastSeenAt;
    }

    public Chat(){
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        this.isSeen = seen;
    }

    public Long getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Long lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
