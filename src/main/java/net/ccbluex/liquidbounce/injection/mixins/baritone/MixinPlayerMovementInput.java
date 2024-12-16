package net.ccbluex.liquidbounce.injection.mixins.baritone;

import baritone.utils.PlayerMovementInput;
import net.ccbluex.liquidbounce.injection.mixins.minecraft.client.MixinInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerMovementInput.class)
public class MixinPlayerMovementInput extends MixinInput {

    @Inject(method = "tick", at = @At("HEAD"))
    public void hookTick(boolean par1, float par2, CallbackInfo ci) {
        // TODO: Fill the DirectionalInput
//        this.proceedKeyboardTick(new DirectionalInput(), () -> {});
    }

}
