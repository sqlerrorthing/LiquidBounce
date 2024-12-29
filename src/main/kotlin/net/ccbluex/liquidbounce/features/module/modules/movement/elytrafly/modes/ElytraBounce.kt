package net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.modes

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.*
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.ElytraFlyHelper
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.startFallFlying
import net.ccbluex.liquidbounce.utils.aiming.Rotation
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket


/**
 * ElytraBounce
 *
 * @author sqlerrorthing
 * @since 12/29/2024
 **/
internal object ElytraBounce : Choice("Bounce"), ElytraFlyHelper {
    override val parent: ChoiceConfigurable<*>
        get() = ModuleElytraFly.modes

    private val pitch by float("Pitch", 85.0f, -90.0f..90.0f)
    private val autoJump by boolean("AutoJump", true)
    private val sprint by boolean("Sprint", true)

    @Suppress("unused")
    private val repeatable = tickHandler {
        if (!isInConditions) {
            return@tickHandler
        }

        if (sprint) {
            player.isSprinting = true
        }

        if (mc.options.jumpKey.isPressed && !player.isGliding) {
            startFallFlying()
        }

        if (autoJump) {
            mc.options.jumpKey.isPressed = true
        }
        mc.options.forwardKey.isPressed = true

        RotationManager.aimAt(
            RotationsConfigurable(this@ElytraBounce).toAimPlan(
                Rotation(
                    player.yaw,
                    pitch
                )
            ),
            Priority.IMPORTANT_FOR_USAGE_2,
            ModuleElytraFly
        )

        if (!sprint) {
            if (player.isGliding) {
                player.isSprinting = player.isOnGround
            } else {
                player.isSprinting = true
            }
        }
    }

    override fun disable() {
        mc.options.forwardKey.isPressed = false
        if (autoJump) {
            mc.options.jumpKey.isPressed = false
        }
    }

    @Suppress("unused")
    private val onPacket = handler<PacketEvent> { event ->
        if (event.origin != TransferOrigin.SEND) {
            return@handler
        }

        if (event.packet is ClientCommandC2SPacket && event.packet.mode == ClientCommandC2SPacket.Mode.START_FALL_FLYING && !sprint) {
            player.isSprinting = true
        }
    }

    fun recastElytra(): Boolean {
        if (isInConditions && ignoreGround) {
            player.startGliding()
            startFallFlying()
            return true
        }

        return false
    }

    private val ignoreGround: Boolean
        get() = !player.isTouchingWater && !player.hasStatusEffect(StatusEffects.LEVITATION)
}
