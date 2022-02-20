package com.example.feruchemy.items;

import com.example.feruchemy.Feruchemy;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegister {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Feruchemy.MOD_ID);

    public static final RegistryObject<Item> METAL_MIND = ITEMS.register("metal_mind", MetalMind::new);
    public static final RegistryObject<Item> THE_BAND_OF_RITUAL_MOURNING = ITEMS.register("the_band_of_ritual_mourning", ()->new MetalMind().setInf());

}
