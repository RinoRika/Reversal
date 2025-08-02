package dev.yalan.live;

import com.google.gson.JsonObject;

import java.util.UUID;

public class LiveUser {
    private final String clientId;
    private final UUID userId;
    private final JsonObject payload;
    private final Level level;

    public LiveUser(String clientId, UUID userId, JsonObject payload) {
        this.clientId = clientId;
        this.userId = userId;
        this.payload = payload;
        this.level = isReversalUser() ? Level.of(payload.get("level").getAsString()) : Level.FOREIGNER;
    }

    public String getName() {
        return payload.get("username").getAsString();
    }

    public String getRank() {
        if (isReversalUser()) {
            return payload.get("rank").getAsString();
        }

        return null;
    }

    public Level getLevel() {
        return level;
    }

    public boolean isReversalUser() {
        return "Reversal".equals(clientId);
    }

    public String getClientId() {
        return clientId;
    }

    public UUID getUserId() {
        return userId;
    }

    public JsonObject getPayload() {
        return payload;
    }

    public enum Level {
        GENERAL("General"),
        ADMINISTRATOR("Administrator"),
        FOREIGNER("Foreigner"),
        UNKNOWN("Unknown");

        private final String formalName;

        Level(String formalName) {
            this.formalName = formalName;
        }

        public static Level of(String name) {
            for (Level level : values()) {
                if (level.formalName.equalsIgnoreCase(name)) {
                    return level;
                }
            }

            return UNKNOWN;
        }
    }
}
