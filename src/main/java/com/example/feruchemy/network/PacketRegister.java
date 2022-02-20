package com.example.feruchemy.network;

import com.example.feruchemy.Feruchemy;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegister {
    private static final String PROTOCOL_VERSION = "1";
    private static int channel_id = 0;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Feruchemy.MOD_ID, "feruchemy_packet"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register(){
        INSTANCE.registerMessage(channel_id++, UpdateStorePacket.class, UpdateStorePacket::encode, UpdateStorePacket::new, UpdateStorePacket::handle);
        INSTANCE.registerMessage(channel_id++, FeruchemyCapabilityPacket.class, FeruchemyCapabilityPacket::encode, FeruchemyCapabilityPacket::decode, FeruchemyCapabilityPacket::handle);
    }

}
