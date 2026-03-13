package dev.lopyluna.slag.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SlagCommonConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CAPACITY_PER_CRUCIBLE = BUILDER.comment("CapacityPerCrucible")
            .defineInRange("CapacityPerCrucible", 1000, 10, 10000);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
