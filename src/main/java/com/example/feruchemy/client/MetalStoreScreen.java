/*
* Code from https://github.com/legobmw99/Allomancy/tree/877c085695833c3ad53f3cba3204ffaca5396734
* */

package com.example.feruchemy.client;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.items.MetalMind;
import com.example.feruchemy.network.PacketRegister;
import com.example.feruchemy.network.UpdateStorePacket;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.modules.powers.PowersConfig;
import com.legobmw99.allomancy.modules.powers.util.AllomancyCapability;
import com.legobmw99.allomancy.network.Network;
import com.legobmw99.allomancy.setup.Metal;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class MetalStoreScreen extends Screen {
    private static final String[] METAL_NAMES = (String[]) Arrays.stream(Metal.values()).map(Metal::getDisplayName).toArray((x$0) -> {
        return new String[x$0];
    });
    private static final String GUI_METAL = "allomancy:textures/gui/metals/%s_symbol.png";
    private static final ResourceLocation[] METAL_ICONS;
    int timeIn;
    int slotSelected;
    Minecraft mc;

    public MetalStoreScreen() {
        super(new StringTextComponent("allomancy_gui"));
        this.timeIn = (Boolean) PowersConfig.animate_selection.get() ? 0 : 16;
        this.slotSelected = -1;
        this.mc = Minecraft.getInstance();
    }

    private static double mouseAngle(int x, int y, int mx, int my) {
        return (MathHelper.atan2((double)(my - y), (double)(mx - x)) + 6.283185307179586D) % 6.283185307179586D;
    }

    private static int toMetalIndex(int segment) {
        return (segment + 8) % Metal.values().length;
    }

    @Override
    public void render(MatrixStack matrixStack, int mx, int my, float partialTicks) {
        super.render(matrixStack, mx, my, partialTicks);
        int x = this.width / 2;
        int y = this.height / 2;
        int maxRadius = 80;
        double angle = mouseAngle(x, y, mx, my);
        int segments = METAL_NAMES.length;
        float step = 0.017453292F;
        float degPer = 6.2831855F / (float)segments;
        this.slotSelected = -1;
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.shadeModel(7424);
        buf.begin(6, DefaultVertexFormats.POSITION_COLOR);

        int seg;
        Metal mt;
        boolean mouseInSector;
        float radius;
        for(seg = 0; seg < segments; ++seg) {
            mouseInSector = (double)(degPer * (float)seg) < angle && angle < (double)(degPer * (float)(seg + 1));
            radius = Math.max(0.0F, Math.min(((float)this.timeIn + partialTicks - (float)seg * 6.0F / (float)segments) * 40.0F, (float)maxRadius));
            if (mouseInSector) {
                this.slotSelected = seg;
                radius *= 1.025F;
            }

            int gs = 64;
            if (seg % 2 == 0) {
                gs += 25;
            }

            // determine segment color
            int r = gs;
            int g = gs;
            int b = gs;
            ClientPlayerEntity player = mc.player;
            assert player != null;
            ItemStack itemStack = FeruchemyUtils.getMetalMindStack(player);
            if (itemStack != null){
                mt = Metal.getMetal(toMetalIndex(seg));
                MetalMind.Status status = MetalMind.getStatus(itemStack, mt);
                if((!FeruchemyCapability.canPlayerUse(player, mt) || MetalMind.getFlakeCount(itemStack, mt)==0) && !(MetalMind.getFid(itemStack)==0 && MetalMind.getCharge(itemStack, mt) > 0)){
                    r = 0x05;
                    g = 0x05;
                    b = 0x05;
                }
                else if(status == MetalMind.Status.STORING){
                    r = 0x2B;
                    g = 0xA6;
                    b = 0xED;
                }
                else if(status == MetalMind.Status.TAPPING){
                    AllomancyCapability capability = AllomancyCapability.forPlayer(player);
                    int power = MetalMind.getLevel(itemStack, mt);
                    if(capability.isBurning(mt)){
                        r = 0xFF;
                        g = 0xFF;
                        b = 0x00;
                    }
                    else {
                        switch (power){
                            case 1: {
                                r = 0x98;
                                g = 0xF9;
                                b = 0x9C;
                                break;
                            }
                            case 2: {
                                r = 0x13;
                                g = 0xDF;
                                b = 0x1D;
                                break;
                            }
                            default: {
                                r = 0x19;
                                g = 0x97;
                                b = 0x1E;
                            }
                        }
                    }
                }
            }
            int a = 153;
            if (seg == 0) {
                buf.pos((double)x, (double)y, 0.0D).color(r, gs, gs, a).endVertex();
            }

            for(float i = 0.0F; i < degPer + step / 2.0F; i += step) {
                float rad = i + (float)seg * degPer;
                float xp = (float)x + MathHelper.cos(rad) * radius;
                float yp = (float)y + MathHelper.sin(rad) * radius;
                if (i == 0.0F) {
                    buf.pos((double)xp, (double)yp, 0.0D).color(r, g, b, a).endVertex();
                }

                buf.pos((double)xp, (double)yp, 0.0D).color(r, g, b, a).endVertex();
            }
        }

        tess.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableTexture();

        for(seg = 0; seg < segments; ++seg) {
            mouseInSector = (double)(degPer * (float)seg) < angle && angle < (double)(degPer * (float)(seg + 1));
            radius = Math.max(0.0F, Math.min(((float)this.timeIn + partialTicks - (float)seg * 6.0F / (float)segments) * 40.0F, (float)maxRadius));
            if (mouseInSector) {
                radius *= 1.025F;
            }

            float rad = ((float)seg + 0.5F) * degPer;
            float xp = (float)x + MathHelper.cos(rad) * radius;
            float yp = (float)y + MathHelper.sin(rad) * radius;
            float xsp = xp - 4.0F;
            float ysp = yp;
            String name = (mouseInSector ? TextFormatting.UNDERLINE : TextFormatting.RESET) + METAL_NAMES[toMetalIndex(seg)];
            int width = this.mc.getRenderManager().getFontRenderer().getStringWidth(name);
            if (xsp < (float)x) {
                xsp -= (float)(width - 8);
            }

            if (yp < (float)y) {
                ysp = yp - 9.0F;
            }

            this.mc.getRenderManager().getFontRenderer().drawStringWithShadow(matrixStack, name, xsp, ysp, 16777215);
            double mod = 0.8D;
            int xdp = (int)((double)(xp - (float)x) * mod + (double)x);
            int ydp = (int)((double)(yp - (float)y) * mod + (double)y);
            this.mc.getRenderManager().textureManager.bindTexture(METAL_ICONS[toMetalIndex(seg)]);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            blit(matrixStack, xdp - 8, ydp - 8, 0.0F, 0.0F, 16, 16, 16, 16);
        }

        FeruchemyCapability capability = FeruchemyCapability.forPlayer(this.mc.player);
        this.mc.getRenderManager().getFontRenderer().drawString(matrixStack, "FID: "+MetalMind.toDigit(capability.getFid(), 4), this.width / 2.0f-30, this.height*0.1f, 0xFC0317);
        this.mc.getRenderManager().getFontRenderer().drawString(matrixStack, "Stored exp: "+MetalMind.toDigit(capability.getStoredExp(), 4), this.width / 2.0f-36, this.height*0.1f+9, 0x056E2D);

//        StringBuilder watermarkBuilder = new StringBuilder();
//        for (int i=0; i<9; i++){
//            watermarkBuilder.append("WATERMARK ");
//        }
//        this.mc.getRenderManager().getFontRenderer().drawString(matrixStack, watermarkBuilder.toString(), 0, this.height/2, 0X11C8D9);

        RenderSystem.enableRescaleNormal();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableStandardItemLighting();
        RenderHelper.disableStandardItemLighting();
        RenderSystem.disableBlend();
        RenderSystem.disableRescaleNormal();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        // 0: left click
        // 1: right click
        // 2: middle click
        int metalIndex = toMetalIndex(this.slotSelected);
        PacketRegister.INSTANCE.sendToServer(new UpdateStorePacket(metalIndex, mouseButton));
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        ++this.timeIn;
    }

    @Override
    public boolean keyReleased(int keysym, int scancode, int p_keyReleased_3_) {
        if (ClientEventSubscriber.storingMenu.matchesKey(keysym, scancode)) {
            this.mc.displayGuiScreen((Screen)null);
            this.mc.mouseHelper.grabMouse();
            return true;
        } else {
            return super.keyReleased(keysym, scancode, p_keyReleased_3_);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static {
        METAL_ICONS = (ResourceLocation[])Arrays.stream(METAL_NAMES).map((s) -> {
            return new ResourceLocation(String.format("feruchemy:textures/gui/metals/%s.png", s.toLowerCase()));
        }).toArray((x$0) -> {
            return new ResourceLocation[x$0];
        });
    }
}
