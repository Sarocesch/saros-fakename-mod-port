package tschipp.fakename;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                FakeNamePayload.TYPE,
                FakeNamePayload.STREAM_CODEC,
                (payload, context) -> tschipp.fakename.client.ClientPayloadHandler.handle(payload, context));
    }
}
