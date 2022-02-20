package com.example.feruchemy.client;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.items.MetalMind;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.api.enums.Metal;
import com.legobmw99.allomancy.modules.powers.PowersConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.ForgeIngameGui;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MetalOverlay {
    private static final Point[] Frames = new Point[4];
    private static int animationCounter = 0;
    private static int currentFrame = 0;

    static {
        int x = 0;
        int firsty = 22;
        for (int i = 0; i < 4; i++) {
            Frames[i] = new Point(x, firsty + (4 * i));
        }
    }

    /**
     * Draws the overlay for the metals, adapted from Allomancy mod
     */
    public static void drawMetalOverlay() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Window res = mc.getWindow();

        if (!player.isAlive()) {
            return;
        }

        ItemStack itemStack = FeruchemyUtils.getMetalMindStack(player);
        if(itemStack == null){
            return;
        }

        int renderX, renderY;

        // Set the offsets of the overlay based on config
        switch (PowersConfig.overlay_position.get()) {
            case TOP_RIGHT:
                renderX = res.getGuiScaledWidth() - 145;
                renderY = 10;
                break;
            case BOTTOM_RIGHT:
                renderX = res.getGuiScaledWidth() - 145;
                renderY = res.getGuiScaledHeight() - 50;
                break;
            case BOTTOM_LEFT:
                renderX = 5;
                renderY = res.getGuiScaledHeight() - 50;
                break;
            default: // TOP_LEFT
                renderX = 5;
                renderY = 10;
                break;
        }

        ForgeIngameGui gui = new ForgeIngameGui(mc);
        mc.getTextureManager().bindForSetup(meterLoc);
        AbstractTexture obj;
        obj = mc.getTextureManager().getTexture(meterLoc);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, obj.getId());


        /*
         * The rendering for the overlay
         */

        for (Metal metal : Metal.values()) {
            if (FeruchemyCapability.canPlayerUse(player, metal)) {
                int maxStorage = MetalMind.getMaxStorage(itemStack, metal);
                if (maxStorage == 0){
                    maxStorage = 1;
                }
                int metalY = (int) (11.0 * (maxStorage - Math.max(0, MetalMind.getCharge(itemStack, metal))) / (maxStorage))-1;
                int i = metal.getIndex();
                int offset = (i / 2) * 4; // Adding a gap between pairs
                // Draw the bars first
                blit(renderX + 1 + (7 * i) + offset, renderY + 25 + metalY, 7 + (6 * i), 1 + metalY, 3, 10 - metalY);
                // Draw the gauges second, so that highlights and decorations show over the bar.
                blit(renderX + (7 * i) + offset, renderY + 20, 0, 0, 5, 20);
                // Draw the fire if it is burning
                MetalMind.Status status = MetalMind.getStatus(itemStack, metal);
                if (status == MetalMind.Status.STORING || status == MetalMind.Status.TAPPING) {
                    blit(renderX + (7 * i) + offset, renderY + 24 + metalY, Frames[currentFrame].x, Frames[currentFrame].y, 5, 3);
                }
            }

        }

        // Update the animation counters
        animationCounter++;
        if (animationCounter > 6) {
            animationCounter = 0;
            currentFrame++;
            if (currentFrame > 3) {
                currentFrame = 0;
            }
        }
    }

    private static void blit(int x, int y, float uOffset, float vOffset, int uWidth, int vHeight) {
        ForgeIngameGui gui = new ForgeIngameGui(Minecraft.getInstance());
        ForgeIngameGui.blit(new PoseStack(), x, y, gui.getBlitOffset(), uOffset, vOffset, uWidth, vHeight, 128, 128);
    }


    private static final ResourceLocation meterLoc = new ResourceLocation("allomancy", "textures/gui/overlay/meter.png");
}
