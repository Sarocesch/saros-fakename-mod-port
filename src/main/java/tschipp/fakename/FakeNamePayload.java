package tschipp.fakename;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FakeNamePayload(String fakename, int entityId, int operation)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FakeNamePayload> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(FakeName.MODID, "fakename"));

    public static final StreamCodec<ByteBuf, FakeNamePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FakeNamePayload::fakename,
            ByteBufCodecs.VAR_INT, FakeNamePayload::entityId,
            ByteBufCodecs.VAR_INT, FakeNamePayload::operation,
            FakeNamePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
