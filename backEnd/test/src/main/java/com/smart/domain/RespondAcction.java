/*
    Description: Used to deal with JSON.
                 Give results to the frontend when files in backend changed
                 (changes caused by frontend message, not changes by operations directly in backend).
*/

package com.smart.domain;

public class RespondAcction {
    private String event;
    private String result;

    public void setEvent(String event) {
        this.event = event;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getEvent() {
        return event;
    }

    public String getResult() {
        return result;
    }
}
