/*
* Code from https://github.com/legobmw99/Allomancy/tree/877c085695833c3ad53f3cba3204ffaca5396734
* */

package com.example.feruchemy.client;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.items.MetalMind;
import com.example.feruchemy.network.PacketRegister;
import com.example.feruchemy.network.UpdateStorePacket;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.api.data.IAllomancerData;
import com.legobmw99.allomancy.api.enums.Metal;
import com.legobmw99.allomancy.modules.powers.PowersConfig;
import com.legobmw99.allomancy.modules.powers.data.AllomancerCapability;
import com.legobmw99.allomancy.modules.powers.data.DefaultAllomancerData;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;

@OnlyIn(Dist.CLIENT)
public class MetalStoreScreen extends Screen {
    private static final String[] METAL_NAMES = (String[])Arrays.stream(Metal.values()).map(Metal::getName).toArray((x$0) -> {
        return new String[x$0];
    });
    private static final String[] METAL_LOCAL;
    private static final String GUI_METAL = "allomancy:textures/gui/metals/%s_symbol.png";
    private static final ResourceLocation[] METAL_ICONS;
    int timeIn;
    int slotSelected;
    Minecraft mc;

    public MetalStoreScreen() {
        super(new TextComponent("allomancy_gui"));
        this.timeIn = (Boolean) PowersConfig.animate_selection.get() ? 0 : 16;
        this.slotSelected = -1;
        this.mc = Minecraft.getInstance();
    }

    private static double mouseAngle(int x, int y, int mx, int my) {
        return (Mth.atan2((double)(my - y), (double)(mx - x)) + 6.283185307179586D) % 6.283185307179586D;
    }

    private static int toMetalIndex(int segment) {
        return (segment + 5) % Metal.values().length;
    }

    @Override
    public void render(PoseStack matrixStack, int mx, int my, float partialTicks) {
        super.render(matrixStack, mx, my, partialTicks);
        int x = this.width / 2;
        int y = this.height / 2;
        int maxRadius = 80;
        double angle = mouseAngle(x, y, mx, my);
        int segments = METAL_NAMES.length;
        float step = 0.017453292F;
        float degPer = 6.2831855F / (float)segments;
        this.slotSelected = -1;
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.getBuilder();
        RenderSystem.disableCull();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

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
            LocalPlayer player = mc.player;
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
                    IAllomancerData capability = player.getCapability(AllomancerCapability.PLAYER_CAP).orElse(new DefaultAllomancerData());
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
                buf.vertex((double)x, (double)y, 0.0D).color(r, gs, gs, a).endVertex();
            }

            for(float i = 0.0F; i < degPer + step / 2.0F; i += step) {
                float rad = i + (float)seg * degPer;
                float xp = (float)x + Mth.cos(rad) * radius;
                float yp = (float)y + Mth.sin(rad) * radius;
                if (i == 0.0F) {
                    buf.vertex((double)xp, (double)yp, 0.0D).color(r, g, b, a).endVertex();
                }

                buf.vertex((double)xp, (double)yp, 0.0D).color(r, g, b, a).endVertex();
            }
        }

        tess.end();
        RenderSystem.enableTexture();

        for(seg = 0; seg < segments; ++seg) {
            mouseInSector = (double)(degPer * (float)seg) < angle && angle < (double)(degPer * (float)(seg + 1));
            radius = Math.max(0.0F, Math.min(((float)this.timeIn + partialTicks - (float)seg * 6.0F / (float)segments) * 40.0F, (float)maxRadius));
            if (mouseInSector) {
                radius *= 1.025F;
            }

            float rad = ((float)seg + 0.5F) * degPer;
            float xp = (float)x + Mth.cos(rad) * radius;
            float yp = (float)y + Mth.sin(rad) * radius;
            float xsp = xp - 4.0F;
            float ysp = yp;
            String name = (mouseInSector ? ChatFormatting.UNDERLINE : ChatFormatting.RESET) + (new TranslatableComponent(METAL_LOCAL[toMetalIndex(seg)])).getString();
            int width = this.mc.font.width(name);
            if (xsp < (float)x) {
                xsp -= (float)(width - 8);
            }

            if (yp < (float)y) {
                ysp = yp - 9.0F;
            }

            this.mc.font.drawShadow(matrixStack, name, xsp, ysp, 16777215);
            double mod = 0.8D;
            int xdp = (int)((double)(xp - (float)x) * mod + (double)x);
            int ydp = (int)((double)(yp - (float)y) * mod + (double)y);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, METAL_ICONS[toMetalIndex(seg)]);
            blit(matrixStack, xdp - 8, ydp - 8, 0.0F, 0.0F, 16, 16, 16, 16);
        }

        FeruchemyCapability capability = FeruchemyCapability.forPlayer(this.mc.player);
        this.mc.font.draw(matrixStack, "FID: "+MetalMind.toDigit(capability.getFid(), 4), this.width / 2.0f-30, this.height*0.1f, 0xFC0317);
        this.mc.font.draw(matrixStack, "Stored exp: "+MetalMind.toDigit(capability.getStoredExp(), 4), this.width / 2.0f-36, this.height*0.1f+9, 0x056E2D);

//        StringBuilder watermarkBuilder = new StringBuilder();
//        for (int i=0; i<9; i++){
//            watermarkBuilder.append("WATERMARK ");
//        }
//        this.mc.getRenderManager().getFontRenderer().drawString(matrixStack, watermarkBuilder.toString(), 0, this.height/2, 0X11C8D9);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.disableBlend();
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
        if (ClientEventSubscriber.storingMenu.matches(keysym, scancode)) {
            this.mc.setScreen((Screen)null);
            this.mc.mouseHandler.grabMouse();
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

    static {
        METAL_LOCAL = (String[])Arrays.stream(METAL_NAMES).map((s) -> {
            return "metals." + s;
        }).toArray((x$0) -> {
            return new String[x$0];
        });
    }
}
