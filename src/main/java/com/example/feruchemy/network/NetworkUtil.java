package com.example.feruchemy.network;

import com.example.feruchemy.caps.FeruchemyCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

public class NetworkUtil {

    public static void sendTo(Object msg, PacketDistributor.PacketTarget target) {
        PacketRegister.INSTANCE.send(target, msg);
    }

    public static void sync(Object msg, PlayerEntity player) {
        sendTo(msg, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player));
    }

    public static void sync(PlayerEntity player) {
        FeruchemyCapability cap = FeruchemyCapability.forPlayer(player);
        sync(cap, player);
    }

    public static void sync(FeruchemyCapability cap, PlayerEntity player) {
        sync(new FeruchemyCapabilityPacket(cap, player.getEntityId()), player);
    }
}
