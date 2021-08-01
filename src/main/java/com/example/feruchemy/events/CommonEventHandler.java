package com.example.feruchemy.events;

import com.example.feruchemy.Feruchemy;
import com.example.feruchemy.utils.FeruStatus;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.api.enums.Metal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Feruchemy.MOD_ID)
public class CommonEventHandler {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(! event.player.world.isRemote()){

        }
        else {
            PlayerEntity player = event.player;
            FeruStatus steelStatus = FeruchemyUtils.getStatus(player, Metal.STEEL);
            if((steelStatus.tappingStatus > 1) && player.stepHeight<1.0f){
                player.stepHeight += 0.5f;
            }
            if((steelStatus.tappingStatus <= 1) && player.stepHeight>1.0f){
                player.stepHeight -= 0.5f;
            }
        }
    }
}
