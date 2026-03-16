package dev.lopyluna.slag.content.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBE;
import dev.lopyluna.slag.register.AllBlocks;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderSceneBuilder;
import net.createmod.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class AllPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(AllBlocks.CRUCIBLE, AllBlocks.INTERFACE, AllBlocks.BASIN, AllBlocks.TABLE, AllBlocks.DRAIN, AllBlocks.MELTER)
                .addStoryBoard("smeltery", AllPonderScenes::smeltery);
    }


    public static void smeltery(SceneBuilder builder, SceneBuildingUtil util) {
        PonderSceneBuilder scene = new PonderSceneBuilder(builder.getScene());
        scene.title("smeltery", "How to make a Smeltery!");
        scene.configureBasePlate(0, 0, 5);
        scene.world().showSection(util.select().layer(0), Direction.UP);

        scene.rotateCameraY(180);

        Selection a = util.select().position(0, 1, 0);
        scene.world().modifyBlockEntityNBT(a, CrucibleBE.class, t -> new CompoundTag());

        scene.idle(8);
        Selection single = util.select().position(0, 5, 0);
        ElementLink<WorldSectionElement> singleLinkA = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkA, util.vector().of(0, -4, 0), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> singleLinkB = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkB, util.vector().of(0, -4, 1), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> singleLinkC = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkC, util.vector().of(1, -4, 1), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> singleLinkD = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkD, util.vector().of(1, -4, 0), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> singleLinkE = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkE, util.vector().of(0, -4, 2), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> singleLinkF = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkF, util.vector().of(1, -4, 2), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> singleLinkG = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkG, util.vector().of(2, -4, 2), 0);
        ElementLink<WorldSectionElement> singleLinkH = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkH, util.vector().of(2, -4, 1), 0);
        ElementLink<WorldSectionElement> singleLinkI = scene.world().showIndependentSection(single, Direction.DOWN);
        scene.world().moveSection(singleLinkI, util.vector().of(2, -4, 0), 0);
        scene.idle(2);

        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkA));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkB));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkC));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkD));

        Selection x2 = util.select().fromTo(1, 5, 0, 2, 5, 1);
        ElementLink<WorldSectionElement> x2LinkA = scene.world().showIndependentSectionImmediately(x2);
        scene.world().moveSection(x2LinkA, util.vector().of(-1, -4, 0), 0);

        scene.idle(2);

        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkE));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkF));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x2LinkA));
        Selection x3a = util.select().fromTo(3, 6, 0, 4, 6, 2);
        ElementLink<WorldSectionElement> x3aLink = scene.world().showIndependentSectionImmediately(x3a);
        scene.world().moveSection(x3aLink, util.vector().of(-3, -5, 0), 0);

        scene.idle(5);
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x3aLink));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkH));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkG));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, singleLinkI));

        Selection x3 = util.select().fromTo(2, 8, 0, 4, 8, 2);
        ElementLink<WorldSectionElement> x3LinkA = scene.world().showIndependentSectionImmediately(x3);
        scene.world().moveSection(x3LinkA, util.vector().of(-2, -7, 0), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> x3LinkB = scene.world().showIndependentSection(x3, Direction.DOWN);
        scene.world().moveSection(x3LinkB, util.vector().of(-2, -6, 0), 0);
        scene.idle(4);
        ElementLink<WorldSectionElement> x3LinkC = scene.world().showIndependentSection(x3, Direction.DOWN);
        scene.world().moveSection(x3LinkC, util.vector().of(-2, -5, 0), 0);
        scene.idle(8);
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x3LinkA));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x3LinkB));
        Selection x3x2Top = util.select().fromTo(0, 7, 2, 2, 7, 4);
        Selection x3x2Bottom = util.select().fromTo(0, 5, 2, 2, 5, 4);
        ElementLink<WorldSectionElement> x3x2TopLink = scene.world().showIndependentSectionImmediately(x3x2Top);
        ElementLink<WorldSectionElement> x3x2BottomLink = scene.world().showIndependentSectionImmediately(x3x2Bottom);
        scene.world().moveSection(x3x2TopLink, util.vector().of(0, -5, -2), 0);
        scene.world().moveSection(x3x2BottomLink, util.vector().of(0, -4, -2), 0);
        scene.idle(6);
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x3x2TopLink));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x3x2BottomLink));
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x3LinkC));
        Selection x3x3 = util.select().fromTo(0, 5, 2, 2, 7, 4);
        ElementLink<WorldSectionElement> x3x3LinkA = scene.world().showIndependentSectionImmediately(x3x3);
        scene.world().moveSection(x3x3LinkA, util.vector().of(0, -4, -2), 0);

        scene.idle(30);
        scene.addKeyframe();
        scene.overlay().showControls(util.vector().blockSurface(util.grid().at(2, 1, 2), Direction.NORTH).add(0, 0.5, 0), Pointing.LEFT, 40)
                .whileSneaking().rightClick();
        scene.idle(20);
        scene.addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, x3x3LinkA));

        Selection x3x3window = util.select().fromTo(0, 9, 2, 2, 11, 4);
        ElementLink<WorldSectionElement> x3x3windowLink = scene.world().showIndependentSectionImmediately(x3x3window);
        scene.world().moveSection(x3x3windowLink, util.vector().of(0, -8, -2), 0);
        scene.idle(40);
        scene.addKeyframe();

        Selection fluid = util.select().position(0, 1, 0);
        scene.world().showSection(fluid, Direction.DOWN);

        scene.idle(10);
        Selection campfireA = util.select().position(0, 1, 3);
        Selection campfireB = util.select().position(1, 1, 3);
        Selection melterA = util.select().position(0, 2, 3);
        Selection melterB = util.select().position(1, 2, 3);
        Selection menu = util.select().position(2, 2, 3);

        Selection basin = util.select().position(3, 1, 0);
        Selection table = util.select().position(3, 1, 1);
        Selection drainA = util.select().position(3, 2, 0);
        Selection drainB = util.select().position(3, 2, 1);
        Selection leverA = util.select().position(3, 3, 0);
        Selection leverB = util.select().position(3, 3, 1);

        scene.world().showSection(campfireA, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(campfireB, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(melterA, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(melterB, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(menu, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(basin, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(table, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(drainA, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(drainB, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(leverA, Direction.DOWN);
        scene.idle(4);
        scene.world().showSection(leverB, Direction.DOWN);
        scene.idle(8);

        scene.addKeyframe();

        scene.idle(16);
        scene.markAsFinished();
    }
}
