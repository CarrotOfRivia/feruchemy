package com.example.feruchemy.network;

import com.example.feruchemy.caps.FeruchemyCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FeruchemyCapabilityPacket {

    private final CompoundTag nbt;
    private final int entityID;

    public FeruchemyCapabilityPacket(FeruchemyCapability data, int entityID) {
        this(data != null ? data.serializeNBT() : new FeruchemyCapability().serializeNBT(), entityID);
    }

    private FeruchemyCapabilityPacket(CompoundTag data, int entityID) {
        this.nbt = data;
        this.entityID = entityID;
    }

    public static FeruchemyCapabilityPacket decode(FriendlyByteBuf buf) {
        return new FeruchemyCapabilityPacket(buf.readNbt(), buf.readInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(this.nbt);
        buf.writeInt(this.entityID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = (Player) Minecraft.getInstance().level.getEntity(this.entityID);
            if (player != null) {
                FeruchemyCapability playerCap = FeruchemyCapability.forPlayer(player);
                playerCap.deserializeNBT(this.nbt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
