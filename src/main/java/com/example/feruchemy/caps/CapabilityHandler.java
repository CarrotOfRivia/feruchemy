package com.example.feruchemy.caps;

import com.example.feruchemy.Feruchemy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Feruchemy.MOD_ID)
public class CapabilityHandler {

    @SubscribeEvent
    public static void attachCapability(final AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player) {
            event.addCapability(FeruchemyCapability.IDENTIFIER, new FeruchemyCapability());
        }
    }
}