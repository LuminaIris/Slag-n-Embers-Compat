package dev.lopyluna.slag.content.blocks.multiblock.connectivity;

import dev.lopyluna.slag.content.blocks.multiblock.IMultiBlockEntityContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class ConnectivityHandler {

    public static <T extends BlockEntity & IMultiBlockEntityContainer> void formMulti(T be) {
        SearchCache<T> cache = new SearchCache<>();
        List<T> frontier = new ArrayList<>();
        frontier.add(be);
        formMulti(be.getType(), be.getLevel(), cache, frontier);
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> void formMulti(BlockEntityType<?> type, BlockGetter level, SearchCache<T> cache, List<T> frontier) {
        PriorityQueue<Pair<Integer, T>> creationQueue = makeCreationQueue();
        Set<BlockPos> visited = new HashSet<>();
        Direction.Axis mainAxis = frontier.getFirst().getMainConnectionAxis();

        int minX = (mainAxis == Direction.Axis.Y ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        int minY = (mainAxis != Direction.Axis.Y ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        int minZ = (mainAxis == Direction.Axis.Y ? Integer.MAX_VALUE : Integer.MIN_VALUE);

        for (T be : frontier) {
            BlockPos pos = be.getBlockPos();
            minX = Math.min(pos.getX(), minX);
            minY = Math.min(pos.getY(), minY);
            minZ = Math.min(pos.getZ(), minZ);
        }
        if (mainAxis == Direction.Axis.Y) minX -= frontier.getFirst().getMaxWidth();
        if (mainAxis != Direction.Axis.Y) minY -= frontier.getFirst().getMaxWidth();
        if (mainAxis == Direction.Axis.Y) minZ -= frontier.getFirst().getMaxWidth();

        while (!frontier.isEmpty()) {
            T part = frontier.removeFirst();
            BlockPos partPos = part.getBlockPos();
            if (visited.contains(partPos)) continue;

            visited.add(partPos);

            int amount = tryToFormNewMulti(part, cache, true);
            if (amount > 1) creationQueue.add(Pair.of(amount, part));

            for (Direction.Axis axis : Direction.Axis.values()) {
                Direction dir = Direction.get(Direction.AxisDirection.NEGATIVE, axis);
                BlockPos next = partPos.relative(dir);

                if (next.getX() <= minX || next.getY() <= minY || next.getZ() <= minZ) continue;
                if (visited.contains(next)) continue;
                T nextBe = partAt(type, level, next);
                if (nextBe == null) continue;
                if (nextBe.isRemoved()) continue;
                frontier.add(nextBe);
            }
        }
        visited.clear();

        while (!creationQueue.isEmpty()) {
            Pair<Integer, T> next = creationQueue.poll();
            T toCreate = next.getValue();
            if (visited.contains(toCreate.getBlockPos())) continue;

            visited.add(toCreate.getBlockPos());
            tryToFormNewMulti(toCreate, cache, false);
        }
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> int tryToFormNewMulti(T be, SearchCache<T> cache, boolean simulate) {
        if (!be.isController()) return 0;

        int bestWX = 1;
        int bestWZ = 1;
        int bestAmount = -1;

        int maxX = be.getMaxWidthX();
        int maxZ = be.getMaxWidthZ();

        for (int wX = 1; wX <= maxX; wX++) for (int wZ = 1; wZ <= maxZ; wZ++) {
            if ((wX == 1 && wZ > 1) || (wX > 1 && wZ == 1)) continue;
            int amount = tryToFormNewMultiOfWidth(be, wX, wZ, cache, true);
            if (amount < bestAmount) continue;
            bestWX = wX;
            bestWZ = wZ;
            bestAmount = amount;
        }

        if (!simulate) {
            int curWX = be.getWidthX();
            int curWZ = be.getWidthZ();

            if (curWX == bestWX && curWZ == bestWZ && curWX * curWZ * be.getHeight() == bestAmount) return bestAmount;

            splitMultiAndInvalidate(be, cache, false);
            if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank()) ifluid.setTankSize(0, bestAmount);
            if (be instanceof IMultiBlockEntityContainer.FluidMulti ifluid && ifluid.hasTank()) ifluid.setTankSize(bestAmount);

            tryToFormNewMultiOfWidth(be, bestWX, bestWZ, cache, false);

            be.preventConnectivityUpdate();
            be.setWidthX(bestWX);
            be.setWidthZ(bestWZ);
            be.setHeight(bestAmount / (bestWX * bestWZ));
            be.notifyMultiUpdated();
        }
        return bestAmount;
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> int tryToFormNewMultiOfWidth(T be, int widthX, int widthZ, SearchCache<T> cache, boolean simulate) {
        if ((widthX == 1 && widthZ > 1) || (widthX > 1 && widthZ == 1)) return 0;

        int amount = 0;
        int height = 0;
        BlockEntityType<?> type = be.getType();
        Level level = be.getLevel();
        if (level == null) return 0;
        BlockPos origin = be.getBlockPos();

        // optional fluid handling
        IFluidTank beTank = null;
        FluidStack fluid = FluidStack.EMPTY;
        if (be instanceof IMultiBlockEntityContainer.Fluid ifluid && ifluid.hasTank()) {
            beTank = ifluid.getTank(0);
            fluid = beTank.getFluid();
        }
        if (be instanceof IMultiBlockEntityContainer.FluidMulti ifluid && ifluid.hasTank()) {
            beTank = ifluid.getTank();
        }
        Direction.Axis axis = be.getMainConnectionAxis();
        int maxLen = be.getMaxLength(axis, Math.max(widthX, widthZ));

        Search:
        for (int yOffset = 0; yOffset < maxLen; yOffset++) {
            for (int xOffset = 0; xOffset < widthX; xOffset++) for (int zOffset = 0; zOffset < widthZ; zOffset++) {
                BlockPos pos = switch (axis) {
                    case X -> origin.offset(yOffset, xOffset, zOffset);
                    case Y -> origin.offset(xOffset, yOffset, zOffset);
                    case Z -> origin.offset(xOffset, zOffset, yOffset);
                };
                Optional<T> part = cache.getOrCache(type, level, pos);
                if (part.isEmpty()) break Search;

                T controller = part.get();
                int otherWidthX = controller.getWidthX();
                int otherWidthZ = controller.getWidthZ();

                if (otherWidthX > widthX || otherWidthZ > widthZ) break Search;
                if (otherWidthX == widthX && otherWidthZ == widthZ && controller.getHeight() == maxLen) break Search;

                Direction.Axis conAxis = controller.getMainConnectionAxis();
                if (axis != conAxis) break Search;

                BlockPos conPos = controller.getBlockPos();
                if (!conPos.equals(origin)) {
                    if (axis == Direction.Axis.Y) { // vertical multi, like a FluidTank
                        if (conPos.getX() < origin.getX()) break Search;
                        if (conPos.getZ() < origin.getZ()) break Search;
                        if (conPos.getX() + otherWidthX > origin.getX() + widthX) break Search;
                        if (conPos.getZ() + otherWidthZ > origin.getZ() + widthZ) break Search;
                    } else { // horizontal multi, like an ItemVault
                        if (axis == Direction.Axis.Z && conPos.getX() < origin.getX()) break Search;
                        if (conPos.getY() < origin.getY()) break Search;
                        if (axis == Direction.Axis.X && conPos.getZ() < origin.getZ()) break Search;
                        if (axis == Direction.Axis.Z && conPos.getX() + otherWidthX > origin.getX() + widthX) break Search;
                        if (conPos.getY() + height > origin.getY() + be.getMaxLength(axis, Math.max(widthZ, widthX))) break Search;
                        if (axis == Direction.Axis.X && conPos.getZ() + otherWidthZ > origin.getZ() + widthX) break Search;
                    }
                }
                if (controller instanceof IMultiBlockEntityContainer.Fluid ifluidCon && ifluidCon.hasTank()) {
                    FluidStack otherFluid = ifluidCon.getFluid(0);
                    if (!fluid.isEmpty() && !otherFluid.isEmpty() && !FluidStack.isSameFluidSameComponents(fluid, otherFluid)) break Search;
                }
            }

            amount += widthX * widthZ;
            height++;
        }

        if (simulate) return amount;

        Object extraData = be.getExtraData();

        for (int yOffset = 0; yOffset < height; yOffset++) for (int xOffset = 0; xOffset < widthX; xOffset++) for (int zOffset = 0; zOffset < widthZ; zOffset++) {
            BlockPos pos = switch (axis) {
                case X -> origin.offset(yOffset, xOffset, zOffset);
                case Y -> origin.offset(xOffset, yOffset, zOffset);
                case Z -> origin.offset(xOffset, zOffset, yOffset);
            };
            T part = partAt(type, level, pos);
            if (part == null || part == be) continue;

            extraData = be.modifyExtraData(extraData);

            if (part instanceof IMultiBlockEntityContainer.Fluid ifluidPart && ifluidPart.hasTank()) {
                IFluidTank tankAt = ifluidPart.getTank(0);
                FluidStack fluidAt = tankAt.getFluid();
                if (!fluidAt.isEmpty() && be instanceof IMultiBlockEntityContainer.Fluid ifluidBE && ifluidBE.hasTank() && beTank != null) beTank.fill(fluidAt, IFluidHandler.FluidAction.EXECUTE);
                tankAt.drain(tankAt.getCapacity(), IFluidHandler.FluidAction.EXECUTE);
            } else if (part instanceof IMultiBlockEntityContainer.FluidMulti ifluidPart && ifluidPart.hasTank()) {
                var tankAt = ifluidPart.getTank();
                for (var fluidAt : ifluidPart.getFluids()) {
                    if (!fluidAt.isEmpty() && be instanceof IMultiBlockEntityContainer.FluidMulti ifluidBE && ifluidBE.hasTank() && beTank != null) beTank.fill(fluidAt, IFluidHandler.FluidAction.EXECUTE);
                }
                while (!tankAt.getFluid().isEmpty()) tankAt.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
            }

            splitMultiAndInvalidate(part, cache, false);
            part.setController(origin);
            part.preventConnectivityUpdate();
            cache.put(pos, be);
            part.setHeight(height);
            part.setWidthX(widthX);
            part.setWidthZ(widthZ);
            part.notifyMultiUpdated();
        }

        be.setExtraData(extraData);
        be.notifyMultiUpdated();
        return amount;
    }

    public static <T extends BlockEntity & IMultiBlockEntityContainer> void splitMulti(T be) {
        splitMultiAndInvalidate(be, null, false);
    }

    @SuppressWarnings("SameParameterValue")
    // tryReconnect helps whenever only a few tanks have been removed
    private static <T extends BlockEntity & IMultiBlockEntityContainer> void splitMultiAndInvalidate(T be, @Nullable SearchCache<T> cache, boolean tryReconnect) {
        Level level = be.getLevel();
        if (level == null) return;

        be = be.getControllerBE();
        if (be == null) return;

        int height = be.getHeight();
        int widthX = be.getWidthX();
        int widthZ = be.getWidthZ();
        if (widthX == 1 && widthZ == 1 && height == 1) return;

        BlockPos origin = be.getBlockPos();
        List<T> frontier = new ArrayList<>();
        Direction.Axis axis = be.getMainConnectionAxis();

        // fluid handling, if present
        FluidStack toDistribute = FluidStack.EMPTY;
        List<FluidStack> toDistributeList = new ArrayList<>();
        int maxCapacity = 0;

        boolean controllerIsMulti = false;
        IFluidTank controllerTank = null;

        if (be instanceof IMultiBlockEntityContainer.Fluid ifluidBE && ifluidBE.hasTank()) {
            toDistribute = ifluidBE.getFluid(0);
            maxCapacity = ifluidBE.getTankSize(0);
            if (!toDistribute.isEmpty() && !be.isRemoved()) toDistribute.shrink(maxCapacity);
            ifluidBE.setTankSize(0, 1);
        }

        if (be instanceof IMultiBlockEntityContainer.FluidMulti cr && cr.hasTank()) {
            controllerIsMulti = true;
            maxCapacity = cr.getTankSize();
            controllerTank = cr.getTank();
            toDistributeList = new ArrayList<>(cr.getFluids());
            //if (!toDistributeList.isEmpty() && !be.isRemoved()) shrinkFluids(toDistributeList, maxCapacity);
            //cr.setTankSize(1);
            while (!controllerTank.getFluid().isEmpty()) controllerTank.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        }

        for (int yOffset = 0; yOffset < height; yOffset++) for (int xOffset = 0; xOffset < widthX; xOffset++) for (int zOffset = 0; zOffset < widthZ; zOffset++) {
            BlockPos pos = switch (axis) {
                case X -> origin.offset(yOffset, xOffset, zOffset);
                case Y -> origin.offset(xOffset, yOffset, zOffset);
                case Z -> origin.offset(xOffset, zOffset, yOffset);
            };

            T partAt = partAt(be.getType(), level, pos);
            if (partAt == null) continue;
            if (!partAt.getController().equals(origin)) continue;

            T controllerBE = partAt.getControllerBE();
            partAt.setExtraData((controllerBE == null ? null : controllerBE.getExtraData()));
            partAt.removeController(true);

            if (partAt != be) {
                if (controllerIsMulti && !toDistributeList.isEmpty()) {
                    var tank = (partAt instanceof IMultiBlockEntityContainer.FluidMulti cr && cr.hasTank()) ? cr.getTank() : null;
                    if (tank != null) for (var distribute : shrinkFluids(toDistributeList, maxCapacity)) {
                        tank.fill(distribute, IFluidHandler.FluidAction.EXECUTE);

                        //var copy = distribute.copy();
                        //int split = Math.min(maxCapacity, distribute.getAmount());
                        //copy.setAmount(split);
                        //distribute.shrink(split);
                        //tank.fill(copy, IFluidHandler.FluidAction.EXECUTE);
                    }

                } else if (!toDistribute.isEmpty()) {
                    FluidStack copy = toDistribute.copy();
                    IFluidTank tank = (partAt instanceof IMultiBlockEntityContainer.Fluid ifluidPart ? ifluidPart.getTank(0) : null);

                    int split = Math.min(maxCapacity, toDistribute.getAmount());
                    copy.setAmount(split);
                    toDistribute.shrink(split);
                    if (tank != null) tank.fill(copy, IFluidHandler.FluidAction.EXECUTE);
                }
            }
            if (tryReconnect) {
                frontier.add(partAt);
                partAt.preventConnectivityUpdate();
            }
            if (cache != null) cache.put(pos, partAt);
        }

        if (controllerIsMulti) {
            if (controllerTank != null) for (var distribute : shrinkFluids(toDistributeList, maxCapacity)) controllerTank.fill(distribute, IFluidHandler.FluidAction.EXECUTE);
            if (be instanceof IMultiBlockEntityContainer.FluidMulti cr) cr.setTankSize(1);
        }
        assert be.getLevel() != null;
        if (be instanceof IMultiBlockEntityContainer.Inventory inv && inv.hasInventory()) be.getLevel().invalidateCapabilities(be.getBlockPos());
        if ((be instanceof IMultiBlockEntityContainer.Fluid f  && f.hasTank()) || (be instanceof IMultiBlockEntityContainer.FluidMulti fm && fm.hasTank())) be.getLevel().invalidateCapabilities(be.getBlockPos());
        if (tryReconnect) formMulti(be.getType(), level, cache == null ? new SearchCache<>() : cache, frontier);
    }

    public static List<FluidStack> shrinkFluids(List<FluidStack> fluids, int capacity) {
        List<FluidStack> shrink = new ArrayList<>();
        if (capacity <= 0 || fluids == null || fluids.isEmpty()) return shrink;

        int left = capacity;
        for (int i = 0; i < fluids.size() && left > 0; ) {
            FluidStack s = fluids.get(i);
            if (s == null || s.isEmpty()) { fluids.remove(i); continue; }

            int amt = s.getAmount();
            if (amt <= left) {
                shrink.add(s.copy());
                left -= amt;
                fluids.remove(i);
            } else {
                FluidStack part = s.copy();
                part.setAmount(left);
                shrink.add(part);
                s.setAmount(amt - left);
                left = 0;
            }
        }
        return shrink;
    }

    private static <T extends BlockEntity & IMultiBlockEntityContainer> PriorityQueue<Pair<Integer, T>> makeCreationQueue() {
        return new PriorityQueue<>((one, two) -> two.getKey() - one.getKey());
    }

    @Nullable
    public static <T extends BlockEntity & IMultiBlockEntityContainer> T partAt(BlockEntityType<?> type, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null && be.getType() == type && !be.isRemoved()) return checked(be);
        return null;
    }

    @SuppressWarnings("unused")
    public static <T extends BlockEntity & IMultiBlockEntityContainer> boolean isConnected(BlockGetter level, BlockPos pos, BlockPos other) {
        T one = checked(level.getBlockEntity(pos));
        T two = checked(level.getBlockEntity(other));
        if (one == null || two == null) return false;
        return one.getController().equals(two.getController());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity & IMultiBlockEntityContainer> T checked(BlockEntity be) {
        if (be instanceof IMultiBlockEntityContainer) return (T) be;
        return null;
    }

    private static class SearchCache<T extends BlockEntity & IMultiBlockEntityContainer> {
        Map<BlockPos, Optional<T>> controllerMap;

        public SearchCache() {
            controllerMap = new HashMap<>();
        }

        void put(BlockPos pos, T target) {
            controllerMap.put(pos, Optional.of(target));
        }

        void putEmpty(BlockPos pos) {
            controllerMap.put(pos, Optional.empty());
        }

        boolean hasVisited(BlockPos pos) {
            return controllerMap.containsKey(pos);
        }

        Optional<T> getOrCache(BlockEntityType<?> type, BlockGetter level, BlockPos pos) {
            if (hasVisited(pos))
                return controllerMap.get(pos);

            T partAt = partAt(type, level, pos);
            if (partAt == null) {
                putEmpty(pos);
                return Optional.empty();
            }
            T controller = checked(level.getBlockEntity(partAt.getController()));
            if (controller == null) {
                putEmpty(pos);
                return Optional.empty();
            }
            put(pos, controller);
            return Optional.of(controller);
        }
    }
}
