package com.example.feruchemy.items;

import com.example.feruchemy.Feruchemy;
import com.example.feruchemy.config.Config;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.example.feruchemy.utils.InstanceFactory;
import com.example.feruchemy.utils.MetalChart;
import com.legobmw99.allomancy.api.enums.Metal;
import com.legobmw99.allomancy.modules.powers.data.AllomancerCapability;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class MetalMind extends Item {
    // I must be drunk to write this...
    public static final List<String> NON_METAL_KEYS = List.of("fid");

    public static final int CD = 20;
    public static final int MAX_SHAVE_COUNT = 64;
    private static final Random random = new Random();
    private boolean isInf = false;

    private static final List<String> validMetalStrings = Stream.of(Metal.values())
            .map(Enum::name).toList();

    public MetalMind() {
        super(new Item.Properties().tab(Feruchemy.ITEM_GROUP).stacksTo(1));
    }

    public static int getMaxStorage(ItemStack itemStack, Metal metal){
        return MetalMind.getFlakeCount(itemStack, metal) * Config.STORAGE.get(metal).get();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand handIn) {
        if(!worldIn.isClientSide()){
            ItemStack flakes = playerIn.getOffhandItem();
            ItemStack metalMind = playerIn.getMainHandItem();
            if(FeruchemyUtils.FLAKE_ITEMS.contains(flakes.getItem()) && handIn==InteractionHand.MAIN_HAND){
                String itemName = Objects.requireNonNull(flakes.getItem().getRegistryName()).getPath();
                String metalName = itemName.split("_")[0];
                Metal metal = Metal.valueOf(metalName.toUpperCase());

                int flakeCount = getFlakeCount(metalMind, metal);
                setFlakeCount(metalMind, metal, flakeCount+flakes.getCount());
                if(getStatus(metalMind, metal) == Status.FULL){
                    setStatus(metalMind, Status.PAUSED, metal);
                }
                flakes.shrink(flakes.getCount());
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }

    public MetalMind setInf() {
        this.isInf = true;
        return this;
    }

    public static void setFlakeCount(ItemStack itemStack, Metal metal, int count){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getOrCreateTag();
        CompoundTag metalNbt = nbt.getCompound(metal.toString());
        metalNbt.putInt("flake", count);
        nbt.put(metal.toString(), metalNbt);
    }

    public static int getFlakeCount(ItemStack itemStack, Metal metal){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getTag();
        if (nbt == null){
            return 0;
        }
        else {
            CompoundTag metalNbt = nbt.getCompound(metal.toString());
            return metalNbt.getInt("flake");
        }
    }

    public static void setStatus(ItemStack itemStack, Status status, int metalIndex){
        Metal metal = Metal.getMetal(metalIndex);
        setStatus(itemStack, status, metal);
    }

    public static void setStatus(ItemStack itemStack, Status status, Metal metal){
        assert itemStack.getItem() instanceof MetalMind;
        Status statusPrev = getStatus(itemStack, metal);
        if (statusPrev == Status.FULL && status == Status.STORING){
            return;
        }
        if (statusPrev == Status.EMPTY && status == Status.TAPPING){
            return;
        }
        if (status == Status.TAPPING){
            int prev_level;
            if(statusPrev == Status.TAPPING){
                prev_level = getLevel(itemStack, metal);
                int maxLevel = MetalChart.getMaxLevel(MetalChart.TAPPING_SPEED.get(metal));
                if (prev_level>=maxLevel){
                    // maximum level is 3
                    prev_level = maxLevel-1;
                }
            }
            else {
                prev_level = 0;
            }
            setLevel(itemStack, metal, prev_level+1);
        }

        CompoundTag nbt = itemStack.getOrCreateTag();

        CompoundTag metalNbt = nbt.getCompound(metal.toString());
        metalNbt.putString("status", status.toString());
        nbt.put(metal.toString(), metalNbt);
    }

    public static Status getStatus(ItemStack itemStack, int metalIndex){
        assert itemStack.getItem() instanceof MetalMind;

        Metal metal = Metal.getMetal(metalIndex);
        return getStatus(itemStack, metal);
    }

    public static Status getStatus(ItemStack itemStack, Metal metal){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getOrCreateTag();
        CompoundTag metalNbt = nbt.getCompound(metal.toString());

        String status = metalNbt.getString("status");
        if ("".equals(status)){
            return Status.NULL;
        }
        else {
            return Status.valueOf(status);
        }
    }

    public static int getTimer(ItemStack itemStack){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getOrCreateTag();
        CompoundTag metalNbt = nbt.getCompound(Metal.BRONZE.toString());
        return metalNbt.getInt("timer");
    }

    public static void setTimer(ItemStack itemStack, int timer){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getOrCreateTag();
        CompoundTag metalNbt = nbt.getCompound(Metal.BRONZE.toString());
        metalNbt.putInt("timer", timer);
        nbt.put(Metal.BRONZE.toString(), metalNbt);
    }

    public static void decreaseTimer(ItemStack itemStack, int amount){
        setTimer(itemStack, getTimer(itemStack)-amount);
    }

    public static int getCharge(ItemStack itemStack, Metal metal){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getTag();
        if (nbt == null){
            return 0;
        }
        else {
            CompoundTag metalNbt = nbt.getCompound(metal.toString());
            return metalNbt.getInt("charge");
        }
    }

    public static void setCharge(ItemStack itemStack, Metal metal, int count){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getOrCreateTag();
        CompoundTag metalNbt = nbt.getCompound(metal.toString());
        metalNbt.putInt("charge", count);
    }

    public static void addCharge(ItemStack itemStack, Metal metal, int charge){
        if (itemStack.getItem() instanceof MetalMind && ((MetalMind) itemStack.getItem()).isInf()){
            return;
        }
        setCharge(itemStack, metal, getCharge(itemStack, metal)+charge);
    }

    public static int getLevel(ItemStack itemStack, Metal metal){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getTag();
        if (nbt == null){
            return 0;
        }
        else {
            CompoundTag metalNbt = nbt.getCompound(metal.toString());
            int level = metalNbt.getInt("level");
            return Math.max(level, 1);
        }
    }

    public static void setLevel(ItemStack itemStack, Metal metal, int level){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getOrCreateTag();
        CompoundTag metalNbt = nbt.getCompound(metal.toString());
        metalNbt.putInt("level", level);
    }

    public static void pauseAll(ItemStack itemStack){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundTag nbt = itemStack.getTag();
        if(nbt!=null){
            for (String key: nbt.getAllKeys()){
                if (! validMetalStrings.contains(key)){
                    continue;
                }
                Metal metal = Metal.valueOf(key);
                Status status = getStatus(itemStack, metal);
                if(status == Status.TAPPING || status == Status.STORING){
                    setStatus(itemStack, Status.PAUSED, metal);
                }
            }
        }
    }

    public static String toDigit(int id, int length){
        StringBuilder result = new StringBuilder(id + "");
        int delta = length - result.length();
        if(delta>0){
            for (int i=0; i<delta; i++){
                result.insert(0, "0");
            }
        }
        return result.toString();
    }

    public boolean isInf() {
        return isInf;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if(isInf()){
            tooltip.add(new TextComponent("This Band holds an Insane amount of Investiture").withStyle(ChatFormatting.GOLD));
        }

        CompoundTag nbt = stack.getTag();
        if(nbt!=null){
            Set<String> keySet = nbt.getAllKeys();
            if(keySet.contains("fid")){
                tooltip.add(new TextComponent("FID: ").withStyle(ChatFormatting.GRAY).append(new TextComponent(toDigit(nbt.getInt("fid"), 4)).withStyle(ChatFormatting.RED)));
            }

            for (Metal metal: Metal.values()){
                if(keySet.contains(metal.toString())){
                    Status status = getStatus(stack, metal);
                    String additional;
                    if(status==Status.TAPPING){
                        additional = "-LVL "+getLevel(stack, metal);
                    }
                    else {
                        additional = "";
                    }
                    if(isInf()){
                        tooltip.add(new TextComponent(metal+ ": ").append(new TextComponent(" INF")
                                .append(new TextComponent(" "+status+additional)).append(new TextComponent(" FLAKES: INF")).withStyle(ChatFormatting.GRAY)));
                    }else {
                        tooltip.add(new TextComponent(metal+ ": ").append(new TextComponent(" "+ Math.max(0, getCharge(stack, metal)))
                                .append(new TextComponent(" "+status+additional)).append(new TextComponent(" FLAKES: "+getFlakeCount(stack, metal))).withStyle(ChatFormatting.GRAY)));
                    }
                }
            }
        }
    }

    public static int getFid(ItemStack stack){
        // 0-9999 FID
        // -1: not initialized
        CompoundTag nbt = stack.getTag();
        if (nbt == null){
            return -1;
        }
        else {
            if(nbt.contains("fid")){
                return nbt.getInt("fid");
            }
            else {
                return -1;
            }
        }
    }

    public static void setFid(ItemStack stack, int fid){
        CompoundTag nbt = stack.getTag();
        if (nbt != null){
            nbt.putInt("fid", fid);
        }
    }

    public static void checkStatus(ItemStack itemStack, Metal metal){
        int charge = getCharge(itemStack, metal);
        Status status = getStatus(itemStack, metal);
        if(charge < 0 && status != Status.STORING){
            setCharge(itemStack, metal, 0);
            setStatus(itemStack, Status.EMPTY, metal);
        }
        if (charge > Config.STORAGE.get(metal).get()*getFlakeCount(itemStack, metal) && status != Status.TAPPING){
            setStatus(itemStack, Status.FULL, metal);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        if(getFlakeCount(stack, Metal.BRONZE) == 0 && stack.getItem() instanceof MetalMind && ((MetalMind) stack.getItem()).isInf()){
            for(Metal metal: Metal.values()){
                setStatus(stack, Status.NULL, metal);
                setCharge(stack, metal, 1);
                setFlakeCount(stack, metal, 1000);
            }
        }

        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if(!worldIn.isClientSide() && (worldIn.getGameTime() % CD == 0)){
            CompoundTag nbt = stack.getTag();
            if(nbt != null){
                // common ticking: consume items, etc..
                for (String key: nbt.getAllKeys()){
                    if (! validMetalStrings.contains(key)){
                        continue;
                    }

                    Metal metal = Metal.valueOf(key);
                    Status status = getStatus(stack, metal);
                    checkStatus(stack, Metal.NICROSIL);
                    Status statusN = getStatus(stack, Metal.NICROSIL);
                    boolean canNicrosilStore = statusN == Status.STORING;
                    boolean canNicrosilTap = statusN == Status.TAPPING;
                    entityIn.getCapability(AllomancerCapability.PLAYER_CAP).ifPresent(
                            (cap)->{
                                if(status == Status.STORING){
                                    int count = getCharge(stack, metal);
                                    if (count < Config.STORAGE.get(metal).get()*getFlakeCount(stack, metal) || canNicrosilStore){

                                        int speed = MetalChart.getStoreSpeed(metal);
                                        if (cap.isBurning(metal)){
                                            speed = speed * 10;
                                        }

                                        if(canNicrosilStore && metal != Metal.NICROSIL){
                                            // try to store NICROSIL
                                            if((random.nextFloat()>0.5)){
                                                if(cap.isBurning(Metal.NICROSIL)){
                                                    speed = speed * 10;
                                                }
                                                addCharge(stack, Metal.NICROSIL, speed);
                                            }
                                        }
                                        else {
                                            addCharge(stack, metal, speed);
                                        }
                                    }
                                    else {
                                        FeruchemyUtils.whenStoringEnd((Player) entityIn, stack, metal);
                                        setStatus(stack, Status.FULL, metal);
                                    }
                                }
                                else if(status == Status.TAPPING && !cap.isBurning(metal)){
                                    int count = getCharge(stack, metal);
                                    if (count <= 0 && (!canNicrosilTap)){
                                        FeruchemyUtils.whenTappingEnd((Player) entityIn, stack, metal);
                                        setCharge(stack, metal, 0);
                                        setStatus(stack, Status.EMPTY, metal);
                                    }
                                    else if(!cap.isBurning(metal)){
                                        if(canNicrosilTap && metal != Metal.NICROSIL){
                                            addCharge(stack, Metal.NICROSIL, -MetalChart.TAPPING_SPEED.get(metal).get(getLevel(stack, metal)-1));
                                        }
                                        else {
                                            addCharge(stack, metal, -MetalChart.TAPPING_SPEED.get(metal).get(getLevel(stack, metal)-1));
                                        }
                                    }
                                }
                                else {
                                    int count = getCharge(stack, metal);
                                    if (count <= 0){
                                        setCharge(stack, metal, 0);
                                        setStatus(stack, Status.EMPTY, metal);
                                    }
                                }
                            }
                    );
                }

                // Apply effect when storing or tapping
                for (String key: nbt.getAllKeys()){
                    if (! validMetalStrings.contains(key)){
                        continue;
                    }
                    entityIn.getCapability(AllomancerCapability.PLAYER_CAP).ifPresent(
                            (cap)->{
                                Metal metal = Metal.valueOf(key);
                                Status status = getStatus(stack, metal);
                                HashSet<InstanceFactory> instanceFactories = null;
                                if(status == Status.STORING){
                                    instanceFactories = MetalChart.STORING_EFFECT_MAP.get(metal);
                                }

                                else if(status == Status.TAPPING && FeruchemyUtils.canPlayerTap((Player) entityIn, stack, metal)){
                                    List<HashSet<InstanceFactory>> tmp = MetalChart.TAPPING_MAP.get(metal);
                                    if(tmp != null){
                                        instanceFactories = tmp.get(getLevel(stack, metal)-1);
                                    }
                                    if(cap.isBurning(metal) && MetalChart.FORTH_TAP_MAP.containsKey(metal)){
                                        instanceFactories = MetalChart.FORTH_TAP_MAP.get(metal);
                                    }
                                }

                                if (instanceFactories != null){
                                    for (InstanceFactory instanceFactory: instanceFactories){
                                        if(instanceFactory != null){
                                            instanceFactory.getOtherEffects().accept((Player) entityIn, stack);
                                            MobEffectInstance instance = instanceFactory.get();
                                            if(instance != null){
                                                if((status==Status.STORING && (metal==Metal.STEEL || metal==Metal.PEWTER || metal==Metal.TIN))){
                                                    // avoid status conflict
                                                    if(cap.isBurning(metal)){
                                                        break;
                                                    }
                                                }
                                                ((Player)entityIn).addEffect(instanceFactory.get());
                                            }
                                        }
                                    }
                                }
                            }
                    );
                }
            }
        }
    }

    public enum Status{
        STORING,
        TAPPING,
        FULL,
        EMPTY,
        PAUSED,
        NULL
    }
}
