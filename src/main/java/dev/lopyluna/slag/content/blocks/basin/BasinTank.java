package dev.lopyluna.slag.content.blocks.basin;

import dev.lopyluna.slag.content.blocks.smart.SmartFluidTank;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class BasinTank extends SmartFluidTank {
    BasinBE be;
    public BasinTank(BasinBE be, int capacity, Consumer<FluidStack> updateCallback) {
        super(capacity, updateCallback);
        this.be = be;
    }

    @Override
    public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
        if (!be.getStack().isEmpty()) return 0;
        var handler = BasinBE.basinHandlers.get(resource.getFluid());
        if (handler == null) return 0;
        return super.fill(resource, action);
    }
}
