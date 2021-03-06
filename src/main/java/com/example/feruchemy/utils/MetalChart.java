package com.example.feruchemy.utils;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.effects.EffectRegister;
import com.example.feruchemy.items.MetalMind;
import com.legobmw99.allomancy.api.enums.Metal;
import com.legobmw99.allomancy.modules.powers.data.AllomancerCapability;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.function.BiConsumer;

public class MetalChart {
    public static final HashMap<Metal, HashSet<InstanceFactory>> STORING_EFFECT_MAP = new HashMap<>();
    public static final HashMap<Metal, List<HashSet<InstanceFactory>>> TAPPING_MAP = new HashMap<>();
    public static final HashMap<Metal, HashSet<InstanceFactory>> FORTH_TAP_MAP = new HashMap<>();
    public static final HashMap<Metal, List<Integer>> TAPPING_SPEED = new HashMap<>();
    private static final Random random = new Random();

    public static int getMaxLevel(List<Integer> chart){
        int len = chart.size();
        for (int i=0; i<len; i++){
            if(chart.get(i) == -1){
                return i;
            }
        }
        return len;
    }

    public static int getStoreSpeed(Metal metal){
        switch (metal){
            case STEEL:
                return 2;
            case ALUMINUM:
            case BRONZE:
            case NICROSIL:
                return 0;
            default:
                return 1;
        }
    }

    static {
        STORING_EFFECT_MAP.put(Metal.IRON, new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.SLOW_FALLING))));
        STORING_EFFECT_MAP.put(Metal.STEEL, new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.MOVEMENT_SLOWDOWN, 2))));
        STORING_EFFECT_MAP.put(Metal.TIN, new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.BLINDNESS, 0, MetalMind.CD*3))));
        STORING_EFFECT_MAP.put(Metal.PEWTER, new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.WEAKNESS), new InstanceFactory(MobEffects.DIG_SLOWDOWN))));
