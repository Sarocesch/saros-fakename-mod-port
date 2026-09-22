package tschipp.fakename;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.UUID;

@Environment(EnvType.CLIENT)
public class FakeNameClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FakeNamePayload.ID,
                (payload, context) -> {
                    String fakename = payload.fakename();
                    UUID uuid = payload.uuid();
                    int operation = payload.operation();

                    context.client().execute(() -> {
                        MinecraftClient client = context.client();
                        if (client.player == null || client.player.networkHandler == null)
                            return;

                        // 1. Update FakeNameData by UUID — works even when the entity
                        //    is not loaded in the world (e.g. player is far away).
                        //    The PlayerDisplayNameMixin reads from this map, so the
                        //    nametag will be correct the moment the entity loads.
                        NbtCompound tag = FakeNameData.getData(uuid);
                        if (operation == 0) {
                            tag.putString("fakename", fakename);
                        } else {
                            tag.remove("fakename");
                        }

                        // 2. Update the tab list display name by UUID — always works
                        //    regardless of render distance.
                        PlayerListEntry playerInfo = client.player.networkHandler
                                .getPlayerListEntry(uuid);
                        if (playerInfo != null) {
                            if (operation == 0) {
                                playerInfo.setDisplayName(Text.literal(fakename));
                            } else {
                                // null resets the tab list to the real profile name
                                playerInfo.setDisplayName(null);
                            }
                        }
                    });
                });
    }
}

