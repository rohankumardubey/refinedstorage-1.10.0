package com.refinedmods.refinedstorage.apiimpl.network.node.cover;

import com.refinedmods.refinedstorage.api.util.Action;
import com.refinedmods.refinedstorage.apiimpl.network.node.CableNetworkNode;
import com.refinedmods.refinedstorage.apiimpl.network.node.NetworkNode;
import com.refinedmods.refinedstorage.item.CoverItem;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class CoverManager {

    public static final ModelProperty<CoverManager> PROPERTY = new ModelProperty<>();
    private static final String NBT_DIRECTION = "Direction";
    private static final String NBT_ITEM = "Item";
    private static final String NBT_TYPE = "Type";
    public static String NBT_COVER_MANAGER = "Cover";
    private Map<Direction, Cover> covers = new HashMap<>();
    private NetworkNode node;

    public CoverManager(NetworkNode node) {
        this.node = node;
    }

    @SuppressWarnings("deprecation")
    public static boolean isValidCover(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }

        Block block = getBlock(item);

        BlockState state = getBlockState(item);

        return block != null
            && state != null
            && isModelSupported(state)
            && !block.isRandomlyTicking(state)
            && !(block instanceof EntityBlock)
            && !state.useShapeForLightOcclusion();
    }

    private static boolean isModelSupported(BlockState state) {
        if (state.getRenderShape() != RenderShape.MODEL) {
            return false;
        }

        return state.canOcclude();
    }

    @Nullable
    public static Block getBlock(ItemStack item) {
        if (item.isEmpty()) {
            return null;
        }

        Block block = Block.byItem(item.getItem());

        if (block == Blocks.AIR) {
            return null;
        }

        return block;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static BlockState getBlockState(ItemStack item) {
        Block block = getBlock(item);

        if (block == null) {
            return null;
        }

        return block.defaultBlockState();
    }

    @Nullable
    public Cover getCover(Direction facing) {
        return covers.get(facing);
    }

    public boolean hasCover(Direction facing) {
        return covers.containsKey(facing);
    }

    public boolean setCover(Direction facing, @Nullable Cover cover) {
        if (cover == null || (isValidCover(cover.getStack()) && !hasCover(facing))) {
            if (cover != null) {
                if (!(node instanceof CableNetworkNode) && facing == node.getDirection() && cover.getType() != CoverType.HOLLOW) {
                    return false;
                }
            }

            if (cover == null) {
                covers.remove(facing);
            } else {
                covers.put(facing, cover);
            }

            node.markDirty();

            if (node.getNetwork() != null) {
                node.getNetwork().getNodeGraph().invalidate(Action.PERFORM, node.getNetwork().getLevel(), node.getNetwork().getPosition());
            }

            return true;
        }

        return false;
    }

    @Nullable
    public Cover removeCover(Direction direction) {
        if (hasCover(direction)) {
            Cover cover = covers.remove(direction);

            node.markDirty();

            if (node.getNetwork() != null) {
                node.getNetwork().getNodeGraph().invalidate(Action.PERFORM, node.getNetwork().getLevel(), node.getNetwork().getPosition());
            }

            return cover;
        }
        return null;
    }

    public void readFromNbt(CompoundTag nbt) {
        covers.clear();
        for (String s : nbt.getAllKeys()) {
            CompoundTag tag = nbt.getCompound(s);
            if (tag.contains(NBT_DIRECTION) && tag.contains(NBT_ITEM)) {
                Direction direction = Direction.from3DDataValue(tag.getInt(NBT_DIRECTION));
                ItemStack item = ItemStack.of(tag.getCompound(NBT_ITEM));
                int type = tag.contains(NBT_TYPE) ? tag.getInt(NBT_TYPE) : 0;

                if (type >= CoverType.values().length) {
                    type = 0;
                }

                if (isValidCover(item)) {
                    covers.put(direction, new Cover(item, CoverType.values()[type]));
                }
            }
        }
    }

    public CompoundTag writeToNbt() {
        CompoundTag list = new CompoundTag();

        for (Map.Entry<Direction, Cover> entry : covers.entrySet()) {
            CompoundTag tag = new CompoundTag();

            tag.putInt(NBT_DIRECTION, entry.getKey().ordinal());
            tag.put(NBT_ITEM, entry.getValue().getStack().serializeNBT());
            tag.putInt(NBT_TYPE, entry.getValue().getType().ordinal());

            list.put(entry.getKey().ordinal() + "", tag);
        }
        return list;
    }

    public IItemHandlerModifiable getAsInventory() {
        ItemStackHandler handler = new ItemStackHandler(covers.size());

        int i = 0;

        for (Map.Entry<Direction, Cover> entry : covers.entrySet()) {
            ItemStack cover = entry.getValue().getType().createStack();

            CoverItem.setItem(cover, entry.getValue().getStack());

            handler.setStackInSlot(i++, cover);
        }

        return handler;
    }

}
