package com.example.feruchemy.items;

import com.example.feruchemy.Feruchemy;
import com.example.feruchemy.config.Config;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.example.feruchemy.utils.InstanceFactory;
import com.example.feruchemy.utils.MetalChart;
import com.legobmw99.allomancy.modules.powers.util.AllomancyCapability;
import com.legobmw99.allomancy.setup.Metal;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetalMind extends Item {
    // I must be drunk to write this...
    public static final List<String> NON_METAL_KEYS = Arrays.asList("fid");

    public static final int CD = 20;
    public static final int MAX_SHAVE_COUNT = 64;
    private static Random random = new Random();
    private boolean isInf = false;

    private static final List<String> validMetalStrings = Stream.of(Metal.values())
            .map(Enum::name)
            .collect(Collectors.toList());

    public MetalMind() {
        super(new Item.Properties().group(Feruchemy.ITEM_GROUP).maxStackSize(1));
    }

    public static int getMaxStorage(ItemStack itemStack, Metal metal){
        return MetalMind.getFlakeCount(itemStack, metal) * Config.STORAGE.get(metal).get();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if(!worldIn.isRemote()){
            ItemStack flakes = playerIn.getHeldItemOffhand();
            ItemStack metalMind = playerIn.getHeldItemMainhand();
            if(FeruchemyUtils.FLAKE_ITEMS.contains(flakes.getItem()) && handIn==Hand.MAIN_HAND){
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
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    public MetalMind setInf() {
        this.isInf = true;
        return this;
    }

    public static void setFlakeCount(ItemStack itemStack, Metal metal, int count){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT metalNbt = nbt.getCompound(metal.toString());
        metalNbt.putInt("flake", count);
        nbt.put(metal.toString(), metalNbt);
    }

    public static int getFlakeCount(ItemStack itemStack, Metal metal){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundNBT nbt = itemStack.getTag();
        if (nbt == null){
            return 0;
        }
        else {
            CompoundNBT metalNbt = nbt.getCompound(metal.toString());
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

        CompoundNBT nbt = itemStack.getOrCreateTag();

        CompoundNBT metalNbt = nbt.getCompound(metal.toString());
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

        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT metalNbt = nbt.getCompound(metal.toString());

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

        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT metalNbt = nbt.getCompound(Metal.BRONZE.toString());
        return metalNbt.getInt("timer");
    }

    public static void setTimer(ItemStack itemStack, int timer){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT metalNbt = nbt.getCompound(Metal.BRONZE.toString());
        metalNbt.putInt("timer", timer);
        nbt.put(Metal.BRONZE.toString(), metalNbt);
    }

    public static void decreaseTimer(ItemStack itemStack, int amount){
        setTimer(itemStack, getTimer(itemStack)-amount);
    }

    public static int getCharge(ItemStack itemStack, Metal metal){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundNBT nbt = itemStack.getTag();
        if (nbt == null){
            return 0;
        }
        else {
            CompoundNBT metalNbt = nbt.getCompound(metal.toString());
            return metalNbt.getInt("charge");
        }
    }

    public static void setCharge(ItemStack itemStack, Metal metal, int count){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT metalNbt = nbt.getCompound(metal.toString());
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

        CompoundNBT nbt = itemStack.getTag();
        if (nbt == null){
            return 0;
        }
        else {
            CompoundNBT metalNbt = nbt.getCompound(metal.toString());
            int level = metalNbt.getInt("level");
            return Math.max(level, 1);
        }
    }

    public static void setLevel(ItemStack itemStack, Metal metal, int level){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundNBT nbt = itemStack.getOrCreateTag();
        CompoundNBT metalNbt = nbt.getCompound(metal.toString());
        metalNbt.putInt("level", level);
    }

    public static void pauseAll(ItemStack itemStack){
        assert itemStack.getItem() instanceof MetalMind;

        CompoundNBT nbt = itemStack.getTag();
        if(nbt!=null){
            for (String key: nbt.keySet()){
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
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if(isInf()){
            tooltip.add(new StringTextComponent("This Band holds an Insane amount of Investiture").mergeStyle(TextFormatting.GOLD));
        }

        CompoundNBT nbt = stack.getTag();
        if(nbt!=null){
            Set<String> keySet = nbt.keySet();
            if(keySet.contains("fid")){
                tooltip.add(new StringTextComponent("FID: ").mergeStyle(TextFormatting.GRAY).append(new StringTextComponent(toDigit(nbt.getInt("fid"), 4)).mergeStyle(TextFormatting.RED)));
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
                        tooltip.add(new StringTextComponent(metal+ ": ").append(new StringTextComponent(" INF")
                                .append(new StringTextComponent(" "+status+additional)).append(new StringTextComponent(" FLAKES: INF")).mergeStyle(TextFormatting.GRAY)));
                    }else {
                        tooltip.add(new StringTextComponent(metal+ ": ").append(new StringTextComponent(" "+ Math.max(0, getCharge(stack, metal)))
                                .append(new StringTextComponent(" "+status+additional)).append(new StringTextComponent(" FLAKES: "+getFlakeCount(stack, metal))).mergeStyle(TextFormatting.GRAY)));
                    }
                }
            }
        }
    }

    public static int getFid(ItemStack stack){
        // 0-9999 FID
        // -1: not initialized
        CompoundNBT nbt = stack.getTag();
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
        CompoundNBT nbt = stack.getTag();
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
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if(getFlakeCount(stack, Metal.BRONZE) == 0 && stack.getItem() instanceof MetalMind && ((MetalMind) stack.getItem()).isInf()){
            for(Metal metal: Metal.values()){
                setStatus(stack, Status.NULL, metal);
                setCharge(stack, metal, 1);
                setFlakeCount(stack, metal, 1000);
            }
        }

        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if(!worldIn.isRemote() && (worldIn.getGameTime() % CD == 0)){
            CompoundNBT nbt = stack.getTag();
            if(nbt != null){
                // common ticking: consume items, etc..
                for (String key: nbt.keySet()){
                    if (! validMetalStrings.contains(key)){
                        continue;
                    }

                    Metal metal = Metal.valueOf(key);
                    Status status = getStatus(stack, metal);
                    checkStatus(stack, Metal.NICROSIL);
                    Status statusN = getStatus(stack, Metal.NICROSIL);
                    boolean canNicrosilStore = statusN == Status.STORING;
                    boolean canNicrosilTap = statusN == Status.TAPPING;
                    AllomancyCapability cap = AllomancyCapability.forPlayer(entityIn);
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
                            FeruchemyUtils.whenStoringEnd((PlayerEntity) entityIn, stack, metal);
                            setStatus(stack, Status.FULL, metal);
                        }
                    }
                    else if(status == Status.TAPPING && !cap.isBurning(metal)){
                        int count = getCharge(stack, metal);
                        if (count <= 0 && (!canNicrosilTap)){
                            FeruchemyUtils.whenTappingEnd((PlayerEntity) entityIn, stack, metal);
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

                // Apply effect when storing or tapping
                for (String key: nbt.keySet()){
                    if (! validMetalStrings.contains(key)){
                        continue;
                    }
                    Metal metal = Metal.valueOf(key);
                    Status status = getStatus(stack, metal);
                    HashSet<InstanceFactory> instanceFactories = null;
                    if(status == Status.STORING){
                        instanceFactories = MetalChart.STORING_EFFECT_MAP.get(metal);
                    }

                    else if(status == Status.TAPPING && FeruchemyUtils.canPlayerTap((PlayerEntity) entityIn, stack, metal)){
                        List<HashSet<InstanceFactory>> tmp = MetalChart.TAPPING_MAP.get(metal);
                        if(tmp != null){
                            instanceFactories = tmp.get(getLevel(stack, metal)-1);
                        }
                        AllomancyCapability cap = AllomancyCapability.forPlayer(entityIn);
                        if(cap.isBurning(metal) && MetalChart.FORTH_TAP_MAP.containsKey(metal)){
                            instanceFactories = MetalChart.FORTH_TAP_MAP.get(metal);
                        }
                    }

                    if (instanceFactories != null){
                        for (InstanceFactory instanceFactory: instanceFactories){
                            if(instanceFactory != null){
                                instanceFactory.getOtherEffects().accept((PlayerEntity) entityIn, stack);
                                EffectInstance instance = instanceFactory.get();
                                if(instance != null){
                                    if((status==Status.STORING && (metal==Metal.STEEL || metal==Metal.PEWTER || metal==Metal.TIN))){
                                        // avoid status conflict
                                        AllomancyCapability capability = AllomancyCapability.forPlayer(entityIn);
                                        if(capability.isBurning(metal)){
                                            break;
                                        }
                                    }
                                    ((PlayerEntity)entityIn).addPotionEffect(instanceFactory.get());
                                }
                            }
                        }
                    }
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
