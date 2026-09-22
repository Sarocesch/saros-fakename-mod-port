package tschipp.fakename.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.fakename.FakeNameData;

/**
 * Mixin to override player display name when a fakename is set.
 * This replaces Forge's PlayerEvent.NameFormat functionality.
 */
@Mixin(Player.class)
public class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Component> cir) {
        Player player = (Player) (Object) this;
        CompoundTag data = FakeNameData.getData(player);

        if (data.contains("fakename")) {
            String fakename = data.getStringOr("fakename", "");
            cir.setReturnValue(net.minecraft.network.chat.Component.literal(fakename));
        }
    }
}
