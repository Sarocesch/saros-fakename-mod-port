package tschipp.fakename;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

/**
 * CustomPayload record for syncing fakename data between server and clients.
 * Uses the new Fabric 1.21+ networking API pattern.
 *
 * Sends player UUID instead of entity ID so the client can update FakeNameData
 * and the tab list even when the target player is not loaded in the world.
 */
public record FakeNamePayload(String fakename, UUID uuid, int operation) implements CustomPayload {

    public static final CustomPayload.Id<FakeNamePayload> ID = new CustomPayload.Id<>(
            Identifier.of(FakeName.MODID, "fakename_sync"));

    public static final PacketCodec<RegistryByteBuf, FakeNamePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, FakeNamePayload::fakename,
            Uuids.PACKET_CODEC, FakeNamePayload::uuid,
            PacketCodecs.INTEGER, FakeNamePayload::operation,
            FakeNamePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
