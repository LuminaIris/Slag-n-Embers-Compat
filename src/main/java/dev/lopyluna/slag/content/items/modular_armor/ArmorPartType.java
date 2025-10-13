package dev.lopyluna.slag.content.items.modular_armor;

import org.joml.Math;

public class ArmorPartType {
    final float duraMod;
    public final String id;

    private ArmorPartType(String id, float duraMod) {
        this.id = id;
        this.duraMod = duraMod;
    }

    public static class Builder {
        private float duraMod = 0;
        private final String id;

        public Builder(String id) {
            this.id = id;
        }

        public Builder setDuraMod(float value) { duraMod = Math.max(value, 0f); return this; }
        public ArmorPartType register() {
            return new ArmorPartType(id, duraMod);
        }
    }
}
