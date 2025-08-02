package dev.yalan.live.events;

import cn.stars.reversal.event.Event;

public class EventLiveChannelException extends Event {
    private final Throwable cause;

    public EventLiveChannelException(Throwable cause) {
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }
}
