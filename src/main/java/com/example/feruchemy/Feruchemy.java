package com.example.feruchemy;

import com.example.feruchemy.client.ClientEventSubscriber;
import com.example.feruchemy.commands.FeruchemyCommand;
import com.example.feruchemy.config.Config;
import com.example.feruchemy.effects.EffectRegister;
import com.example.feruchemy.events.CommonEventHandler;
import com.example.feruchemy.events.ServerEventHandler;
import com.example.feruchemy.items.ItemRegister;
import com.example.feruchemy.network.PacketRegister;
import com.example.feruchemy.utils.ExternalMods;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.api.SlotTypeMessage;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Feruchemy.MOD_ID)
public class Feruchemy
{
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "feruchemy";

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab("feruchemy") {
        @Override
        public @NotNull ItemStack makeIcon() {
            return new ItemStack(ItemRegister.METAL_MIND.get());
        }
    };

    public Feruchemy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        modEventBus.addListener(this::setup);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(this::doClientStuff);
        modEventBus.addListener(this::onModCommunicate);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ItemRegister.ITEMS.register(modEventBus);
        EffectRegister.EFFECT.register(modEventBus);

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        PacketRegister.register();

    }

    private void doClientStuff(final FMLClientSetupEvent event) {

        MinecraftForge.EVENT_BUS.register(new ClientEventSubscriber());
        ClientEventSubscriber.storingMenu = new KeyMapping("key."+MOD_ID+".storing_menu", GLFW.GLFW_KEY_N, "key."+MOD_ID);
        ClientRegistry.registerKeyBinding(ClientEventSubscriber.storingMenu);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
    }

    private void onModCommunicate(final InterModEnqueueEvent event){
        if(ExternalMods.CURIOS.isLoaded()){
            InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> new SlotTypeMessage.Builder("metal_mind").size(1).build());
        }
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onCommand(final RegisterCommandsEvent event) {
            // register a new block here
            FeruchemyCommand.register(event.getDispatcher());
        }
    }
}
