package com.example.feruchemy.events;


import com.example.feruchemy.Feruchemy;
import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.config.Config;
import com.example.feruchemy.effects.EffectRegister;
import com.example.feruchemy.items.MetalMind;
import com.example.feruchemy.network.NetworkUtil;
import com.example.feruchemy.utils.FeruStatus;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.modules.materials.MaterialsSetup;
import com.legobmw99.allomancy.modules.powers.PowersConfig;
import com.legobmw99.allomancy.modules.powers.util.AllomancyCapability;
import com.legobmw99.allomancy.network.Network;
import com.legobmw99.allomancy.setup.Metal;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Feruchemy.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEventHandler {

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event){
        LivingEntity entity = event.getEntityLiving();
        if(!entity.world.isRemote() && entity instanceof PlayerEntity){
            ItemStack itemstack = FeruchemyUtils.getMetalMindStack((PlayerEntity) entity);
            AllomancyCapability capability = AllomancyCapability.forPlayer(entity);
            if(itemstack!=null && capability.isBurning(Metal.GOLD) && MetalMind.getStatus(itemstack, Metal.GOLD)== MetalMind.Status.TAPPING){
                event.setCanceled(true);
                entity.setHealth(1.0F);
                entity.clearActivePotions();
                entity.addPotionEffect(new EffectInstance(Effects.REGENERATION, 900, 1));
                entity.addPotionEffect(new EffectInstance(Effects.ABSORPTION, 100, 1));
                entity.addPotionEffect(new EffectInstance(Effects.FIRE_RESISTANCE, 800, 0));
                entity.world.setEntityState(entity, (byte) 35);
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event){
        PlayerEntity player = event.getPlayer();
        if(! player.world.isRemote()){
            EffectInstance effect = player.getActivePotionEffect(EffectRegister.FIRE_ASPECT.get());
            if(effect != null){
                event.getTarget().setFire(3);
            }

            EffectInstance effect1 = player.getActivePotionEffect(EffectRegister.KNOCK_BACK.get());
            if(effect1 != null && event.getTarget() instanceof LivingEntity){
                int level = effect1.getAmplifier()+1;
                ((LivingEntity)event.getTarget()).applyKnockback(level * 0.5F, (double) MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))));
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event){
        PlayerEntity player = event.getPlayer();
        if(! player.world.isRemote()){
            EffectInstance effect = player.getActivePotionEffect(EffectRegister.FORTUNE.get());
            if(effect != null && !player.isCreative()){
                event.setCanceled(true);
                ItemStack tmp = new ItemStack(Items.NETHERITE_PICKAXE);
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(tmp);
                map.putIfAbsent(Enchantments.FORTUNE, effect.getAmplifier()+1);
                EnchantmentHelper.setEnchantments(map, tmp);
                Block.spawnDrops(event.getState(), player.world, event.getPos(), null, null, tmp);
                player.world.destroyBlock(event.getPos(), false);
            }
        }
    }

    @SubscribeEvent
    public void onSleepingTimeCheck(SleepingTimeCheckEvent event){
        PlayerEntity player = event.getPlayer();
        if(! player.world.isRemote()){
            EffectInstance effect = player.getActivePotionEffect(EffectRegister.BED_USE.get());
            if(effect != null){
                event.setResult(Event.Result.ALLOW);
            }
        }
    }

    @SubscribeEvent
    public void onLivingHeal(LivingHealEvent event){

    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(! event.player.world.isRemote()){
            PlayerEntity player = event.player;

            NetworkUtil.sync(event.player);
            EffectInstance effect = event.player.getActivePotionEffect(EffectRegister.NON_GENERATION.get());
            if(effect!=null){
                if(event.player.getHealth()>event.player.getMaxHealth()-7){
                    event.player.setHealth(event.player.getMaxHealth()-7);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getPlayer().world.isRemote()) {
            PlayerEntity player = event.getPlayer();
            FeruchemyCapability cap = FeruchemyCapability.forPlayer(player);
            PlayerEntity oldPlayer = event.getOriginal();
            oldPlayer.getCapability(FeruchemyCapability.FERUCHEMY_CAP).ifPresent((oldCap) -> {
                cap.setDeathLoc(oldCap.getDeathLoc(), oldCap.getDeathDim());
                Metal[] metals;
                int len;
                int i;
                Metal mt;

                if (true) {
                    metals = Metal.values();
                    len = metals.length;

                    for(i = 0; i < len; ++i) {
                        mt = metals[i];
                        if(oldCap.hasPower(mt)){
                            cap.addPower(mt);
                        }
                    }
                    cap.setFid(oldCap.getFid());
                    cap.setStoredExp(oldCap.getStoredExp());
                    cap.setStepAssist(oldCap.isStepAssist());
                }

            });
            Network.sync(player);
        }
    }

    @SubscribeEvent
    public void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().world.isRemote && event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity)event.getPlayer();
            FeruchemyCapability cap = FeruchemyCapability.forPlayer(player);
            if (cap.isUninvested()) {
                if(Config.STARTING_POWER.get()==0){
                    byte randomMisting = getRandMisting();
                    cap.addPower(Metal.getMetal(randomMisting));
                    ItemStack flakes = new ItemStack((IItemProvider)((RegistryObject) MaterialsSetup.FLAKES.get(randomMisting)).get());
                    if (!player.inventory.addItemStackToInventory(flakes)) {
                        ItemEntity entity = new ItemEntity(player.getEntityWorld(), player.getPositionVec().getX(), player.getPositionVec().getY(), player.getPositionVec().getZ(), flakes);
                        player.getEntityWorld().addEntity(entity);
                    }
                }
                else if(Config.STARTING_POWER.get() == 1){
                    for(Metal metal: Metal.values()){
                        cap.addPower(metal);
                    }
                }
            }

            Network.sync(event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onGetXp(PlayerXpEvent.PickupXp event){
        // TODO use FeruchemyUtils.getStatus
        PlayerEntity playerEntity = event.getPlayer();
        if(! playerEntity.world.isRemote()){
            ItemStack metalMind = FeruchemyUtils.getMetalMindStack(playerEntity);
            if(metalMind != null){
                if(MetalMind.getStatus(metalMind, Metal.ZINC) == MetalMind.Status.STORING){
                    event.setCanceled(true);
                }
                else if(MetalMind.getStatus(metalMind, Metal.ZINC) == MetalMind.Status.TAPPING){
                    ExperienceOrbEntity orbEntity = event.getOrb();
                    if(MetalMind.getLevel(metalMind, Metal.ZINC) == 1){
                        orbEntity.xpValue *= 2;
                    }
                    else if(MetalMind.getLevel(metalMind, Metal.ZINC) == 2){
                        orbEntity.xpValue *= 3;
                    }
                }
            }
        }
    }


    private byte getRandMisting(){
        Metal metal;
        byte randomMisting;
        while (true){
            randomMisting = (byte)((int)(Math.random() * (double)Metal.values().length));
            metal = Metal.getMetal(randomMisting);
            if(Config.SERVER_RESTRICT.get() && (metal!=Metal.ZINC) && (metal!=Metal.BRONZE)){
                break;
            }
            if(!Config.SERVER_RESTRICT.get()){
                break;
            }
        }
        return randomMisting;
    }

}

