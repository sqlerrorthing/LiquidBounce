package net.ccbluex.liquidbounce.injection.mixins.baritone;

import baritone.utils.PlayerMovementInput;
import net.ccbluex.liquidbounce.injection.mixins.minecraft.client.MixinInput;
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerMovementInput.class)
public class MixinPlayerMovementInput extends MixinInput {

    @Inject(method = "tick", at = @At("HEAD"))
    public void hookTick(boolean par1, float par2, CallbackInfo ci) {
        var options =  MinecraftClient.getInstance().options

        this.proceedKeyboardTick(new DirectionalInput(
                options.forwardKey.isPressed(),
                options.backKey.isPressed(),
                options.leftKey.isPressed(),
                options.rightKey.isPressed()
        ), options.jumpKey.isPressed(), options.sneakKey.isPressed(), false, () -> {});
    }

}
