package com.example.feruchemy.utils;

import com.example.feruchemy.items.MetalMind;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;

import java.util.function.BiConsumer;

public class InstanceFactory {
    private final Effect effect;
    private final int amp;
    private final int duration;
    private final BiConsumer<PlayerEntity, ItemStack> otherEffects;
    public InstanceFactory(Effect effect, int amp, int duration, BiConsumer<PlayerEntity, ItemStack> others){
        this.effect = effect;
        this.amp = amp;
        this.duration = duration;
        this.otherEffects = others;
    }

    public InstanceFactory(Effect effect){
        this(effect, 0);
    }

    public InstanceFactory(Effect effect, BiConsumer<PlayerEntity, ItemStack> others){
        this(effect, 0, MetalMind.CD+5, others);
    }

    public InstanceFactory(Effect effect, int amp, BiConsumer<PlayerEntity, ItemStack> others){
        this(effect, amp, MetalMind.CD+5, others);
    }

    public InstanceFactory(Effect effect, int amp){
        this(effect, amp, MetalMind.CD+5);
    }

    public InstanceFactory(Effect effect, int amp, int duration){
        this(effect, amp, duration, ((playerEntity, itemStack) -> {}));
    }

    public EffectInstance get(){
        if(effect!=null){
            return new EffectInstance(effect, duration, amp);
        }
        else {
            return null;
        }
    }

    public BiConsumer<PlayerEntity, ItemStack> getOtherEffects() {
        return otherEffects;
    }
}
