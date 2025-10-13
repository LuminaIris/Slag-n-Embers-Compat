package dev.lopyluna.slag.content.blocks.table;

import com.mojang.datafixers.util.Pair;
import dev.lopyluna.slag.content.blocks.smart.SmartFluidTank;
import dev.lopyluna.slag.register.AllDataComponents;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class TableTank extends SmartFluidTank {
    TableBE be;
    public TableTank(TableBE be, int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity, updateCallback);
        this.be = be;
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        var mold = be.getMold();
        if (mold.isEmpty() || !mold.has(AllDataComponents.CAST_TYPE) || !be.getStack().isEmpty()) return 0;
        var handler = TableBE.tableHandlers.get(Pair.of(resource.getFluid(), mold.get(AllDataComponents.CAST_TYPE)));
        if (handler == null) return 0;
        return super.fill(resource, action);
    }
}
