package dev.yalan.live.events;

import cn.stars.reversal.event.Event;

public class EventLiveConnectionStatus extends Event {
    private final boolean isSuccess;
    private final Throwable cause;

    public EventLiveConnectionStatus(boolean isSuccess, Throwable cause) {
        this.isSuccess = isSuccess;
        this.cause = cause;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Throwable getCause() {
        return cause;
    }
}
