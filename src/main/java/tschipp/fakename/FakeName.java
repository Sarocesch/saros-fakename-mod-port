package tschipp.fakename;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import tschipp.fakename.CommandFakeName.FakenameArgumentType;

import java.util.function.Supplier;

@Mod(FakeName.MODID)
public class FakeName {
    public static final String MODID = "fakename";

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister
            .create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MODID);

    private static final Supplier<SingletonArgumentInfo<FakenameArgumentType>> FAKENAME_ARGUMENT = COMMAND_ARGUMENT_TYPES
            .register("fakename", () -> ArgumentTypeInfos.registerByClass(FakenameArgumentType.class,
                    SingletonArgumentInfo.contextFree(FakenameArgumentType::fakename)));

    public FakeName(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        modEventBus.register(NetworkHandler.class);
    }

    public static void sendPacket(Player player, String fakename, int operation) {
        performFakenameOperation(player, fakename, operation);
        PacketDistributor.sendToAllPlayers(new FakeNamePayload(fakename, player.getId(), operation));
    }

    public static void performFakenameOperation(Player player, String fakename, int operation) {
        CompoundTag tag = player.getPersistentData();
        if (operation == 0) {
            tag.putString("fakename", fakename);
            player.refreshDisplayName();
        } else {
            tag.remove("fakename");
            player.refreshDisplayName();
        }
    }
}
