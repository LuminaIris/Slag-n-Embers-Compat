package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.register.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"all", "unchecked"})
public class CrucibleBE extends FluidMultiBlockEntity {
    public boolean hasCover = false;

    public CrucibleBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null) return;
        if (level.isClientSide) return;

        var tank = getTankInventory();
        if (tank.tryAlloy(level, 1)) tank.onContentsChanged();

        var cover = hasCover();
        if (cover == null) return;
        if (hasCover != cover) {
            hasCover = hasCover();
            setChanged();
        }

        if (!hasCover) tank.noCoverTick();
    }

    public Boolean hasCover() {
        for (int widthX = 0; widthX < getWidthX(); widthX++) for (int widthZ = 0; widthZ < getWidthZ(); widthZ++) {
            var pos = getBlockPos().offset(widthX, height, widthZ);
            if (!level.isLoaded(pos)) return null;
            var state = level.getBlockState(pos);
            if (state.isAir()) return false;
            if (!state.isFaceSturdy(level, pos, Direction.DOWN)) return false;
            if (state.canBeReplaced()) return false;
        }
        return true;
    }

    @Override
    public void notifyMultiUpdated() {
        super.notifyMultiUpdated();

        BlockState state = this.getBlockState();
        if (state.is(AllBlocks.CRUCIBLE)) {
            state = state.setValue(BOTTOM, getController().getY() == getBlockPos().getY());
            state = state.setValue(TOP, getController().getY() + getHeight() - 1 == getBlockPos().getY());
            level.setBlock(getBlockPos(), state, Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE);
        }

        if (isController()) setShapes();

        setChanged();
    }

    @Override
    public void removeController(boolean keepFluids) {
        super.removeController(keepFluids);

        if (level != null && !level.isClientSide) {
            BlockState state = this.getBlockState();
            if (state.is(AllBlocks.CRUCIBLE)) {
                state = state.setValue(BOTTOM, true)
                    .setValue(TOP, true)
                    .setValue(WINDOW, false)
                    .setValue(SHAPE, Shape.PLAIN);
                level.setBlock(getBlockPos(), state, Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    public boolean isSameController(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CrucibleBE be && be.getController().equals(getController());
    }

    @Override
    public void setWindows(boolean window) {
        super.setWindows(window);
        setShapes();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        hasCover = compound.getBoolean("hasCover");
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean("hasCover", hasCover);
    }

    public void setShapes() {
        if (level == null || level.isClientSide) return;

        boolean window = isWindow();
        int widthX = getWidthX();
        int widthZ = getWidthZ();
        int height = getHeight();

        for (int yOffset = 0; yOffset < height; yOffset++) for (int xOffset = 0; xOffset < widthX; xOffset++) for (int zOffset = 0; zOffset < widthZ; zOffset++) {
            BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
            BlockState blockState = level.getBlockState(pos);

            if (!blockState.is(AllBlocks.CRUCIBLE)) continue;

            Shape shape = calculateShapeForPosition(xOffset, zOffset, widthX, widthZ);
            level.setBlock(pos, blockState.setValue(WINDOW, window).setValue(SHAPE, shape), Block.UPDATE_CLIENTS | Block.UPDATE_INVISIBLE | Block.UPDATE_KNOWN_SHAPE);
        }
    }

    private Shape calculateShapeForPosition(int xOffset, int zOffset, int widthX, int widthZ) {
        if (widthX == 1 || widthZ == 1) return Shape.PLAIN;

        boolean isNorth = zOffset == 0;
        boolean isSouth = zOffset == widthZ - 1;
        boolean isWest = xOffset == 0;
        boolean isEast = xOffset == widthX - 1;

        int edgeCount = (isNorth ? 1 : 0) + (isSouth ? 1 : 0) + (isWest ? 1 : 0) + (isEast ? 1 : 0);

        if (edgeCount == 2) {
            if (isNorth && isWest) return Shape.NW;
            if (isNorth && isEast) return Shape.NE;
            if (isSouth && isWest) return Shape.SW;
            if (isSouth && isEast) return Shape.SE;
        }

        if (edgeCount == 1) {
            if (isNorth) return Shape.NORTH;
            if (isSouth) return Shape.SOUTH;
            if (isWest) return Shape.WEST;
            if (isEast) return Shape.EAST;
        }

        return Shape.INNER;
    }

    public static final BooleanProperty TOP = BooleanProperty.create("top");
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    public static final BooleanProperty WINDOW = BooleanProperty.create("window");
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);

    @SuppressWarnings("unused")
    public enum Shape implements StringRepresentable {
        PLAIN,
        INNER,
        NW, SW, NE, SE,
        NORTH, SOUTH, WEST, EAST;

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }

        public static Shape fromDir(Direction direction) {
            return switch (direction) {
                case DOWN, UP -> PLAIN;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
            };
        }
        public static Shape fromDirDir(Direction dir1, Direction dir2) {
            return switch (dir1) {
                case NORTH -> switch (dir2) {
                    case NORTH -> NORTH;
                    case WEST -> NW;
                    case EAST -> NE;
                    default -> PLAIN;
                };
                case SOUTH -> switch (dir2) {
                    case SOUTH -> SOUTH;
                    case WEST -> SW;
                    case EAST -> SE;
                    default -> PLAIN;
                };
                case WEST -> switch (dir2) {
                    case NORTH -> NW;
                    case SOUTH -> SW;
                    case WEST -> WEST;
                    default -> PLAIN;
                };
                case EAST -> switch (dir2) {
                    case NORTH -> NE;
                    case SOUTH -> SE;
                    case EAST -> EAST;
                    default -> PLAIN;
                };
                default -> PLAIN;
            };
        }

        public boolean isWall() {
            return this.equals(NORTH) || this.equals(SOUTH) || this.equals(WEST) || this.equals(EAST);
        }
        public boolean isCorner() {
            return this.equals(NW) || this.equals(SW) || this.equals(NE) || this.equals(SE);
        }

        public Direction toDirection() {
            return switch (this) {
                case NE, NORTH -> Direction.NORTH;
                case NW, WEST -> Direction.WEST;
                case SW, SOUTH -> Direction.SOUTH;
                case SE, EAST -> Direction.EAST;
                default -> Direction.DOWN;
            };
        }
    }
}
