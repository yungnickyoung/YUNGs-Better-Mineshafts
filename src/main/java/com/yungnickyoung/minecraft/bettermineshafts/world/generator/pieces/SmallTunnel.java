package com.yungnickyoung.minecraft.bettermineshafts.world.generator.pieces;

import com.google.common.collect.Lists;
import com.yungnickyoung.minecraft.bettermineshafts.world.BetterMineshaftFeature;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.BetterMineshaftGenerator;
import com.yungnickyoung.minecraft.bettermineshafts.world.generator.BetterMineshaftStructurePieceType;
import com.yungnickyoung.minecraft.bettermineshafts.util.BoxUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.List;
import java.util.Random;

public class SmallTunnel extends MineshaftPiece {
    private final List<Integer> supports = Lists.newLinkedList(); // local z coords
    private static final int
        SECONDARY_AXIS_LEN = 5,
        Y_AXIS_LEN = 5,
        MAIN_AXIS_LEN = 8;
    private static final int
        LOCAL_X_END = SECONDARY_AXIS_LEN - 1,
        LOCAL_Y_END = Y_AXIS_LEN - 1,
        LOCAL_Z_END = MAIN_AXIS_LEN - 1;

    public SmallTunnel(StructureManager structureManager, CompoundTag compoundTag) {
        super(BetterMineshaftStructurePieceType.SMALL_TUNNEL, compoundTag);
        ListTag listTag1 = compoundTag.getList("Supports", 3);
        for (int i = 0; i < listTag1.size(); ++i) {
            this.supports.add(listTag1.getInt(i));
        }
    }

    public SmallTunnel(int i, int chunkPieceLen, Random random, BlockBox blockBox, Direction direction, BetterMineshaftFeature.Type type) {
        super(BetterMineshaftStructurePieceType.SMALL_TUNNEL, i, chunkPieceLen, type);
        this.setOrientation(direction);
        this.boundingBox = blockBox;
    }

    @Override
    protected void toNbt(CompoundTag tag) {
        super.toNbt(tag);
        ListTag listTag1 = new ListTag();
        supports.forEach(z -> listTag1.add(IntTag.of(z)));
        tag.put("Supports", listTag1);
    }

    public static BlockBox determineBoxPosition(List<StructurePiece> list, Random random, int x, int y, int z, Direction direction) {
        BlockBox blockBox = BoxUtil.boxFromCoordsWithRotation(x, y, z, SECONDARY_AXIS_LEN, Y_AXIS_LEN, MAIN_AXIS_LEN, direction);

        // The following func call returns null if this new blockbox does not intersect with any pieces in the list.
        // If there is an intersection, the following func call returns the piece that intersects.
        StructurePiece intersectingPiece = StructurePiece.method_14932(list, blockBox); // findIntersecting

        // Thus, this function returns null if blackBox intersects with an existing piece. Otherwise, we return blackbox
        return intersectingPiece != null ? null : blockBox;
    }

