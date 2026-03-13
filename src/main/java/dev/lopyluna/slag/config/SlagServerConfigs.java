package dev.lopyluna.slag.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SlagServerConfigs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue CRUCIBLE_MAX_WIDTH = BUILDER.comment("Max Width for the Crucible")
            .defineInRange("CrucibleMaxWidth", 4, 1, 9);
    public static final ModConfigSpec.IntValue CRUCIBLE_MAX_HEIGHT = BUILDER.comment("Max Height for the Crucible")
            .defineInRange("CrucibleMaxHeight", 6, 1, 9);
    public static final ModConfigSpec.IntValue DRAIN_MB_SPEED = BUILDER.comment("Drain Speed in Millibuckets")
            .defineInRange("DrainMbSpeed", 25, 1, 10000);

    public static final ModConfigSpec.BooleanValue INSERT_FLUID_ITEM_INTO_CRUCIBLE = BUILDER.comment("Whether to insert Fluid Items into the Crucible")
            .define("InsertFluidIntoCrucible", true);
    public static final ModConfigSpec.BooleanValue EXTRACT_FLUID_FROM_DRAIN_TO_ITEM = BUILDER.comment("Whether to extract Fluid from Drain to Item")
            .define("ExtractFluidFromDrainToItem", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
