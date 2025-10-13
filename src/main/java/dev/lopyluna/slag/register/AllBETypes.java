package dev.lopyluna.slag.register;

import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.lopyluna.slag.content.blocks.basin.BasinBE;
import dev.lopyluna.slag.content.blocks.crucible.CrucibleBE;
import dev.lopyluna.slag.content.blocks.crucible_interface.InterfaceBE;
import dev.lopyluna.slag.content.blocks.drain.DrainBE;
import dev.lopyluna.slag.content.blocks.forge.ForgeBE;
import dev.lopyluna.slag.content.blocks.melter.MelterBE;
import dev.lopyluna.slag.content.blocks.table.TableBE;
import net.minecraft.world.level.block.entity.BlockEntity;

import static dev.lopyluna.slag.SlagEmbers.REG;

@SuppressWarnings("unused")
public class AllBETypes {

    public static BlockEntityEntry<ForgeBE> FORGE = simpleBE("brick_forge", AllBlocks.FORGE, ForgeBE::new);
    public static BlockEntityEntry<InterfaceBE> INTERFACE = simpleBE("crucible_interface", AllBlocks.INTERFACE, InterfaceBE::new);
    public static BlockEntityEntry<CrucibleBE> CRUCIBLE = simpleBE("crucible", AllBlocks.CRUCIBLE, CrucibleBE::new);
    public static BlockEntityEntry<TableBE> TABLE = simpleBE("table", AllBlocks.TABLE, TableBE::new);
    public static BlockEntityEntry<BasinBE> BASIN = simpleBE("basin", AllBlocks.BASIN, BasinBE::new);
    public static BlockEntityEntry<DrainBE> DRAIN = simpleBE("drain", AllBlocks.DRAIN, DrainBE::new);
    public static BlockEntityEntry<MelterBE> MELTER = simpleBE("melter", AllBlocks.MELTER, MelterBE::new);

    public static <T extends BlockEntity> BlockEntityEntry<T> simpleBE(String name, BlockEntry<?> entry, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return REG.blockEntity(name, factory).validBlock(entry).register();
    }

    public static void register() {}
}
