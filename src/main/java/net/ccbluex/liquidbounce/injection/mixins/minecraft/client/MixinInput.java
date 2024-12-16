/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */

package net.ccbluex.liquidbounce.injection.mixins.minecraft.client;

import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.MovementInputEvent;
import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleSuperKnockback;
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ModuleCriticals;
import net.ccbluex.liquidbounce.features.module.modules.movement.ModuleSprint;
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Input.class)
public class MixinInput {
    @Shadow
    public boolean pressingRight;
    @Shadow
    public boolean pressingLeft;
    @Shadow
    public boolean pressingBack;
    @Shadow
    public boolean pressingForward;
    @Shadow
    public float movementForward;
    @Shadow
    public float movementSideways;
    @Shadow
    public boolean jumping;

    @Shadow public boolean sneaking;

    @Inject(method = "hasForwardMovement", cancellable = true, at = @At("RETURN"))
    private void hookOmnidirectionalSprintA(final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (ModuleCriticals.WhenSprinting.INSTANCE.getRunning() && ModuleCriticals.WhenSprinting.INSTANCE.getStopSprinting() == ModuleCriticals.WhenSprinting.StopSprintingMode.LEGIT) {
            callbackInfoReturnable.setReturnValue(false);
            return;
        }

        final boolean hasMovement = Math.abs(movementForward) > 1.0E-5F || Math.abs(movementSideways) > 1.0E-5F;

        callbackInfoReturnable.setReturnValue(!ModuleSprint.INSTANCE.shouldPreventSprint() && (ModuleSprint.INSTANCE.shouldSprintOmnidirectionally() ? hasMovement : callbackInfoReturnable.getReturnValue()));
    }

    @Unique
    public void proceedKeyboardTick(DirectionalInput baseDirectionalInput, boolean jumping, boolean sneaking, Runnable additive) {
        var event = new MovementInputEvent(baseDirectionalInput, jumping, sneaking);

        EventManager.INSTANCE.callEvent(event);

        var directionalInput = event.getDirectionalInput();

        this.pressingForward = directionalInput.getForwards();
        this.pressingBack = directionalInput.getBackwards();
        this.pressingLeft = directionalInput.getLeft();
        this.pressingRight = directionalInput.getRight();
        this.movementForward = KeyboardInput.getMovementMultiplier(directionalInput.getForwards(), directionalInput.getBackwards());
        this.movementSideways = KeyboardInput.getMovementMultiplier(directionalInput.getLeft(), directionalInput.getRight());

        additive.run();

        if (ModuleSuperKnockback.INSTANCE.shouldStopMoving()) {
            this.movementForward = 0f;

            ModuleSprint sprint = ModuleSprint.INSTANCE;

            if (sprint.shouldSprintOmnidirectionally()) {
                this.movementSideways = 0f;
            }
        }

        this.jumping = event.getJumping();
        this.sneaking = event.getSneaking();
    }
}
