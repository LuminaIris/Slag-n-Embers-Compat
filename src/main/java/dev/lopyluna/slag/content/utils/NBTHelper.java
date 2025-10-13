package dev.lopyluna.slag.content.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NBTHelper {
    public static BlockPos readBlockPos(CompoundTag nbt, String key) {
        Optional<BlockPos> pos = NbtUtils.readBlockPos(nbt, key);
        if (pos.isPresent()) return pos.get();
        CompoundTag oldTag = nbt.getCompound(key);
        return new BlockPos(oldTag.getInt("X"), oldTag.getInt("Y"), oldTag.getInt("Z"));
    }

    public Vec3i loadVec3i(CompoundTag nbt, String key) {
        var tag = nbt.getCompound(key);
        return tag.isEmpty() ? null : new Vec3i(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }

    public void saveVec3i(CompoundTag nbt, String key, Vec3i vec) {
        if (vec == null) return;
        var tag = new CompoundTag();
        for (var axis : Direction.Axis.values()) tag.putInt(axis.getName(), vec.get(axis));
        nbt.put(key, tag);
    }

    public static ListTag writeFluidList(List<FluidStack> stacks, HolderLookup.Provider registries) {
        ListTag listNBT = new ListTag();
        for (FluidStack stack : stacks) listNBT.add(stack.saveOptional(registries));
        return listNBT;
    }

    public static List<FluidStack> readFluidList(ListTag stacks, HolderLookup.Provider registries) {
        List<FluidStack> list = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) list.add(i, FluidStack.parseOptional(registries, stacks.getCompound(i)));
        return list;
    }
}
