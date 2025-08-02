package dev.yalan.live.events;

import cn.stars.reversal.event.Event;

public class EventLiveGenericMessage extends Event {
    private final String channel;
    private final String message;

    public EventLiveGenericMessage(String channel, String message) {
        this.channel = channel;
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public String getMessage() {
        return message;
    }
}
