package com.example.feruchemy.utils;

import com.example.feruchemy.caps.FeruchemyCapability;
import com.example.feruchemy.items.ItemRegister;
import com.example.feruchemy.items.MetalMind;
import com.legobmw99.allomancy.api.data.IAllomancerData;
import com.legobmw99.allomancy.api.enums.Metal;
import com.legobmw99.allomancy.modules.materials.MaterialsSetup;
import com.legobmw99.allomancy.modules.powers.data.AllomancerCapability;
import com.legobmw99.allomancy.modules.powers.data.DefaultAllomancerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec2;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.util.ICuriosHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeruchemyUtils {
    public static final List<Item> FLAKE_ITEMS = new ArrayList<>();
    static {
        for (int i=0; i<Metal.values().length; i++){
            FLAKE_ITEMS.add(MaterialsSetup.FLAKES.get(i).get());
        }
    }

    public static ItemStack getMetalMindStack(Player player){
        // search create band first
        // I should've written it into a function
        if(ExternalMods.CURIOS.isLoaded()){
            ICuriosHelper helper = CuriosApi.getCuriosHelper();
            Optional<ImmutableTriple<String, Integer, ItemStack>> tmp = helper.findEquippedCurio(ItemRegister.THE_BAND_OF_RITUAL_MOURNING.get(), player);
            if(tmp.isPresent()){
                ItemStack stack = tmp.get().getRight();
                if (stack.getItem() instanceof MetalMind){
                    return stack;
                }
            }

            tmp = helper.findEquippedCurio(ItemRegister.METAL_MIND.get(), player);
            if(tmp.isPresent()){
                ItemStack stack = tmp.get().getRight();
                if (stack.getItem() instanceof MetalMind){
                    return stack;
                }
            }
        }

        for(int i=0; i<9; i++){
            ItemStack stack = player.inventory.getItem(i);
            if(stack.getItem() instanceof MetalMind){
                return stack;
            }
        }
        return null;
    }

    public static boolean canPlayerTap(Player playerEntity, ItemStack itemStack, Metal metal){
        if(itemStack.getItem() instanceof MetalMind && ((MetalMind) itemStack.getItem()).isInf()){
            return true;
        }

        FeruchemyCapability capability = FeruchemyCapability.forPlayer(playerEntity);

        int playerFid = capability.getFid();
        int itemFid = MetalMind.getFid(itemStack);

        boolean condition = FeruchemyCapability.canPlayerUse(playerEntity, metal);

        return (condition && (playerFid == itemFid)) || (itemFid == 0);
    }


    public static boolean canPlayerStore(Player playerEntity, ItemStack itemStack, Metal metal){
        if(itemStack.getItem() instanceof MetalMind && ((MetalMind) itemStack.getItem()).isInf()){
            return true;
        }

        FeruchemyCapability capability = FeruchemyCapability.forPlayer(playerEntity);

        int playerFid = capability.getFid();
        int itemFid = MetalMind.getFid(itemStack);

        MetalMind.Status statusAluminum = MetalMind.getStatus(itemStack, Metal.ALUMINUM);
        boolean condition1 = (itemFid == 0) && ((metal==Metal.ALUMINUM) || (statusAluminum== MetalMind.Status.STORING));

        MetalMind.Status status = MetalMind.getStatus(itemStack, metal);
        boolean condition2 = (status == MetalMind.Status.TAPPING || status == MetalMind.Status.STORING);

        boolean condition3 = FeruchemyCapability.canPlayerUse(playerEntity, metal);

        return (condition3 && ((playerFid == itemFid) || condition1 || (itemFid == -1))) || condition2;
    }

    public static void whenEnd(Player playerEntity, ItemStack itemStack, Metal metal, MetalMind.Status status){
        if(status == MetalMind.Status.TAPPING){
            whenTappingEnd(playerEntity, itemStack, metal);
        }
        else if(status == MetalMind.Status.STORING){
            whenStoringEnd(playerEntity, itemStack, metal);
        }
    }


    public static void whenStoringEnd(Player playerEntity, ItemStack itemStack, Metal metal){
        // called when storing/tapping ends
        if(! playerEntity.level.isClientSide()){
            if(metal == Metal.TIN){
                MobEffectInstance effectInstance = playerEntity.getEffect(MobEffects.BLINDNESS);
                if(effectInstance!=null && effectInstance.getDuration() <= 3*MetalMind.CD){
                    playerEntity.removeEffect(MobEffects.BLINDNESS);
                }
            }
            else if(metal == Metal.ZINC){
                ServerLevel world = (ServerLevel) playerEntity.level;
                world.getServer().getCommands().performCommand(new CommandSourceStack(CommandSource.NULL, playerEntity.position(), Vec2.ZERO, world, 2, "null", new TextComponent(""), world.getServer(), null),
                        "/gamerule randomTickSpeed 3");
            }
        }
    }

    public static void whenTappingEnd(Player playerEntity, ItemStack itemStack, Metal metal) {
        // called when storing/tapping ends
        if (!playerEntity.level.isClientSide()) {
            if(metal == Metal.TIN){
                MobEffectInstance effectInstance = playerEntity.getEffect(MobEffects.NIGHT_VISION);
                if(effectInstance!=null && effectInstance.getDuration() <= 210+MetalMind.CD){
                    playerEntity.removeEffect(MobEffects.NIGHT_VISION);
                }
            }
            else if(metal == Metal.ZINC){
                ServerLevel world = (ServerLevel) playerEntity.level;
                world.getServer().getCommands().performCommand(new CommandSourceStack(CommandSource.NULL, playerEntity.position(), Vec2.ZERO, world, 2, "null", new TextComponent(""), world.getServer(), null),
                        "/gamerule doDaylightCycle true");
            }
        }
    }

    public static FeruStatus getStatus(Player playerEntity, Metal metal){
        ItemStack metalMind = getMetalMindStack(playerEntity);
        if (metalMind != null){
            switch (MetalMind.getStatus(metalMind, metal)){
                case STORING:
                    return new FeruStatus(1, 0);
                case TAPPING:
                    IAllomancerData capability = playerEntity.getCapability(AllomancerCapability.PLAYER_CAP).orElse(new DefaultAllomancerData());
                    if(capability.isBurning(metal)){
                        return new FeruStatus(0, 4);
                    }
                    else {
                        return new FeruStatus(0, MetalMind.getLevel(metalMind, metal));
                    }
                default:
                    return new FeruStatus(0, 0);
            }
        }
        else {
            return new FeruStatus(0, 0);
        }
    }


}
