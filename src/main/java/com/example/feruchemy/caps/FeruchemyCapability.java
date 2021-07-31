package com.example.feruchemy.caps;

import com.example.feruchemy.config.Config;
import com.legobmw99.allomancy.setup.Metal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

public class FeruchemyCapability implements ICapabilitySerializable<CompoundNBT> {
    @CapabilityInject(FeruchemyCapability.class)
    public static final Capability<FeruchemyCapability> FERUCHEMY_CAP = null;
    public static final ResourceLocation IDENTIFIER = new ResourceLocation("feruchemy", "feruchemy_data");
    private static final int[] MAX_BURN_TIME = new int[]{1800, 1800, 3600, 600, 1800, 1800, 2400, 1600, 100, 20, 300, 40, 1000, 10000, 3600, 160};
    private final LazyOptional<FeruchemyCapability> handler = LazyOptional.of(() -> {
        return this;
    });
    private String death_dimension;
    private BlockPos death_pos;
    private String spawn_dimension;
    private BlockPos spawn_pos;
    private int fid;
    private int storedExp=0;
    private boolean isStepAssist;
    public final boolean[] feruchemy_powers;

    public FeruchemyCapability() {
        this.death_pos = null;
        this.spawn_pos = null;
        this.fid = (new Random().nextInt(9998)+1);

        int powers = Metal.values().length;
        this.feruchemy_powers = new boolean[powers];
        Arrays.fill(this.feruchemy_powers, false);
    }

    public static FeruchemyCapability forPlayer(Entity player) {
        return (FeruchemyCapability)player.getCapability(FERUCHEMY_CAP).orElseThrow(() -> {
            return new RuntimeException("Capability not attached!");
        });
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(FeruchemyCapability.class, new FeruchemyCapability.Storage(), () -> null);
    }

    public void setDeathLoc(BlockPos pos, RegistryKey<World> dim) {
        if (dim != null) {
            this.setDeathLoc(pos, dim.getLocation().toString());
        }
    }

    protected void setDeathLoc(BlockPos pos, String dim_name) {
        this.death_pos = pos;
        this.death_dimension = dim_name;
    }

    public BlockPos getDeathLoc() {
        return this.death_pos;
    }

    public RegistryKey<World> getDeathDim() {
        return this.death_dimension == null ? null : RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(this.death_dimension));
    }

    public void setSpawnLoc(BlockPos pos, RegistryKey<World> dim) {
        this.setSpawnLoc(pos, dim.getLocation().toString());
    }

    public void setSpawnLoc(BlockPos pos, String dim_name) {
        this.spawn_pos = pos;
        this.spawn_dimension = dim_name;
    }

    public BlockPos getSpawnLoc() {
        return this.spawn_pos;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public boolean isStepAssist() {
        return isStepAssist;
    }

    public void setStepAssist(boolean stepAssist) {
        isStepAssist = stepAssist;
    }

    public RegistryKey<World> getSpawnDim() {
        return this.spawn_dimension == null ? null : RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(this.spawn_dimension));
    }

    public void storeExp(int amount){
        this.storedExp += amount;
    }

    public int gainExp(int amount){
        int gain = Math.min(storedExp, amount);
        this.storedExp -= gain;
        return gain;
    }

    public int getStoredExp() {
        return storedExp;
    }

    public void setStoredExp(int storedExp) {
        this.storedExp = storedExp;
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return FERUCHEMY_CAP.orEmpty(cap, this.handler);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT feruchemy_data = new CompoundNBT();

        CompoundNBT abilities = new CompoundNBT();
        for (Metal mt : Metal.values()) {
            abilities.putBoolean(mt.getName(), this.hasPower(mt));
        }
        feruchemy_data.put("abilities", abilities);

        CompoundNBT position = new CompoundNBT();
        if (this.death_pos != null) {
            position.putString("death_dimension", this.death_dimension);
            position.putInt("death_x", this.death_pos.getX());
            position.putInt("death_y", this.death_pos.getY());
            position.putInt("death_z", this.death_pos.getZ());
        }

        if (this.spawn_pos != null) {
            position.putString("spawn_dimension", this.spawn_dimension);
            position.putInt("spawn_x", this.spawn_pos.getX());
            position.putInt("spawn_y", this.spawn_pos.getY());
            position.putInt("spawn_z", this.spawn_pos.getZ());
        }

        feruchemy_data.put("position", position);
        feruchemy_data.putInt("fid", fid);
        feruchemy_data.putInt("stored_exp", storedExp);
        feruchemy_data.putBoolean("is_step_assist", isStepAssist);
        return feruchemy_data;
    }

    public boolean hasPower(Metal metal) {
        return this.feruchemy_powers[metal.getIndex()];
    }
    public void addPower(Metal metal) {
        this.feruchemy_powers[metal.getIndex()] = true;
    }

    public void revokePower(Metal metal) {
        this.feruchemy_powers[metal.getIndex()] = false;
    }

    public static void addPower(Metal metal, PlayerEntity player){
        FeruchemyCapability capability = FeruchemyCapability.forPlayer(player);
        capability.addPower(metal);
    }

    public static void addAll(PlayerEntity player){
        FeruchemyCapability capability = FeruchemyCapability.forPlayer(player);
        Arrays.fill(capability.feruchemy_powers, true);
    }

    public static void revokeAll(PlayerEntity player){
        FeruchemyCapability capability = FeruchemyCapability.forPlayer(player);
        Arrays.fill(capability.feruchemy_powers, false);
    }

    public static void revokePower(Metal metal, PlayerEntity player){
        FeruchemyCapability capability = FeruchemyCapability.forPlayer(player);
        capability.revokePower(metal);
    }

    @Override
    public void deserializeNBT(CompoundNBT feruchemy_data) {
        CompoundNBT abilities = (CompoundNBT) feruchemy_data.get("abilities");
        for (Metal mt : Metal.values()) {
            if (abilities.getBoolean(mt.getName())) {
                this.addPower(mt);
            } else {
                this.revokePower(mt);
            }
        }

        CompoundNBT position = (CompoundNBT)feruchemy_data.get("position");
        assert position != null;
        if (position.contains("death_dimension")) {
            this.setDeathLoc(new BlockPos(position.getInt("death_x"), position.getInt("death_y"), position.getInt("death_z")), position.getString("death_dimension"));
        }

        if (position.contains("spawn_dimension")) {
            this.setSpawnLoc(new BlockPos(position.getInt("spawn_x"), position.getInt("spawn_y"), position.getInt("spawn_z")), position.getString("spawn_dimension"));
        }

        this.fid = feruchemy_data.getInt("fid");
        this.storedExp = feruchemy_data.getInt("stored_exp");
        this.isStepAssist = feruchemy_data.getBoolean("is_step_assist");

    }

    public boolean isUninvested() {
        for (boolean power : this.feruchemy_powers) {
            if (power) {
                return false;
            }
        }
        return true;
    }

    public static boolean canPlayerUse(PlayerEntity playerEntity, Metal metal){
        if(Config.SERVER_RESTRICT.get() && (metal==Metal.ZINC || metal==Metal.BRONZE)){
            return false;
        }
        FeruchemyCapability capability = FeruchemyCapability.forPlayer(playerEntity);
        return capability.hasPower(metal);
    }

    public static class Storage implements Capability.IStorage<FeruchemyCapability> {
        public Storage() {
        }

        @Override
        public INBT writeNBT(Capability<FeruchemyCapability> capability, FeruchemyCapability instance, Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<FeruchemyCapability> capability, FeruchemyCapability instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                instance.deserializeNBT((CompoundNBT)nbt);
            }

        }
    }
}
