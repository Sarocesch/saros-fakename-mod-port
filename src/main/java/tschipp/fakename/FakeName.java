package tschipp.fakename;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FakeName.MODID)
public class FakeName {
    public static final String MODID = "fakename";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public FakeName(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);

        IEventBus modBus = modContainer.getEventBus();
        modBus.addListener(this::onRegisterPayloads);
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(FakeNamePacket.TYPE, FakeNamePacket.STREAM_CODEC, FakeNamePacket::handleClient);
    }

    public static void sendPacket(Player player, String fakename, int operation) {
        performFakenameOperation(player, fakename, operation);
        PacketDistributor.sendToAllPlayers(new FakeNamePacket(fakename, player.getId(), operation));
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
