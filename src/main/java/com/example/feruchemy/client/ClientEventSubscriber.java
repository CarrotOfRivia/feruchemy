package com.example.feruchemy.client;

import com.example.feruchemy.Feruchemy;
import com.legobmw99.allomancy.modules.powers.PowersConfig;
import com.legobmw99.allomancy.modules.powers.client.gui.MetalSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
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
    public static KeyBinding storingMenu;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (storingMenu.isPressed()) {
            PlayerEntity player = this.mc.player;
            if (this.mc.currentScreen == null) {
                if (player == null || !this.mc.isGameFocused()) {
                    return;
                }
                this.mc.displayGuiScreen(new MetalStoreScreen());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {

        if (!PowersConfig.enable_overlay.get() && !(this.mc.currentScreen instanceof MetalSelectScreen)) {
            return;
        }
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }
        if (!this.mc.isGameFocused() || !this.mc.player.isAlive()) {
            return;
        }
        if (this.mc.currentScreen != null && !(this.mc.currentScreen instanceof ChatScreen) && !(this.mc.currentScreen instanceof MetalSelectScreen)) {
            return;
        }

        MetalOverlay.drawMetalOverlay();
    }

}
