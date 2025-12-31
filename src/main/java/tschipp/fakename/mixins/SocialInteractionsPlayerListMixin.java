package tschipp.fakename.mixins;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListWidget;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

/**
 * Mixin to update the social interactions screen to show fake names.
 */
@Mixin(SocialInteractionsPlayerListWidget.class)
public class SocialInteractionsPlayerListMixin {

	@Shadow
	private List<SocialInteractionsPlayerListEntry> players;

	@Shadow
	private SocialInteractionsScreen parent;

	@Inject(method = "setPlayers", at = @At("TAIL"))
	private void onUpdatePlayerList(Collection<UUID> uuids, double scrollAmount, CallbackInfo ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.player.networkHandler == null)
			return;

		for (int i = 0; i < players.size(); i++) {
			SocialInteractionsPlayerListEntry entry = players.get(i);

			// Try to find if this entry has a fakename by checking the tab list
			for (UUID uuid : uuids) {
				PlayerListEntry playerInfo = client.player.networkHandler.getPlayerListEntry(uuid);
				if (playerInfo != null) {
					Text displayName = playerInfo.getDisplayName();
					if (displayName != null) {
						String displayString = displayName.getString();
						// Check if entry name matches the profile name but display is different
						String profileName = playerInfo.getProfile().name();
						if (entry.getName().equals(profileName) && !profileName.equals(displayString)) {
							// Replace entry with one showing the fake name
							// In 1.21.10, the constructor takes Supplier<SkinTextures>
							players.set(i, new SocialInteractionsPlayerListEntry(
									client,
									this.parent,
									uuid,
									displayString,
									playerInfo::getSkinTextures,
									true));
							break;
						}
					}
				}
			}
		}
	}
}
