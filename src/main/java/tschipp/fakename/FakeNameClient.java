package tschipp.fakename;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class FakeNameClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register client-side packet handler with new API
        ClientPlayNetworking.registerGlobalReceiver(FakeNamePayload.ID,
                (payload, context) -> {
                    String fakename = payload.fakename();
                    int entityId = payload.entityId();
                    int operation = payload.operation();

                    context.client().execute(() -> {
                        MinecraftClient client = context.client();
                        if (client.world == null)
                            return;

                        PlayerEntity toSync = (PlayerEntity) client.world.getEntityById(entityId);

                        if (toSync != null) {
                            FakeName.performFakenameOperation(toSync, fakename, operation);

                            // Update tab list
                            PlayerListEntry playerInfo = client.player.networkHandler
                                    .getPlayerListEntry(toSync.getUuid());
                            if (playerInfo != null) {
                                if (operation == 0) {
                                    playerInfo.setDisplayName(Text.literal(fakename));
                                } else {
                                    playerInfo.setDisplayName(Text.literal(toSync.getGameProfile().name()));
                                }
                            }
                        }
                    });
                });
    }
}
