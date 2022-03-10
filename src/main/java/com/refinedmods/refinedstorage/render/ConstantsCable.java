package com.refinedmods.refinedstorage.render;

import com.refinedmods.refinedstorage.api.network.node.ICoverable;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.Cover;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverManager;
import com.refinedmods.refinedstorage.apiimpl.network.node.cover.CoverType;
import com.refinedmods.refinedstorage.render.collision.CollisionGroup;
import com.refinedmods.refinedstorage.blockentity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage.util.CollisionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

public class ConstantsCable {

    public static final CollisionGroup CORE = new CollisionGroup().addItem(CollisionUtils.getBounds(6, 6, 6, 10, 10, 10));
    public static final CollisionGroup NORTH = new CollisionGroup().addItem(CollisionUtils.getBounds(6, 6, 0, 10, 10, 6));
    public static final CollisionGroup EAST = new CollisionGroup().addItem(CollisionUtils.getBounds(10, 6, 6, 16, 10, 10));
    public static final CollisionGroup SOUTH = new CollisionGroup().addItem(CollisionUtils.getBounds(6, 6, 10, 10, 10, 16));
    public static final CollisionGroup WEST = new CollisionGroup().addItem(CollisionUtils.getBounds(0, 6, 6, 6, 10, 10));
    public static final CollisionGroup UP = new CollisionGroup().addItem(CollisionUtils.getBounds(6, 10, 6, 10, 16, 10));
    public static final CollisionGroup DOWN = new CollisionGroup().addItem(CollisionUtils.getBounds(6, 0, 6, 10, 6, 10));

    public static final CollisionGroup HOLDER_NORTH = new CollisionGroup().addItem(getHolderBounds(Direction.NORTH));
    public static final CollisionGroup HOLDER_EAST = new CollisionGroup().addItem(getHolderBounds(Direction.EAST));
    public static final CollisionGroup HOLDER_SOUTH = new CollisionGroup().addItem(getHolderBounds(Direction.SOUTH));
    public static final CollisionGroup HOLDER_WEST = new CollisionGroup().addItem(getHolderBounds(Direction.WEST));
    public static final CollisionGroup HOLDER_UP = new CollisionGroup().addItem(getHolderBounds(Direction.UP));
    public static final CollisionGroup HOLDER_DOWN = new CollisionGroup().addItem(getHolderBounds(Direction.DOWN));

    @Nonnull
    public static AABB getCoverBounds(Direction side) {
        switch (side) {
            case DOWN:
                return CollisionUtils.getBounds(0, 0, 0, 16, 2, 16);
            case UP:
                return CollisionUtils.getBounds(0, 14, 0, 16, 16, 16);
            case NORTH:
                return CollisionUtils.getBounds(0, 0, 0, 16, 16, 2);
            case SOUTH:
                return CollisionUtils.getBounds(0, 0, 14, 16, 16, 16);
            case WEST:
                return CollisionUtils.getBounds(0, 0, 0, 2, 16, 16);
            case EAST:
                return CollisionUtils.getBounds(14, 0, 0, 16, 16, 16);
            default:
                return null;
        }
    }

    @Nonnull
    public static AABB getHolderBounds(Direction side) {
        switch (side) {
            case DOWN:
                return CollisionUtils.getBounds(7, 2, 7, 9, 6, 9);
            case UP:
                return CollisionUtils.getBounds(7, 10, 7, 9, 14, 9);
            case NORTH:
                return CollisionUtils.getBounds(7, 7, 2, 9, 9, 6);
            case SOUTH:
                return CollisionUtils.getBounds(7, 7, 10, 9, 9, 14);
            case WEST:
                return CollisionUtils.getBounds(2, 7, 7, 6, 9, 9);
            case EAST:
                return CollisionUtils.getBounds(10, 7, 7, 14, 9, 9);
            default:
                return null;
        }
    }

    public static VoxelShape addCoverVoxelShapes(VoxelShape shape, BlockGetter world, BlockPos pos) {
        if (world != null) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof NetworkNodeBlockEntity && ((NetworkNodeBlockEntity<?>) entity).getNode() instanceof ICoverable) {
                CoverManager coverManager = ((ICoverable) ((NetworkNodeBlockEntity<?>) entity).getNode()).getCoverManager();
                for (Direction value : Direction.values()) {
                    Cover cover = coverManager.getCover(value);
                    if (cover != null) {
                        shape = Shapes.or(shape, Shapes.create(ConstantsCable.getCoverBounds(value)));
                        if (cover.getType() == CoverType.NORMAL) {
                            shape = Shapes.or(shape, Shapes.create(ConstantsCable.getHolderBounds(value)));
                        }
                    }
                }
            }
        }
        return shape;
    }

}
