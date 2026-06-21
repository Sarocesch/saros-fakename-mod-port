package tschipp.fakename;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record FakeNamePayload(String fakename, int entityId, int operation) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<FakeNamePayload> TYPE = new CustomPacketPayload.Type<>(
            Identifier.fromNamespaceAndPath(FakeName.MODID, "fakename_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FakeNamePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FakeNamePayload::fakename,
            ByteBufCodecs.INT, FakeNamePayload::entityId,
            ByteBufCodecs.INT, FakeNamePayload::operation,
            FakeNamePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
