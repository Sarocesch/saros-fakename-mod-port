package tschipp.fakename.mixins;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.fakename.FakeNameData;

/**
 * Mixin to override player display name when a fakename is set.
 * This replaces Forge's PlayerEvent.NameFormat functionality.
 */
@Mixin(PlayerEntity.class)
public class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        NbtCompound data = FakeNameData.getData(player);

        if (data.contains("fakename")) {
            String fakename = data.getString("fakename");
            cir.setReturnValue(Text.literal(fakename));
        }
    }
}
