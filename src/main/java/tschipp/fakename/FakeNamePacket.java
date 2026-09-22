package tschipp.fakename;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class FakeNamePacket {
    public String fakename;
    public int entityId;
    public int deleteFakename;

    public FakeNamePacket(String fakename, int entityId, int deleteFakename) {
        this.fakename = fakename;
        this.entityId = entityId;
        this.deleteFakename = deleteFakename;
    }

    public FakeNamePacket(FriendlyByteBuf buf) {
        this.fakename = buf.readUtf();
        this.entityId = buf.readInt();
        this.deleteFakename = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(fakename);
        buf.writeInt(entityId);
        buf.writeInt(deleteFakename);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null)
                return;

            Player toSync = (Player) mc.level.getEntity(entityId);

            if (toSync != null) {
                FakeName.performFakenameOperation(toSync, fakename, deleteFakename);

                if (mc.player != null && mc.player.connection != null) {
                    if (deleteFakename == 0) {
                        try {
                            var playerInfo = mc.player.connection.getPlayerInfo(toSync.getGameProfile().getId());
                            if (playerInfo != null)
                                playerInfo.setTabListDisplayName(Component.literal(fakename));
                        } catch (Exception e) {
                        }
                    } else {
                        try {
                            var playerInfo = mc.player.connection.getPlayerInfo(toSync.getGameProfile().getId());
                            if (playerInfo != null)
                                playerInfo.setTabListDisplayName(Component.literal(toSync.getGameProfile().getName()));
                        } catch (Exception e) {
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
