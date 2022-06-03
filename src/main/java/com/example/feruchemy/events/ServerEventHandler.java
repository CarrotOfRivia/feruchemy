package com.example.feruchemy.events;


import com.example.feruchemy.Feruchemy;
import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.config.Config;
import com.example.feruchemy.effects.EffectRegister;
import com.example.feruchemy.items.MetalMind;
import com.example.feruchemy.network.NetworkUtil;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.api.data.IAllomancerData;
import com.legobmw99.allomancy.api.enums.Metal;
import com.legobmw99.allomancy.modules.materials.MaterialsSetup;
import com.legobmw99.allomancy.modules.powers.data.AllomancerCapability;
import com.legobmw99.allomancy.modules.powers.data.DefaultAllomancerData;
import com.legobmw99.allomancy.network.Network;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.ItemLike;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = Feruchemy.MOD_ID, value = Dist.DEDICATED_SERVER)
public class ServerEventHandler {

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event){
        LivingEntity entity = event.getEntityLiving();
        if(!entity.level.isClientSide() && entity instanceof Player){
            ItemStack itemstack = FeruchemyUtils.getMetalMindStack((Player) entity);
            IAllomancerData capability = entity.getCapability(AllomancerCapability.PLAYER_CAP).orElse(new DefaultAllomancerData());
            if(itemstack!=null && capability.isBurning(Metal.GOLD) && MetalMind.getStatus(itemstack, Metal.GOLD)== MetalMind.Status.TAPPING){
                event.setCanceled(true);
                entity.setHealth(1.0F);
                entity.removeAllEffects();
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                entity.level.broadcastEntityEvent(entity, (byte) 35);
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event){
        Player player = event.getPlayer();
        if(! player.level.isClientSide()){
            MobEffectInstance effect = player.getEffect(EffectRegister.FIRE_ASPECT.get());
            if(effect != null){
                event.getTarget().setSecondsOnFire(3);
            }

            MobEffectInstance effect1 = player.getEffect(EffectRegister.KNOCK_BACK.get());
            if(effect1 != null && event.getTarget() instanceof LivingEntity){
                int level = effect1.getAmplifier()+1;
                ((LivingEntity)event.getTarget()).knockback(level * 0.5F, (double) Mth.sin(player.getYRot() * ((float)Math.PI / 180F)), (double)(-Mth.cos(player.getYRot() * ((float)Math.PI / 180F))));
            }
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event){
        Player player = event.getPlayer();
        if(! player.level.isClientSide()){
            MobEffectInstance effect = player.getEffect(EffectRegister.FORTUNE.get());
            if(effect != null && !player.isCreative()){
                event.setCanceled(true);
                ItemStack tmp = new ItemStack(Items.NETHERITE_PICKAXE);
                Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(tmp);
                map.putIfAbsent(Enchantments.BLOCK_FORTUNE, effect.getAmplifier()+1);
                EnchantmentHelper.setEnchantments(map, tmp);
                Block.dropResources(event.getState(), player.level, event.getPos(), null, null, tmp);
                player.level.destroyBlock(event.getPos(), false);
            }
        }
    }

    @SubscribeEvent
    public void onSleepingTimeCheck(SleepingTimeCheckEvent event){
        Player player = event.getPlayer();
        if(! player.level.isClientSide()){
            MobEffectInstance effect = player.getEffect(EffectRegister.BED_USE.get());
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
        if(! event.player.level.isClientSide()){
            Player player = event.player;

            NetworkUtil.sync(event.player);
            MobEffectInstance effect = event.player.getEffect(EffectRegister.NON_GENERATION.get());
            if(effect!=null){
                if(event.player.getHealth()>event.player.getMaxHealth()-7){
                    event.player.setHealth(event.player.getMaxHealth()-7);
                }
            }
        }
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(FeruchemyCapability.class);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.getPlayer().level.isClientSide()) {
            Player player = event.getPlayer();
            FeruchemyCapability cap = FeruchemyCapability.forPlayer(player);
            Player oldPlayer = event.getOriginal();
            oldPlayer.reviveCaps();
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
            oldPlayer.invalidateCaps();
        }
    }

    @SubscribeEvent
    public void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide && event.getPlayer() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)event.getPlayer();
            FeruchemyCapability cap = FeruchemyCapability.forPlayer(player);
            if (cap.isUninvested()) {
                if(Config.STARTING_POWER.get()==0){
                    byte randomMisting;
                    if (Config.RESPECT_UUID.get()) {
                        randomMisting = getUUIDFerring(player);
                    } else {
                        randomMisting = getRandFerring();
                    }
                    cap.addPower(Metal.getMetal(randomMisting));
                    ItemStack flakes = new ItemStack((ItemLike)((RegistryObject) MaterialsSetup.FLAKES.get(randomMisting)).get());
                    if (!player.getInventory().add(flakes)) {
                        ItemEntity entity = new ItemEntity(player.getCommandSenderWorld(), player.position().x(), player.position().y(), player.position().z(), flakes);
                        player.getCommandSenderWorld().addFreshEntity(entity);
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
        Player playerEntity = event.getPlayer();
        if(! playerEntity.level.isClientSide()){
            ItemStack metalMind = FeruchemyUtils.getMetalMindStack(playerEntity);
            if(metalMind != null){
                if(MetalMind.getStatus(metalMind, Metal.ZINC) == MetalMind.Status.STORING){
                    event.setCanceled(true);
                }
                else if(MetalMind.getStatus(metalMind, Metal.ZINC) == MetalMind.Status.TAPPING){
                    ExperienceOrb orbEntity = event.getOrb();
                    if(MetalMind.getLevel(metalMind, Metal.ZINC) == 1){
                        orbEntity.value *= 2;
                    }
                    else if(MetalMind.getLevel(metalMind, Metal.ZINC) == 2){
                        orbEntity.value *= 3;
                    }
                }
            }
        }
    }

    private byte getUUIDFerring(Player player) {
        byte randomMisting;
        Metal metal;
        Random random = new Random();
        random.setSeed(player.getUUID().hashCode());
        //random.setSeed(player.hashCode());
        while (true) {
            randomMisting = (byte) random.nextInt(Metal.values().length);
            metal = Metal.getMetal(randomMisting);
            if(Config.SERVER_RESTRICT.get() && (metal!=Metal.ZINC) && (metal!=Metal.BRONZE)){
                break;
            } else {
                random.setSeed(randomMisting);
            }
            if(!Config.SERVER_RESTRICT.get()){
                break;
            }
        }
        return randomMisting;
    }

    private byte getRandFerring(){
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

