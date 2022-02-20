package com.example.feruchemy.utils;

import com.example.feruchemy.items.MetalMind;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.function.BiConsumer;

public class InstanceFactory {
    private final MobEffect effect;
    private final int amp;
    private final int duration;
    private final BiConsumer<Player, ItemStack> otherEffects;
    public InstanceFactory(MobEffect effect, int amp, int duration, BiConsumer<Player, ItemStack> others){
        this.effect = effect;
        this.amp = amp;
        this.duration = duration;
        this.otherEffects = others;
    }

    public InstanceFactory(MobEffect effect){
        this(effect, 0);
    }

    public InstanceFactory(MobEffect effect, BiConsumer<Player, ItemStack> others){
        this(effect, 0, MetalMind.CD+5, others);
    }

    public InstanceFactory(MobEffect effect, int amp, BiConsumer<Player, ItemStack> others){
        this(effect, amp, MetalMind.CD+5, others);
    }

    public InstanceFactory(MobEffect effect, int amp){
        this(effect, amp, MetalMind.CD+5);
    }

    public InstanceFactory(MobEffect effect, int amp, int duration){
        this(effect, amp, duration, ((playerEntity, itemStack) -> {}));
    }

    public MobEffectInstance get(){
        if(effect!=null){
            return new MobEffectInstance(effect, duration, amp);
        }
        else {
            return null;
        }
    }

    public BiConsumer<Player, ItemStack> getOtherEffects() {
        return otherEffects;
    }
}
