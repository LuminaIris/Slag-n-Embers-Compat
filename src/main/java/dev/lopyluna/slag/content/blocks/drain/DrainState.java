package dev.lopyluna.slag.content.blocks.drain;

public enum DrainState {
    OFF,
    POURING,
    POWERED;

    public static DrainState fromString(String value) {
        return switch (value) {
            case "POURING" -> POURING;
            case "POWERED" -> POWERED;
            default -> OFF;
        };
    }
}
