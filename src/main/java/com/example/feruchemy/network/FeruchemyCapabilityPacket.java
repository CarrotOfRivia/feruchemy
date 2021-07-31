package com.example.feruchemy.network;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.legobmw99.allomancy.modules.powers.network.AllomancyCapabilityPacket;
import com.legobmw99.allomancy.modules.powers.util.AllomancyCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class FeruchemyCapabilityPacket {

    private final CompoundNBT nbt;
    private final int entityID;

    public FeruchemyCapabilityPacket(FeruchemyCapability data, int entityID) {
        this(data != null ? data.serializeNBT() : new FeruchemyCapability().serializeNBT(), entityID);
    }

    private FeruchemyCapabilityPacket(CompoundNBT data, int entityID) {
        this.nbt = data;
        this.entityID = entityID;
    }

    public static FeruchemyCapabilityPacket decode(PacketBuffer buf) {
        return new FeruchemyCapabilityPacket(buf.readCompoundTag(), buf.readInt());
    }

    public void encode(PacketBuffer buf) {
        buf.writeCompoundTag(this.nbt);
        buf.writeInt(this.entityID);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = (PlayerEntity) Minecraft.getInstance().world.getEntityByID(this.entityID);
            if (player != null) {
                FeruchemyCapability playerCap = FeruchemyCapability.forPlayer(player);
                playerCap.deserializeNBT(this.nbt);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
