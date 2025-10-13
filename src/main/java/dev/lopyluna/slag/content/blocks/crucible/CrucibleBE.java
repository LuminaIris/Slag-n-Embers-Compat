package dev.lopyluna.slag.content.blocks.crucible;

import dev.lopyluna.slag.content.blocks.multiblock.FluidMultiBlockEntity;
import dev.lopyluna.slag.register.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all", "unchecked"})
public class CrucibleBE extends FluidMultiBlockEntity {
    public int updateShape = 2;
    
    public CrucibleBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        setLazyTickRate(1);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null) return;
        if (updateShape > 0) handleShapeConnections(level, worldPosition, getBlockState());
        if (level.isClientSide) return;

        var tank = getTankInventory();
        if (tank.tryAlloy(level, 1)) tank.onContentsChanged();
    }

    public void updateShape() {
        updateShape = 2;
    }

    @Override
    public void notifyMultiUpdated() {
        super.notifyMultiUpdated();
        updateShape();
    }

    @Override
    public void removeController(boolean keepFluids) {
        super.removeController(keepFluids);
        updateShape();
    }

    @Override
    public void setController(BlockPos controller) {
        super.setController(controller);
        updateShape();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        updateShape();
    }

    public boolean isSameController(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CrucibleBE be && be.getController().equals(getController());
    }

    @Override
    public void setWindows(boolean window) {
        super.setWindows(window);
        updateShape();
        if (level != null) for (int yOffset = 0; yOffset < height; yOffset++) for (int xOffset = 0; xOffset < widthX; xOffset++) for (int zOffset = 0; zOffset < widthZ; zOffset++) {
            BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
            if (level.getBlockEntity(pos) instanceof CrucibleBE be) be.updateShape();
        }
    }

    public void handleShapeConnections(Level level, BlockPos pos, BlockState state) {
        updateShape -= 1;
        List<Direction> dirVCtrl = new ArrayList<>();
        List<Direction> dirHCtrl = new ArrayList<>();
        for (var dir : Direction.values()) {
            var relPos = pos.relative(dir);
            if (isSameController(level, relPos)) {
                if (dir.getAxis().isVertical()) dirVCtrl.add(dir);
                else dirHCtrl.add(dir.getOpposite());
            }
        }

        var sizeS = dirHCtrl.size();
        if (sizeS == 2) {
            var a = dirHCtrl.getFirst();
            var b = dirHCtrl.getLast();
            var target = isSameController(level, pos.relative(a.getOpposite()).relative(b.getOpposite()));
            if (!target) {
                dirHCtrl.remove(a);
                dirHCtrl.remove(b);
            }
        }
        sizeS = dirHCtrl.size();

        var shape = Shape.PLAIN;
        switch (sizeS) {
            case 4 -> shape = Shape.INNER;
            case 2 -> shape = Shape.fromDirDir(dirHCtrl.getFirst(), dirHCtrl.getLast());
            case 3 -> {
                var a = dirHCtrl.getFirst();
                var b = dirHCtrl.get(1);
                var c = dirHCtrl.getLast();
                var shaping = Shape.PLAIN;
                var targetDir = Direction.DOWN;
                if (a.getAxis().equals(b.getAxis())) {
                    var posA = isSameController(level, pos.relative(a.getOpposite()).relative(c.getOpposite()));
                    var posB = isSameController(level, pos.relative(b.getOpposite()).relative(c.getOpposite()));
                    if (posA && posB) targetDir = c;
                    else if (posA) shaping = Shape.fromDirDir(a, c);
                    else if (posB) shaping = Shape.fromDirDir(b, c);
                }
                if (a.getAxis().equals(c.getAxis())) {
                    var posA = isSameController(level, pos.relative(a.getOpposite()).relative(b.getOpposite()));
                    var posB = isSameController(level, pos.relative(c.getOpposite()).relative(b.getOpposite()));
                    if (posA && posB) targetDir = b;
                    else if (posA) shaping = Shape.fromDirDir(a, b);
                    else if (posB) shaping = Shape.fromDirDir(c, b);
                }
                if (c.getAxis().equals(b.getAxis())) {
                    var posA = isSameController(level, pos.relative(c.getOpposite()).relative(a.getOpposite()));
                    var posB = isSameController(level, pos.relative(b.getOpposite()).relative(a.getOpposite()));
                    if (posA && posB) targetDir = a;
                    else if (posA) shaping = Shape.fromDirDir(c, a);
                    else if (posB) shaping = Shape.fromDirDir(b, a);
                }
                shape = targetDir == Direction.DOWN ? shaping : Shape.fromDir(targetDir);
            }
        }
        var ctrl = getControllerBE();
        var window = ctrl != null && ctrl.isWindow();
        if (state.is(AllBlocks.CRUCIBLE)) level.setBlockAndUpdate(pos, state.setValue(WINDOW, window).setValue(TOP, !dirVCtrl.contains(Direction.UP)).setValue(BOTTOM, !dirVCtrl.contains(Direction.DOWN)).setValue(SHAPE, shape));
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
