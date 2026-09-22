package tschipp.fakename;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple persistent data storage for fakenames.
 * This replaces Forge's getPersistentData() functionality.
 */
public class FakeNameData {

    private static final Map<UUID, CompoundTag> PLAYER_DATA = new ConcurrentHashMap<>();

    public static CompoundTag getData(Player player) {
        return PLAYER_DATA.computeIfAbsent(player.getUUID(), k -> new CompoundTag());
    }

    public static void clearData(Player player) {
        PLAYER_DATA.remove(player.getUUID());
    }

    public static void setData(Player player, CompoundTag data) {
        PLAYER_DATA.put(player.getUUID(), data);
    }
}
