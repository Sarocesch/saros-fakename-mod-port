package tschipp.fakename.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.SocialInteractionsPlayerList;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * Mixin to update the social interactions screen to show fake names.
 * Simplified for 26.1 - the social interactions API changed significantly.
 */
@Mixin(SocialInteractionsPlayerList.class)
public class SocialInteractionsPlayerListMixin {

    @Inject(method = "updatePlayerList", at = @At("TAIL"), require = 0)
    private void onUpdatePlayerList(Collection<UUID> uuids, double scrollAmount, CallbackInfo ci) {
        // In 26.1, the social interactions player list API changed.
        // This mixin is a no-op placeholder until the new API is understood.
        // Fakename display in tab list is handled by FakeNameClient instead.
    }
}
