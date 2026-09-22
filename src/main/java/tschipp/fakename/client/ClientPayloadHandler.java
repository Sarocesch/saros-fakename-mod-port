package tschipp.fakename.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import com.mojang.authlib.GameProfile;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tschipp.fakename.FakeName;
import tschipp.fakename.FakeNamePayload;

public class ClientPayloadHandler {

    public static void handle(FakeNamePayload data, IPayloadContext context) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null)
            return;

        Player toSync = (Player) mc.level.getEntity(data.entityId());

        if (toSync != null) {
            FakeName.performFakenameOperation(toSync, data.fakename(), data.operation());

            var playerInfo = mc.player.connection.getPlayerInfo(toSync.getGameProfile().id());
            if (playerInfo != null) {
                if (data.operation() == 0) {
                    playerInfo.setTabListDisplayName(Component.literal(data.fakename()));
                } else {
                    playerInfo.setTabListDisplayName(
                            Component.literal(toSync.getGameProfile().name()));
                }
            }
        }
    }
}
