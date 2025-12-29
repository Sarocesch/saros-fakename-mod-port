package tschipp.fakename;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tschipp.fakename.CommandFakeName.FakenameArgumentType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(bus = Bus.MOD)
@Mod(FakeName.MODID)
public class FakeName {
    public static final String MODID = "fakename";

    public static SimpleChannel network;

    public static IModInfo info;

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister
            .create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, MODID);
    private static final RegistryObject<SingletonArgumentInfo<FakenameArgumentType>> FAKENAME_ARGUMENT = COMMAND_ARGUMENT_TYPES
            .register("fakename", () -> {
                return ArgumentTypeInfos.registerByClass(FakenameArgumentType.class,
                        SingletonArgumentInfo.contextFree(FakenameArgumentType::fakename));
            });

    @SuppressWarnings("removal")
    public FakeName(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::setup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

        info = ModLoadingContext.get().getActiveContainer().getModInfo();

        COMMAND_ARGUMENT_TYPES.register(modEventBus);

        Logger logger = LogManager.getLogger(MODID);
        InputStream in = FakeName.class.getClassLoader().getResourceAsStream("fakename.mixins.json");
        if (in == null) {
            logger.error("fakename.mixins.json not found on classpath");
        } else {
            try {
                byte[] data = in.readAllBytes();
                String s = new String(data, StandardCharsets.UTF_8);
                if (!s.trim().startsWith("{")) {
                    logger.error("fakename.mixins.json is not valid JSON content");
                } else {
                    logger.info("fakename.mixins.json loaded ({} bytes)", data.length);
                }
            } catch (Exception e) {
                logger.error("Failed to read fakename.mixins.json", e);
            }
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FakeName.network = ChannelBuilder
                    .named(ResourceLocation.fromNamespaceAndPath(FakeName.MODID, "fakenamechannel"))
                    .serverAcceptedVersions((status, version) -> true)
                    .clientAcceptedVersions((status, version) -> true)
                    .networkProtocolVersion(1)
                    .simpleChannel();

            FakeName.network.messageBuilder(FakeNamePacket.class, NetworkDirection.PLAY_TO_CLIENT)
                    .encoder(FakeNamePacket::toBytes)
                    .decoder(FakeNamePacket::new)
                    .consumerMainThread(FakeNamePacket::handle)
                    .add();
        });

    }

    public static void sendPacket(Player player, String fakename, int operation) {
        performFakenameOperation(player, fakename, operation);
        FakeName.network.send(new FakeNamePacket(fakename, player.getId(), operation), PacketDistributor.ALL.noArg());
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