//        STORING_EFFECT_MAP.put(Metal.ZINC, new HashSet<>(Collections.singletonList(new InstanceFactory(null, ((playerEntity, itemStack) -> {
//            ServerWorld world = (ServerWorld) playerEntity.world;
//            if(world.getGameRules().get(GameRules.RANDOM_TICK_SPEED).get() != 1){
//                world.getServer().getCommandManager().handleCommand(new CommandSource(ICommandSource.DUMMY, playerEntity.getPositionVec(), Vector2f.ZERO, world, 2, "null", new StringTextComponent(""), world.getServer(), null),
//                        "/gamerule randomTickSpeed 1");
//            }
//        })))));
        STORING_EFFECT_MAP.put(Metal.ZINC, new HashSet<>(Collections.singletonList(new InstanceFactory(null, ((playerEntity, itemStack) -> {})))));
        STORING_EFFECT_MAP.put(Metal.BRASS, new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.FIRE_RESISTANCE))));
        STORING_EFFECT_MAP.put(Metal.COPPER, new HashSet<>(Collections.singletonList(new InstanceFactory(null, ((playerEntity, itemStack) -> {
            FeruchemyCapability capability = FeruchemyCapability.forPlayer(playerEntity);
            if(ExperienceUtil.removeExpFromPlayer(playerEntity, 30)){
                capability.storeExp(30);
            }
        })))));
        STORING_EFFECT_MAP.put(Metal.BRONZE, new HashSet<>(Collections.singletonList(
                new InstanceFactory(EffectRegister.BED_USE.get(), 0, 610, ((playerEntity, itemStack) -> {
                    ServerLevel world = (ServerLevel) playerEntity.level;
                    MetalMind.decreaseTimer(itemStack, MetalMind.CD);
                    if(MetalMind.getTimer(itemStack) <= 0){
                        MetalMind.setTimer(itemStack, 600);
                        EntityType.PHANTOM.spawn(world, null, playerEntity, playerEntity.blockPosition().offset(0, 2, 0), MobSpawnType.SPAWN_EGG, true, true);
                        playerEntity.getCapability(AllomancerCapability.PLAYER_CAP).ifPresent(
                                (cap) ->{
                                    int charge = 30;
                                    if (cap.isBurning(Metal.BRONZE)){
                                        charge = charge * 10;
                                    }
                                    MetalMind.addCharge(itemStack, Metal.BRONZE, charge);
                                }
                        );
                    }
                }))
        )));
        STORING_EFFECT_MAP.put(Metal.ALUMINUM, new HashSet<>(Collections.singletonList(null)));
        STORING_EFFECT_MAP.put(Metal.DURALUMIN, new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.BAD_OMEN))));
        STORING_EFFECT_MAP.put(Metal.CHROMIUM, new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.UNLUCK))));
        STORING_EFFECT_MAP.put(Metal.NICROSIL, new HashSet<>(Collections.singletonList(null)));
        STORING_EFFECT_MAP.put(Metal.GOLD, new HashSet<>(Collections.singletonList(new InstanceFactory(null, ((playerEntity, itemStack) -> {
            DamageSource damage = DamageSource.MAGIC;
            playerEntity.hurt(damage, 1f);
        })))));
        STORING_EFFECT_MAP.put(Metal.ELECTRUM, new HashSet<>(Collections.singletonList(new InstanceFactory(EffectRegister.NON_GENERATION.get(), 0, MetalMind.CD))));
        STORING_EFFECT_MAP.put(Metal.CADMIUM, new HashSet<>(Collections.singletonList(new InstanceFactory(null, ((playerEntity, itemStack) -> {
            if(! playerEntity.isEyeInFluid(FluidTags.WATER)){
                playerEntity.setAirSupply(playerEntity.getAirSupply()-5*MetalMind.CD);
                if (playerEntity.getAirSupply() <= -20) {
                    playerEntity.setAirSupply(0);
                    Vec3 vector3d = playerEntity.getDeltaMovement();

                    for(int i = 0; i < 8; ++i) {
                        double d2 = random.nextDouble() - random.nextDouble();
                        double d3 = random.nextDouble() - random.nextDouble();
                        double d4 = random.nextDouble() - random.nextDouble();
                        playerEntity.level.addParticle(ParticleTypes.BUBBLE, playerEntity.getX() + d2, playerEntity.getY() + d3, playerEntity.getZ() + d4, vector3d.x, vector3d.y, vector3d.z);
                    }

                    playerEntity.hurt(DamageSource.DROWN, 2.0F);
                }
            }
        })))));
        STORING_EFFECT_MAP.put(Metal.BENDALLOY, new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.HUNGER, ((playerEntity, itemStack) -> {
            int hunger = playerEntity.getFoodData().getFoodLevel();
            if (hunger>=2){
                playerEntity.getFoodData().setFoodLevel(hunger-1);
            }
        })))));

        TAPPING_MAP.put(Metal.IRON, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.MOVEMENT_SLOWDOWN))),
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.MOVEMENT_SLOWDOWN, 1), new InstanceFactory(MobEffects.DAMAGE_RESISTANCE, 0))),
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.MOVEMENT_SLOWDOWN, 1), new InstanceFactory(MobEffects.DAMAGE_RESISTANCE, 1)))
        ));
        TAPPING_MAP.put(Metal.STEEL, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.MOVEMENT_SPEED))),
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.MOVEMENT_SPEED, 1), new InstanceFactory(MobEffects.JUMP, 0))),
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.MOVEMENT_SPEED, 2), new InstanceFactory(MobEffects.JUMP, 1)))
        ));
        TAPPING_MAP.put(Metal.TIN, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.NIGHT_VISION, 0, 210+MetalMind.CD))),
                null,
                null
        ));
        TAPPING_MAP.put(Metal.PEWTER, Arrays.asList(
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.DAMAGE_BOOST), new InstanceFactory(MobEffects.DIG_SPEED))),
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.DAMAGE_BOOST, 1), new InstanceFactory(MobEffects.DIG_SPEED, 1), new InstanceFactory(EffectRegister.KNOCK_BACK.get(), 0))),
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.DAMAGE_BOOST, 3), new InstanceFactory(MobEffects.DIG_SPEED, 2), new InstanceFactory(EffectRegister.KNOCK_BACK.get(), 1)))
        ));
        TAPPING_MAP.put(Metal.ZINC, Arrays.asList(
                new HashSet<>(Collections.singleton(new InstanceFactory(null, ((playerEntity, itemStack) -> {})))),
                new HashSet<>(Collections.singleton(new InstanceFactory(null, ((playerEntity, itemStack) -> {})))),
                null
        ));
        BiConsumer<Player, ItemStack> destroyIce = (playerEntity, itemStack) -> {
            BlockPos pos = playerEntity.blockPosition();
            int radius = 5;
            for(int dx=-radius; dx<=radius; dx++){
                int zMax = (int) (Math.sqrt(radius*radius-dx*dx));
                for (int dz=-zMax; dz<=zMax; dz++){
                    for(int dy=-1; dy<=2; dy++){
                        BlockPos target = pos.offset(dx, dy, dz);
                        Block targetBlock = playerEntity.level.getBlockState(target).getBlock();
                        if(targetBlock== Blocks.BLUE_ICE || targetBlock == Blocks.PACKED_ICE || targetBlock == Blocks.ICE || targetBlock == Blocks.FROSTED_ICE || targetBlock == Blocks.SNOW_BLOCK || targetBlock == Blocks.SNOW){
                            if(random.nextFloat()>0.6){
                                playerEntity.level.destroyBlock(target, true);
                            }
                        }
                    }
                }
            }
        };
        TAPPING_MAP.put(Metal.BRASS, Arrays.asList(
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(null, destroyIce)
                )),
                new HashSet<>(Collections.singleton(new InstanceFactory(EffectRegister.FIRE_ASPECT.get(), 0, destroyIce))),
                null
        ));
        TAPPING_MAP.put(Metal.COPPER, Arrays.asList(
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(null, ((playerEntity, itemStack) -> {
                            FeruchemyCapability capability = FeruchemyCapability.forPlayer(playerEntity);
                            int gain = capability.gainExp(30);
                            ExperienceUtil.addExpToPlayer(playerEntity, gain);
                        }))
                )),
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(null, ((playerEntity, itemStack) -> {
                            FeruchemyCapability capability = FeruchemyCapability.forPlayer(playerEntity);
                            int gain = capability.gainExp(75);
                            ExperienceUtil.addExpToPlayer(playerEntity, gain);
                        }))
                )),
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(null, ((playerEntity, itemStack) -> {
                            FeruchemyCapability capability = FeruchemyCapability.forPlayer(playerEntity);
                            int gain = capability.gainExp(150);
                            ExperienceUtil.addExpToPlayer(playerEntity, gain);
                        }))
                ))
        ));
        TAPPING_MAP.put(Metal.BRONZE, Arrays.asList(
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(null, ((playerEntity, itemStack) -> {
                            if(MetalMind.getTimer(itemStack) > 0){
                                MetalMind.setTimer(itemStack, 0);
                                MetalMind.addCharge(itemStack, Metal.BRONZE, -10);
                            }
                        }))
                )),
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(null, ((playerEntity, itemStack) -> {
                            ServerLevel world = (ServerLevel) playerEntity.level;
                            world.setDayTime(world.getDayTime()+12000);
                            MetalMind.addCharge(itemStack, Metal.BRONZE, -30);
                            if(MetalMind.getCharge(itemStack, Metal.BRONZE)<=0){
                                MetalMind.setStatus(itemStack, MetalMind.Status.EMPTY, Metal.BRONZE);
                                MetalMind.setCharge(itemStack, Metal.BRONZE, 0);
                            }
                            else {
                                MetalMind.setStatus(itemStack, MetalMind.Status.PAUSED, Metal.BRONZE);
                            }
                        }))
                )),
                null
        ));
        TAPPING_MAP.put(Metal.ALUMINUM, Arrays.asList(
                null,
                null,
                null
        ));
        TAPPING_MAP.put(Metal.DURALUMIN, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.HERO_OF_THE_VILLAGE))),
                null,
                null
        ));
        TAPPING_MAP.put(Metal.CHROMIUM, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.LUCK))),
                new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.LUCK, 1), new InstanceFactory(EffectRegister.FORTUNE.get(), 0))),
                null
        ));
        TAPPING_MAP.put(Metal.NICROSIL, Arrays.asList(
                null,
                null,
                null
        ));
        TAPPING_MAP.put(Metal.GOLD, Arrays.asList(
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(MobEffects.REGENERATION, 1)
//                                ((playerEntity, itemStack) -> {
//                            playerEntity.heal(0.4f);})
//                        )
                )),
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(MobEffects.REGENERATION, 2)
//                        new InstanceFactory(null, ((playerEntity, itemStack) -> {
//                            playerEntity.heal(0.8f);
//                        }))
                )),
                new HashSet<>(Collections.singleton(
                        new InstanceFactory(MobEffects.REGENERATION, 3)
//                        new InstanceFactory(null, ((playerEntity, itemStack) -> {
//                            playerEntity.heal(2.33f);
//                        }))
                ))
        ));
        TAPPING_MAP.put(Metal.ELECTRUM, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.ABSORPTION, 0))),
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.ABSORPTION, 1))),
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.ABSORPTION, 2)))
        ));
        TAPPING_MAP.put(Metal.CADMIUM, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(null, ((playerEntity, itemStack) -> {
                    if(playerEntity.getAirSupply() < playerEntity.getMaxAirSupply() && playerEntity.isEyeInFluid(FluidTags.WATER)){
                        if(random.nextFloat()>0.66f){
                            playerEntity.setAirSupply(playerEntity.getAirSupply()+ MetalMind.CD);
                        }
                    }
                })))),
                new HashSet<>(Collections.singletonList(new InstanceFactory(null, ((playerEntity, itemStack) -> {
                    if(playerEntity.getAirSupply() < playerEntity.getMaxAirSupply() && playerEntity.isEyeInFluid(FluidTags.WATER)){
                        if(random.nextFloat()>0.33f){
                            playerEntity.setAirSupply(playerEntity.getAirSupply()+ MetalMind.CD);
                        }
                    }
                })))),
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.WATER_BREATHING, 0)))
        ));
        TAPPING_MAP.put(Metal.BENDALLOY, Arrays.asList(
                new HashSet<>(Collections.singletonList(new InstanceFactory(MobEffects.SATURATION))),
                null,
                null
        ));

        TAPPING_SPEED.put(Metal.IRON, Arrays.asList(1, 2, 3));
        TAPPING_SPEED.put(Metal.STEEL, Arrays.asList(1, 2, 3));
        TAPPING_SPEED.put(Metal.TIN, Arrays.asList(1, -1, -1));
        TAPPING_SPEED.put(Metal.PEWTER, Arrays.asList(1, 2, 3));
        TAPPING_SPEED.put(Metal.ZINC, Arrays.asList(2, 3, -1));
        TAPPING_SPEED.put(Metal.BRASS, Arrays.asList(1, 2, -1));
        TAPPING_SPEED.put(Metal.COPPER, Arrays.asList(1, 2, 3));
        TAPPING_SPEED.put(Metal.BRONZE, Arrays.asList(0, 0, -1));
        TAPPING_SPEED.put(Metal.ALUMINUM, Arrays.asList(1, -1, -1));
        TAPPING_SPEED.put(Metal.DURALUMIN, Arrays.asList(2, -1, -1));
        TAPPING_SPEED.put(Metal.CHROMIUM, Arrays.asList(2, 4, -1));
        TAPPING_SPEED.put(Metal.NICROSIL, Arrays.asList(0, -1, -1));
        TAPPING_SPEED.put(Metal.GOLD, Arrays.asList(2, 4, 8));
        TAPPING_SPEED.put(Metal.ELECTRUM, Arrays.asList(2, 4, 8));
        TAPPING_SPEED.put(Metal.CADMIUM, Arrays.asList(1, 2, 3));
        TAPPING_SPEED.put(Metal.BENDALLOY, Arrays.asList(5, -1, -1));

        FORTH_TAP_MAP.put(Metal.PEWTER, new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.DAMAGE_BOOST, 5), new InstanceFactory(MobEffects.DIG_SPEED, 2), new InstanceFactory(EffectRegister.KNOCK_BACK.get(), 2))));
        FORTH_TAP_MAP.put(Metal.IRON, new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.MOVEMENT_SLOWDOWN, 2), new InstanceFactory(MobEffects.DAMAGE_RESISTANCE, 2))));
        FORTH_TAP_MAP.put(Metal.STEEL, new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.MOVEMENT_SPEED, 3), new InstanceFactory(MobEffects.JUMP, 1))));
        FORTH_TAP_MAP.put(Metal.ZINC, new HashSet<>(Collections.singleton(new InstanceFactory(null, ((playerEntity, itemStack) -> {})))));
        FORTH_TAP_MAP.put(Metal.CHROMIUM, new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.LUCK, 1), new InstanceFactory(EffectRegister.FORTUNE.get(), 1))));
        FORTH_TAP_MAP.put(Metal.GOLD, new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.REGENERATION, 7))));
        FORTH_TAP_MAP.put(Metal.ELECTRUM, new HashSet<>(Arrays.asList(new InstanceFactory(MobEffects.ABSORPTION, 4))));

    }
}
