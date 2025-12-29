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

    public FakeNamePacket(FriendlyByteBuf buf) {
        this.fakename = buf.readUtf();
        this.entityId = buf.readInt();
        this.deleteFakename = buf.readInt();
    }

    public FakeNamePacket() {

    }

    public FakeNamePacket(String fakename, int entityID, int delete) {
        this.fakename = fakename;
        this.entityId = entityID;
        this.deleteFakename = delete;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(fakename);
        buf.writeInt(entityId);
        buf.writeInt(deleteFakename);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        // Since we use consumerMainThread, work is already enqueued on the main thread
        Minecraft mc = Minecraft.getInstance();

        Player toSync = (Player) mc.level.getEntity(entityId);

        if (toSync != null) {
            FakeName.performFakenameOperation(toSync, fakename, deleteFakename);

            if (deleteFakename == 0)
                mc.player.connection.getPlayerInfo(toSync.getGameProfile().getId())
                        .setTabListDisplayName(Component.literal(fakename));
            else
                mc.player.connection.getPlayerInfo(toSync.getGameProfile().getId())
                        .setTabListDisplayName(Component.literal(toSync.getGameProfile().getName()));
        }

        ctx.setPacketHandled(true);
    }

}
