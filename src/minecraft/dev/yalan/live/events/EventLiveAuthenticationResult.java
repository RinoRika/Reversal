package dev.yalan.live.events;

import cn.stars.reversal.event.Event;

public class EventLiveAuthenticationResult extends Event {
    private final boolean isSuccess;
    private final String message;

    public EventLiveAuthenticationResult(boolean isSuccess, String message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getMessage() {
        return message;
    }
}
