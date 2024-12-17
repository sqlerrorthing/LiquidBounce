package net.ccbluex.liquidbounce.injection.mixins.baritone;

import baritone.behavior.PathingBehavior;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.BaritonePathCancelEverythingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author 00101110001100010111000101111
 * @since 12/17/2024
 **/
@Mixin(PathingBehavior.class)
public class MixinPathingBehavior {

    @Inject(method = "cancelEverything", at = @At("TAIL"), remap = false)
    public void hookCancelEverything(CallbackInfoReturnable<Boolean> cir) {
        EventManager.INSTANCE.callEvent(new BaritonePathCancelEverythingEvent());
    }

}
