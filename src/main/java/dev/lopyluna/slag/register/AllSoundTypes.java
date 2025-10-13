package dev.lopyluna.slag.register;

import net.neoforged.neoforge.common.util.DeferredSoundType;

@SuppressWarnings("all")
public class AllSoundTypes {
    public static final DeferredSoundType CRUCIBLE = new DeferredSoundType(
            0.9F,
            1.0F,
            AllSoundEvents.CRUCIBLE.BREAK::getMainEvent,
            AllSoundEvents.CRUCIBLE.STEP::getMainEvent,
            AllSoundEvents.CRUCIBLE.PLACE::getMainEvent,
            AllSoundEvents.CRUCIBLE.HIT::getMainEvent,
            AllSoundEvents.CRUCIBLE.FALL::getMainEvent
    );
}
