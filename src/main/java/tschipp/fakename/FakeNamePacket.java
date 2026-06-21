package tschipp.fakename;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FakeNamePacket(String fakename, int entityId, int deleteFakename) implements CustomPacketPayload {

    public static final Type<FakeNamePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(FakeName.MODID, "fakename_sync"));

    public static final StreamCodec<ByteBuf, FakeNamePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FakeNamePacket::fakename,
            ByteBufCodecs.VAR_INT, FakeNamePacket::entityId,
            ByteBufCodecs.VAR_INT, FakeNamePacket::deleteFakename,
            FakeNamePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(FakeNamePacket packet, IPayloadContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Player toSync = (Player) mc.level.getEntity(packet.entityId());
        if (toSync != null) {
            FakeName.performFakenameOperation(toSync, packet.fakename(), packet.deleteFakename());

            if (packet.deleteFakename() == 0)
                mc.player.connection.getPlayerInfo(toSync.getGameProfile().id())
                        .setTabListDisplayName(Component.literal(packet.fakename()));
            else
                mc.player.connection.getPlayerInfo(toSync.getGameProfile().id())
                        .setTabListDisplayName(Component.literal(toSync.getGameProfile().name()));
        }
    }
}
