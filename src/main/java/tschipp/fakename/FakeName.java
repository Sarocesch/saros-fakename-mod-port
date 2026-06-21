package tschipp.fakename;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeName implements ModInitializer {

    public static final String MODID = "fakename";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("FakeName mod initializing...");
        Config.load();

        PayloadTypeRegistry.clientboundPlay().register(FakeNamePayload.TYPE, FakeNamePayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandFakeName.register(dispatcher);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            CompoundTag persistentData = getPersistentData(player);
            if (persistentData.contains("fakename")) {
                sendPacket(player, persistentData.getStringOr("fakename", ""), 0);
            }
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other == player) continue;
                CompoundTag otherData = getPersistentData(other);
                if (otherData.contains("fakename")) {
                    FakeNamePayload payload = new FakeNamePayload(
                            otherData.getStringOr("fakename", ""),
                            other.getId(), 0);
                    ServerPlayNetworking.send(player, payload);
                }
            }
        });

        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            CompoundTag oldData = getPersistentData(oldPlayer);
            if (oldData.contains("fakename")) {
                String fakename = oldData.getStringOr("fakename", "");
                getPersistentData(newPlayer).putString("fakename", fakename);
            }
        });
    }

    public static CompoundTag getPersistentData(Player player) {
        return FakeNameData.getData(player);
    }

    public static void sendPacket(ServerPlayer player, String fakename, int operation) {
        performFakenameOperation(player, fakename, operation);
        FakeNamePayload payload = new FakeNamePayload(fakename, player.getId(), operation);
        MinecraftServer server = ((net.minecraft.server.level.ServerLevel) player.level()).getServer();
        for (ServerPlayer serverPlayer : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(serverPlayer, payload);
        }
    }

    public static void performFakenameOperation(Player player, String fakename, int operation) {
        CompoundTag tag = getPersistentData(player);
        if (operation == 0) {
            tag.putString("fakename", fakename);
        } else {
            tag.remove("fakename");
        }
    }
}
