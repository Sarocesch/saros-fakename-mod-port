package tschipp.fakename;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple persistent data storage for fakenames.
 * This replaces Forge's getPersistentData() functionality.
 */
public class FakeNameData {

    private static final Map<UUID, NbtCompound> PLAYER_DATA = new ConcurrentHashMap<>();

    /** Convenience overload for server-side code that has a PlayerEntity. */
    public static NbtCompound getData(PlayerEntity player) {
        return getData(player.getUuid());
    }

    /** UUID-based accessor used on both sides (client packet handler, server). */
    public static NbtCompound getData(UUID uuid) {
        return PLAYER_DATA.computeIfAbsent(uuid, k -> new NbtCompound());
    }

    public static void clearData(PlayerEntity player) {
        PLAYER_DATA.remove(player.getUuid());
    }

    public static void setData(PlayerEntity player, NbtCompound data) {
        PLAYER_DATA.put(player.getUuid(), data);
    }
}
