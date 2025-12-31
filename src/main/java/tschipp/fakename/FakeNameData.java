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

    public static NbtCompound getData(PlayerEntity player) {
        return PLAYER_DATA.computeIfAbsent(player.getUuid(), k -> new NbtCompound());
    }

    public static void clearData(PlayerEntity player) {
        PLAYER_DATA.remove(player.getUuid());
    }

    public static void setData(PlayerEntity player, NbtCompound data) {
        PLAYER_DATA.put(player.getUuid(), data);
    }
}