    @Override
    public void method_14918(StructurePiece structurePiece, List<StructurePiece> list, Random random) {
        Direction direction = this.getFacing();
        if (direction == null) {
            return;
        }

        switch (direction) {
            case NORTH:
            default:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, direction, this.method_14923(), pieceChainLen);
                break;
            case SOUTH:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ + 1, direction, this.method_14923(), pieceChainLen);
                break;
            case WEST:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.maxZ, direction, this.method_14923(), pieceChainLen);
                break;
            case EAST:
                BetterMineshaftGenerator.generateAndAddSmallTunnelPiece(structurePiece, list, random, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, direction, this.method_14923(), pieceChainLen);
        }

        buildSupports(random);
    }

    @Override
    public boolean generate(IWorld world, ChunkGenerator<?> generator, Random random, BlockBox box, ChunkPos pos) {
        if (this.method_14937(world, box)) {
            return false;
        }

        // Place floor
        this.fill(world, box, 1, 0, 0, LOCAL_X_END - 1, 0, LOCAL_Z_END, getMainBlock());
        this.randomReplaceNonAir(world, box, random, .5f, 1, 0, 0, LOCAL_X_END - 1, 0, LOCAL_Z_END, Blocks.STONE.getDefaultState());
        this.randomReplaceNonAir(world, box, random, .1f, 1, 0, 0, LOCAL_X_END - 1, 0, LOCAL_Z_END, Blocks.COBBLESTONE.getDefaultState());

        // Randomize blocks
        this.randomReplaceNonAir(world, box, random, .1f, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, Blocks.COBBLESTONE.getDefaultState());
        this.randomReplaceNonAir(world, box, random, .1f, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, Blocks.STONE_BRICKS.getDefaultState());
        this.randomReplaceNonAir(world, box, random, .1f, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, Blocks.MOSSY_STONE_BRICKS.getDefaultState());
        this.randomReplaceNonAir(world, box, random, .1f, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, Blocks.CRACKED_STONE_BRICKS.getDefaultState());
        this.randomReplaceNonAir(world, box, random, .2f, 0, 1, 0, LOCAL_X_END, LOCAL_Y_END, LOCAL_Z_END, AIR);

        // Fill with air
        this.fill(world, box, 1, 1, 0, LOCAL_X_END - 1, LOCAL_Y_END - 1, LOCAL_Z_END, AIR);

        // Decorations
        this.supports.forEach(z -> generateSupport(world, box, random, z));
        generateRails(world, box, random);
        generateCobwebs(world, box, random);
        generateTntCarts(world, box, random);

        return true;
    }

    private void generateCobwebs(IWorld world, BlockBox box, Random random) {
        supports.forEach(z -> {
            this.randomReplaceAir(world, box, random, .15f, 1, 3, z - 3, 1, 3, z + 3, Blocks.COBWEB.getDefaultState());
            this.randomReplaceAir(world, box, random, .15f, 3, 3, z - 3, 3, 3, z + 3, Blocks.COBWEB.getDefaultState());
        });
    }

    private void generateSupport(IWorld world, BlockBox box, Random random, int z) {
        this.fill(world, box, 1, 1, z, 1, 2, z, getSupportBlock());
        this.fill(world, box, 3, 1, z, 3, 2, z, getSupportBlock());
        this.fill(world, box, 1, 3, z, 3, 3, z, getMainBlock());
        this.randomReplaceNonAir(world, box, random, .25f, 1, 3, z, 3, 3, z, getSupportBlock());
    }

    private void generateRails(IWorld world, BlockBox box, Random random) {
        // Place rails in center
        this.randomFill(world, box,  random, .5f, 2, 1, 0, 2, 1, LOCAL_Z_END, Blocks.RAIL.getDefaultState());
        // Place powered rails
        for (int n = 0; n <= LOCAL_Z_END; n++) {
            this.randomAddBlock(world, random, .07f, Blocks.POWERED_RAIL.getDefaultState().with(PoweredRailBlock.POWERED, true), 2, 1, n, box);
        }
    }

    private void generateTntCarts(IWorld world, BlockBox box, Random random) {
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            if (random.nextInt(400) == 0) {
                BlockPos blockPos = new BlockPos(this.applyXTransform(LOCAL_X_END / 2, z), applyYTransform(1), this.applyZTransform(LOCAL_X_END / 2, z));
                if (box.contains(blockPos) && !world.getBlockState(blockPos.down()).isAir()) {
                    TntMinecartEntity tntMinecartEntity = new TntMinecartEntity(world.getWorld(), ((float) blockPos.getX() + 0.5F), ((float) blockPos.getY() + 0.5F), ((float) blockPos.getZ() + 0.5F));
                    world.spawnEntity(tntMinecartEntity);
                }
            }
        }
    }

    private void buildSupports(Random random) {
        for (int z = 0; z <= LOCAL_Z_END; z++) {
            int r = random.nextInt(7);
            if (r == 0) { // Big support
                supports.add(z);
                z += 5;
            }
        }
    }
}
