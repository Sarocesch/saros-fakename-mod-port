package tschipp.fakename;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeName implements ModInitializer {

    public static final String MODID = "fakename";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("FakeName mod initializing...");

        // Load config
        Config.load();

        // Register payload type for server-to-client packets
        PayloadTypeRegistry.playS2C().register(FakeNamePayload.ID, FakeNamePayload.CODEC);

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandFakeName.register(dispatcher);
        });

        // Player join event - sync fakenames to joining player
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;

            // Send this player's fakename to everyone if they have one
            NbtCompound persistentData = getPersistentData(player);
            if (persistentData.contains("fakename")) {
                sendPacket(player, persistentData.getString("fakename").orElse(""), 0);
            }

            // Send all other players' fakenames to this player
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                if (other == player)
                    continue;
                NbtCompound otherData = getPersistentData(other);
                if (otherData.contains("fakename")) {
                    FakeNamePayload payload = new FakeNamePayload(
                            otherData.getString("fakename").orElse(""),
                            other.getId(),
                            0);
                    ServerPlayNetworking.send(player, payload);
                }
            }
        });

        // Player death/respawn - copy fakename data
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            NbtCompound oldData = getPersistentData(oldPlayer);
            if (oldData.contains("fakename")) {
                String fakename = oldData.getString("fakename").orElse("");
                getPersistentData(newPlayer).putString("fakename", fakename);
            }
        });
    }

    public static NbtCompound getPersistentData(PlayerEntity player) {
        return FakeNameData.getData(player);
    }

    public static void sendPacket(ServerPlayerEntity player, String fakename, int operation) {
        performFakenameOperation(player, fakename, operation);

        FakeNamePayload payload = new FakeNamePayload(fakename, player.getId(), operation);

        // Send to all players
        MinecraftServer server = player.getEntityWorld().getServer();
        for (ServerPlayerEntity serverPlayer : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(serverPlayer, payload);
        }
    }

    public static void performFakenameOperation(PlayerEntity player, String fakename, int operation) {
        NbtCompound tag = getPersistentData(player);

        if (operation == 0) {
            tag.putString("fakename", fakename);
        } else {
            tag.remove("fakename");
        }
    }
}
