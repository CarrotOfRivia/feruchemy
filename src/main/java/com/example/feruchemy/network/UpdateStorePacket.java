package com.example.feruchemy.network;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.config.Config;
import com.example.feruchemy.items.MetalMind;
import com.example.feruchemy.utils.FeruchemyUtils;
import com.legobmw99.allomancy.api.enums.Metal;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.PacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;


public class UpdateStorePacket {
    public int metalIndex;
    public int button;

    public UpdateStorePacket(int metalIndex, int button){
        this.metalIndex = metalIndex;
        this.button = button;
    }

    public UpdateStorePacket(final FriendlyByteBuf packetBuffer){
        this.metalIndex = packetBuffer.readInt();
        this.button = packetBuffer.readInt();
    }

    public void encode(final FriendlyByteBuf packetBuffer){
        packetBuffer.writeInt(metalIndex);
        packetBuffer.writeInt(button);
    }

    public static void handle(UpdateStorePacket packet, Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(()->{

            NetworkEvent.Context context = ctx.get();
            PacketListener handler = context.getNetworkManager().getPacketListener();

            if (handler instanceof ServerGamePacketListenerImpl){
                Metal metal = Metal.getMetal(packet.metalIndex);
                ServerPlayer player = ((ServerGamePacketListenerImpl) handler).player;
                ItemStack itemStack = FeruchemyUtils.getMetalMindStack(player);
                if (itemStack != null){
                    if (packet.button == 0 && FeruchemyUtils.canPlayerTap(player, itemStack, metal)){
                        // Tapping
                        if(MetalMind.getStatus(itemStack, metal) == MetalMind.Status.STORING){
                            FeruchemyUtils.whenStoringEnd(player, itemStack, metal);
                        }
                        MetalMind.setStatus(itemStack, MetalMind.Status.TAPPING, packet.metalIndex);
                        MetalMind.addCharge(itemStack, metal, -Config.TAPPING_START_COST.get());
                    }
                    else if(packet.button == 1 && FeruchemyUtils.canPlayerStore(player, itemStack, metal)){
                        // Storing
                        MetalMind.Status status = MetalMind.getStatus(itemStack, packet.metalIndex);
                        if(status == MetalMind.Status.TAPPING || status == MetalMind.Status.STORING){
                            // cancel tapping
                            FeruchemyUtils.whenEnd(player, itemStack, metal, status);
                            MetalMind.setStatus(itemStack, MetalMind.Status.PAUSED, packet.metalIndex);
                        }
                        else {
                            // storing
                            if(MetalMind.getFid(itemStack) == -1){
                                if(metal == Metal.ALUMINUM){
                                    MetalMind.setFid(itemStack, 0);
                                }
                                else {
                                    MetalMind.setFid(itemStack, FeruchemyCapability.forPlayer(player).getFid());
                                }
                            }
                            MetalMind.setStatus(itemStack, MetalMind.Status.STORING, metal);
                        }
                    }
                    else if(packet.button == 2){
                        // Pause all
                        FeruchemyUtils.whenEnd(player, itemStack, metal, MetalMind.getStatus(itemStack, metal));
                        MetalMind.pauseAll(itemStack);
                    }
                }
            }

            ctx.get().setPacketHandled(true);
        });
    }
}
