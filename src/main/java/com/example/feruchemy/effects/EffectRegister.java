package com.example.feruchemy.effects;

import com.example.feruchemy.Feruchemy;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectRegister {
    public static final DeferredRegister<Effect> EFFECT = DeferredRegister.create(ForgeRegistries.POTIONS, Feruchemy.MOD_ID);

    public static final RegistryObject<MyEffect> FIRE_ASPECT = EFFECT.register("fire_aspect", ()->new MyEffect(EffectType.BENEFICIAL, 0));
    public static final RegistryObject<MyEffect> KNOCK_BACK = EFFECT.register("knock_back", ()->new MyEffect(EffectType.BENEFICIAL, 0));
    public static final RegistryObject<MyEffect> BED_USE = EFFECT.register("bed_use", ()->new MyEffect(EffectType.BENEFICIAL, 0));
    public static final RegistryObject<MyEffect> FORTUNE = EFFECT.register("fortune", ()->new MyEffect(EffectType.BENEFICIAL, 0));

    public static final RegistryObject<MyEffect> NON_GENERATION = EFFECT.register("non_generation", ()->new MyEffect(EffectType.HARMFUL, 0));
    public static final RegistryObject<MyEffect> SUFFOCATE = EFFECT.register("suffocate", ()->new MyEffect(EffectType.HARMFUL, 0));
}
