package com.example.feruchemy.events;

import com.example.feruchemy.Feruchemy;
import com.example.feruchemy.utils.FeruStatus;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.api.enums.Metal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Feruchemy.MOD_ID)
public class CommonEventHandler {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(! event.player.level.isClientSide()){

        }
        else {
            Player player = event.player;
            FeruStatus steelStatus = FeruchemyUtils.getStatus(player, Metal.STEEL);
            if((steelStatus.tappingStatus > 1) && player.maxUpStep<1.0f){
                player.maxUpStep += 0.5f;
            }
            if((steelStatus.tappingStatus <= 1) && player.maxUpStep>1.0f){
                player.maxUpStep -= 0.5f;
            }
        }
    }
}
