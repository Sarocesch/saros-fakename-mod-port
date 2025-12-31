package tschipp.fakename;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FakeName implements ModInitializer {

    public static final String MODID = "fakename";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static final Identifier FAKENAME_PACKET_ID = new Identifier(MODID, "fakename_sync");

    @Override
    public void onInitialize() {
        LOGGER.info("FakeName mod initializing...");

        // Load config
        Config.load();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandFakeName.register(dispatcher);
        });

        // Player join event - sync fakenames to joining player
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;

            // Send this player's fakename to everyone if they have one
            if (player.getCommandTags().contains("fakename") ||
                    (player.writeNbt(new NbtCompound()).contains("fakename"))) {
                NbtCompound persistentData = getPersistentData(player);
                if (persistentData.contains("fakename")) {
                    sendPacket(player, persistentData.getString("fakename"), 0);
                }
            }

            // Send all other players' fakenames to this player
            for (ServerPlayerEntity other : server.getPlayerManager().getPlayerList()) {
                NbtCompound otherData = getPersistentData(other);
                if (otherData.contains("fakename")) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeString(otherData.getString("fakename"));
                    buf.writeInt(other.getId());
                    buf.writeInt(0);
                    ServerPlayNetworking.send(player, FAKENAME_PACKET_ID, buf);
                }
            }
        });

        // Player death/respawn - copy fakename data
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            NbtCompound oldData = getPersistentData(oldPlayer);
            if (oldData.contains("fakename")) {
                String fakename = oldData.getString("fakename");
                getPersistentData(newPlayer).putString("fakename", fakename);
            }
        });
    }

    public static NbtCompound getPersistentData(PlayerEntity player) {
        // Use the player's custom data NBT for persistence
        NbtCompound nbt = new NbtCompound();
        player.writeNbt(nbt);

        // We need to use a dedicated storage - let's use the player's persistent data
        // In Fabric, we can use DataAttachments or custom NBT field
        // For simplicity, we'll access the player's NBT directly via a field
        // Actually, Fabric doesn't have getPersistentData like Forge
        // We need a different approach - using a static map or similar
        return FakeNameData.getData(player);
    }

    public static void sendPacket(PlayerEntity player, String fakename, int operation) {
        performFakenameOperation(player, fakename, operation);

        if (player.getWorld().isClient())
            return;

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(fakename);
        buf.writeInt(player.getId());
        buf.writeInt(operation);

        // Send to all players
        for (ServerPlayerEntity serverPlayer : PlayerLookup.all(player.getServer())) {
            ServerPlayNetworking.send(serverPlayer, FAKENAME_PACKET_ID, buf);
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
