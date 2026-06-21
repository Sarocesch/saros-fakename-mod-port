package tschipp.fakename;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class FakenameEvents {

    @SubscribeEvent
    public static void serverLoad(RegisterCommandsEvent event) {
        CommandFakeName.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void renderName(PlayerEvent.NameFormat event) {
        CompoundTag tag = event.getEntity().getPersistentData();
        if (tag.contains("fakename")) {
            event.setDisplayname(Component.literal(tag.getString("fakename").orElse("")));
        } else {
            event.setDisplayname(event.getUsername());
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            if (player.getPersistentData().contains("fakename"))
                FakeName.sendPacket(player, player.getPersistentData().getString("fakename").orElse(""), 0);

            for (Player other : player.level().getServer().getPlayerList().getPlayers()) {
                if (other.getPersistentData().contains("fakename"))
                    PacketDistributor.sendToPlayer((ServerPlayer) player,
                            new FakeNamePacket(other.getPersistentData().getString("fakename").orElse(""),
                                    other.getId(), 0));
            }
        }
    }

    @SubscribeEvent
    public static void onTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player targetPlayer) {
            if (targetPlayer.getPersistentData() != null && targetPlayer.getPersistentData().contains("fakename")) {
                ServerPlayer toReceive = (ServerPlayer) event.getEntity();
                PacketDistributor.sendToPlayer(toReceive,
                        new FakeNamePacket(targetPlayer.getPersistentData().getString("fakename").orElse(""),
                                targetPlayer.getId(), 0));
            }
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        if (oldPlayer.getPersistentData().contains("fakename")) {
            String fakename = oldPlayer.getPersistentData().getString("fakename").orElse("");
            newPlayer.getPersistentData().putString("fakename", fakename);
        }
    }
}
