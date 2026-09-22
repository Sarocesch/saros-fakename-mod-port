package tschipp.fakename;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class FakeNameClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FakeNamePayload.TYPE,
                (payload, context) -> {
                    String fakename = payload.fakename();
                    int entityId = payload.entityId();
                    int operation = payload.operation();

                    context.client().execute(() -> {
                        Minecraft client = context.client();
                        if (client.level == null) return;

                        Player toSync = (Player) client.level.getEntity(entityId);

                        if (toSync != null) {
                            FakeName.performFakenameOperation(toSync, fakename, operation);

                            PlayerInfo playerInfo = client.player.connection
                                    .getPlayerInfo(toSync.getUUID());
                            if (playerInfo != null) {
                                if (operation == 0) {
                                    playerInfo.setTabListDisplayName(Component.literal(fakename));
                                } else {
                                    playerInfo.setTabListDisplayName(Component.literal(toSync.getGameProfile().name()));
                                }
                            }
                        }
                    });
                });
    }
}
