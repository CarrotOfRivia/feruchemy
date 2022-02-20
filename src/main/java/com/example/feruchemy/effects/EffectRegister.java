package com.example.feruchemy.effects;

import com.example.feruchemy.Feruchemy;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectRegister {
    public static final DeferredRegister<MobEffect> EFFECT = DeferredRegister.create(ForgeRegistries.POTIONS, Feruchemy.MOD_ID);

    public static final RegistryObject<MyEffect> FIRE_ASPECT = EFFECT.register("fire_aspect", ()->new MyEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<MyEffect> KNOCK_BACK = EFFECT.register("knock_back", ()->new MyEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<MyEffect> BED_USE = EFFECT.register("bed_use", ()->new MyEffect(MobEffectCategory.BENEFICIAL, 0));
    public static final RegistryObject<MyEffect> FORTUNE = EFFECT.register("fortune", ()->new MyEffect(MobEffectCategory.BENEFICIAL, 0));

    public static final RegistryObject<MyEffect> NON_GENERATION = EFFECT.register("non_generation", ()->new MyEffect(MobEffectCategory.HARMFUL, 0));
    public static final RegistryObject<MyEffect> SUFFOCATE = EFFECT.register("suffocate", ()->new MyEffect(MobEffectCategory.HARMFUL, 0));
}
