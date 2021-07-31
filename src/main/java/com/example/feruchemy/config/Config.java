package com.example.feruchemy.config;

import com.example.feruchemy.items.MetalMind;
import com.legobmw99.allomancy.setup.Metal;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.HashMap;

public class Config {
    public static ForgeConfigSpec COMMON;

    public static final HashMap<Metal, ForgeConfigSpec.IntValue> STORAGE = new HashMap<>();

    public static final ForgeConfigSpec.IntValue STARTING_POWER;
    public static final ForgeConfigSpec.IntValue TAPPING_START_COST;

    public static final ForgeConfigSpec.BooleanValue SERVER_RESTRICT;

    static {
        ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();

        for (Metal metal: Metal.values()){
            int defaultVal;
            if(metal == Metal.COPPER){
                defaultVal = 25;
            }
            else {
                defaultVal = 10;
            }
            STORAGE.put(metal, CONFIG_BUILDER.defineInRange("multiplier_"+metal.toString().toLowerCase(), defaultVal, 0, 100));
        }

        STARTING_POWER = CONFIG_BUILDER.comment("1: all feruchemy; 0: random").defineInRange("starting_power", 0, 0, 1);

        SERVER_RESTRICT = CONFIG_BUILDER.comment("Restrict access to zinc/bronze Feruchemy").define("server_restrict", false);

        TAPPING_START_COST = CONFIG_BUILDER.comment("start cost of tapping").defineInRange("tapping_start_cost", 1, 0, 9999999);

        COMMON = CONFIG_BUILDER.build();
    }
}
