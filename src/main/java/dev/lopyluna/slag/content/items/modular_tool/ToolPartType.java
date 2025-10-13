package dev.lopyluna.slag.content.items.modular_tool;

import org.joml.Math;

public class ToolPartType {
    final float speedMod;
    final float duraMod;
    final float sharpMod;
    public final String id;

    private ToolPartType(String id, float speedMod, float duraMod, float sharpMod) {
        this.id = id;
        this.speedMod = speedMod;
        this.duraMod = duraMod;
        this.sharpMod = sharpMod;
    }

    public static class Builder {
        private float speedMod = 0;
        private float duraMod = 0;
        private float sharpMod = 0;
        private final String id;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setSpeedMod(float value) { speedMod = Math.max(value, 0f); return this; }
        public Builder setDuraMod(float value) { duraMod = Math.max(value, 0f); return this; }
        public Builder setSharpMod(float value) { sharpMod = Math.max(value, 0f); return this; }
        public ToolPartType register() {
            return new ToolPartType(id, speedMod, duraMod, sharpMod);
        }
    }
}
