package net.ccbluex.liquidbounce.injection.mixins.baritone;

import baritone.api.pathing.goals.Goal;
import baritone.process.CustomGoalProcess;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.BaritoneCustomGoalProcessCreatedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomGoalProcess.class)
public class MixinCustomGoalProcess {

    @Inject(method = "setGoal", at = @At("HEAD"), cancellable = true, remap = false)
    public void hookSetGoal(Goal goal, CallbackInfo ci) {
        var event = new BaritoneCustomGoalProcessCreatedEvent(goal);
        EventManager.INSTANCE.callEvent(event);

        if (event.isCancelled()) ci.cancel();
    }

}
