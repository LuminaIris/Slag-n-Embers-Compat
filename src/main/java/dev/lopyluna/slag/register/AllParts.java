package dev.lopyluna.slag.register;

import dev.lopyluna.slag.content.types.PartType;

@SuppressWarnings("unused")
public class AllParts {
    private static final float NAN = 987654321f;

    //TODO: add Spike Tips, Rods, Wires

    public static final PartType PICKAXE_HEAD = register(new PartType.Builder("pickaxe_head").setSortOrder(1)
            .setSharpMod(0.6f)
            .setSpeedMod(2.8f)
            .setSegmentPart(AllTags.PARTS_PICKAXE_HEADS)
            .itemTags(AllTags.CAST_PICKAXE_HEADS)
            .register());
    public static final PartType AXE_HEAD = register(new PartType.Builder("axe_head").setSortOrder(2)
            .setSharpMod(1.6f)
            .setSpeedMod(3f)
            .setSegmentPart(AllTags.PARTS_AXE_HEADS)
            .itemTags(AllTags.CAST_AXE_HEADS)
            .register());
    public static final PartType SHOVEL_HEAD = register(new PartType.Builder("shovel_head").setSortOrder(0)
            .setSharpMod(0.75f)
            .setSpeedMod(3f)
            .setSegmentPart(AllTags.PARTS_SHOVEL_HEADS)
            .itemTags(AllTags.CAST_SHOVEL_HEADS)
            .register());
    public static final PartType HOE_HEAD = register(new PartType.Builder("hoe_head").setSortOrder(3)
            .setSharpMod(0f)
            .setSpeedMod(0f)
            .setSegmentPart(AllTags.PARTS_HOE_HEADS)
            .itemTags(AllTags.CAST_HOE_HEADS)
            .register());
    public static final PartType SWORD_BLADE = register(new PartType.Builder("sword_blade").setSortOrder(4)
            .setSpeedMod(2.4f)
            .setSegmentPart(AllTags.PARTS_SWORD_BLADES)
            .itemTags(AllTags.CAST_SWORD_BLADES)
            .register());
    public static final PartType GUARD = register(new PartType.Builder("guard").setSortOrder(5)
            .setSpeedMod(2.4f)
            .setDuraMod(0.28f)
            .setSegmentPart(AllTags.PARTS_GUARDS)
            .itemTags(AllTags.CAST_GUARDS)
            .register());
    public static final PartType PLATE = register(new PartType.Builder("plate").setSortOrder(6)
            .setSharpMod(0f)
            .setDura(64)
            .setDuraMod(NAN)
            .setDefenceMod(NAN)
            .setSegmentPart(AllTags.PARTS_PLATES)
            .itemTags(AllTags.CAST_PLATES)
            .register());
    public static final PartType HELMET = register(new PartType.Builder("helmet").setSortOrder(7)
            .setSharpMod(0f)
            .setDura(256)
            .setDuraMod(0.32f)
            .setDefenceMod(0.36f)
            .setSegmentPart(AllTags.PARTS_HELMETS)
            .itemTags(AllTags.CAST_HELMETS)
            .register());
    public static final PartType CHESTPLATE = register(new PartType.Builder("chestplate").setSortOrder(8)
            .setSharpMod(0f)
            .setDura(416)
            .setDuraMod(0.35f)
            .setDefenceMod(1f)
            .setSegmentPart(AllTags.PARTS_CHESTPLATES)
            .itemTags(AllTags.CAST_CHESTPLATES)
            .register());
    public static final PartType LEGGINGS = register(new PartType.Builder("leggings").setSortOrder(9)
            .setSharpMod(0f)
            .setDura(376)
            .setDuraMod(0.35f)
            .setDefenceMod(0.7f)
            .setSegmentPart(AllTags.PARTS_LEGGINGS)
            .itemTags(AllTags.CAST_LEGGINGS)
            .register());
    public static final PartType BOOTS = register(new PartType.Builder("boots").setSortOrder(10)
            .setSharpMod(0f)
            .setDura(296)
            .setDuraMod(0.34f)
            .setDefenceMod(0.325f)
            .setSegmentPart(AllTags.PARTS_BOOTS)
            .itemTags(AllTags.CAST_BOOTS)
            .register());
    
    private static PartType register(PartType part) {
        return AllDynamicTypes.registerPart(part);
    }

    public static void register() {}
}
