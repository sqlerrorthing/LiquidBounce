package net.ccbluex.liquidbounce.injection.mixins.baritone;

import baritone.Baritone;
import baritone.api.IBaritone;
import baritone.api.behavior.ILookBehavior;
import baritone.api.event.listener.IEventBus;
import net.ccbluex.liquidbounce.utils.client.baritone.behaviors.LiquidBounceLookBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Baritone.class)
public abstract class MixinBaritone implements IBaritone {

    @Shadow
    public abstract IEventBus getGameEventHandler();

    @Unique
    private LiquidBounceLookBehavior lookBehavior;

    @Inject(method = "getLookBehavior", at = @At("HEAD"), cancellable = true, remap = false)
    public void hookLookBehavior(CallbackInfoReturnable<ILookBehavior> cir) {
        if (lookBehavior == null) {
            lookBehavior = new LiquidBounceLookBehavior();
            getGameEventHandler().registerEventListener(lookBehavior);
        }

        cir.setReturnValue(lookBehavior);
    }

}
