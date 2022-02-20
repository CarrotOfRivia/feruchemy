package com.example.feruchemy.client;

import com.example.feruchemy.Feruchemy;
import com.legobmw99.allomancy.modules.powers.PowersConfig;
import com.legobmw99.allomancy.modules.powers.client.gui.MetalSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Feruchemy.MOD_ID, value = Dist.CLIENT)
public class ClientEventSubscriber {
    private final Minecraft mc = Minecraft.getInstance();
    private boolean isScreenOn=false;
    public static KeyMapping storingMenu;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (storingMenu.consumeClick()) {
            Player player = this.mc.player;
            if (this.mc.screen == null) {
                if (player == null || !this.mc.isWindowActive()) {
                    return;
                }
                this.mc.setScreen(new MetalStoreScreen());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {

        if (!PowersConfig.enable_overlay.get() && !(this.mc.screen instanceof MetalSelectScreen)) {
            return;
        }
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.LAYER) {
            return;
        }
        if (!this.mc.isWindowActive() || !this.mc.player.isAlive()) {
            return;
        }
        if (this.mc.screen != null && !(this.mc.screen instanceof ChatScreen) && !(this.mc.screen instanceof MetalSelectScreen)) {
            return;
        }

        MetalOverlay.drawMetalOverlay();
    }

}
