package com.railway.helloworld.model;

import lombok.Data;

@Data
public class ChatMessage {

    private String message;  // User's message/prompt

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
