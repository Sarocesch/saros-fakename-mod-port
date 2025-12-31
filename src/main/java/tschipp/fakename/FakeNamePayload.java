package tschipp.fakename;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * CustomPayload record for syncing fakename data between server and clients.
 * Uses the new Fabric 1.21+ networking API pattern.
 */
public record FakeNamePayload(String fakename, int entityId, int operation) implements CustomPayload {

    public static final CustomPayload.Id<FakeNamePayload> ID = new CustomPayload.Id<>(
            Identifier.of(FakeName.MODID, "fakename_sync"));

    public static final PacketCodec<RegistryByteBuf, FakeNamePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FakeNamePayload::fakename,
            PacketCodecs.INTEGER, FakeNamePayload::entityId,
            PacketCodecs.INTEGER, FakeNamePayload::operation,
            FakeNamePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
