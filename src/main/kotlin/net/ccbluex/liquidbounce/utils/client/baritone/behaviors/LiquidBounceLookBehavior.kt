package net.ccbluex.liquidbounce.utils.client.baritone.behaviors

import baritone.api.behavior.ILookBehavior
import baritone.api.behavior.look.IAimProcessor
import baritone.api.behavior.look.ITickableAimProcessor
import baritone.api.event.events.PlayerUpdateEvent
import baritone.api.event.events.WorldEvent
import baritone.api.event.events.type.EventState
import net.ccbluex.liquidbounce.features.module.modules.client.ModuleBaritone
import net.ccbluex.liquidbounce.utils.aiming.*
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import baritone.api.utils.Rotation as BRotation


/**
 * @author 00101110001100010111000101111
 * @since 12/17/2024
 **/
class LiquidBounceLookBehavior : ILookBehavior {

    /**
     * The current look target, may be `null`.
     */
    private var target: Rotation? = null
    private val processor: CustomBaritoneAimProcessor = CustomBaritoneAimProcessor()

    override fun updateTarget(rotation: BRotation, blockInteract: Boolean) {
        target = rotation.toClientRotation()
    }

    override fun getAimProcessor(): IAimProcessor = processor

    override fun onWorldEvent(event: WorldEvent?) {
        target = null
    }

    override fun onPlayerUpdate(event: PlayerUpdateEvent) {
        if (target == null) {
            return
        }

        when (event.state) {
            EventState.PRE -> {
                processor.nextRotation(target!!.toBaritoneRotation())
            }
            EventState.POST -> {
                target = null
            }
            else -> {}
        }
    }
}

private class CustomBaritoneAimProcessor : ITickableAimProcessor {
    override fun peekRotation(desired: BRotation): BRotation {
        return RotationManager.currentRotation?.toBaritoneRotation() ?: desired
    }

    override fun fork(): ITickableAimProcessor {
        return CustomBaritoneAimProcessor()
    }

    @Suppress("EmptyFunctionBlock")
    override fun tick() {}

    @Suppress("EmptyFunctionBlock")
    override fun advance(ticks: Int) {}

    override fun nextRotation(rotation: BRotation): BRotation {
        RotationManager.aimAt(
            ModuleBaritone.Rotations.toAimPlan(rotation.toClientRotation()),
            Priority.IMPORTANT_FOR_USAGE_3,
            ModuleBaritone
        )

        return peekRotation(rotation)
    }

}
