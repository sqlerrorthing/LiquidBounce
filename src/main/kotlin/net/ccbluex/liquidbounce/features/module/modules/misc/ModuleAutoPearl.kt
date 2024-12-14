package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.render.trajectories.TrajectoryInfo
import net.ccbluex.liquidbounce.features.module.modules.render.trajectories.TrajectoryInfoRenderer
import net.ccbluex.liquidbounce.utils.aiming.Rotation
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.client.SilentHotbar
import net.ccbluex.liquidbounce.utils.inventory.useHotbarSlotOrOffhand
import net.ccbluex.liquidbounce.utils.item.findHotbarItemSlot
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

private const val MAX_SIMULATED_TICKS = 240

/**
 * Auto pearl module
 *
 * AutoPearl aims and throws a pearl at an enemies pearl trajectory
 *
 * @author sqlerrorthing
 */
object ModuleAutoPearl : ClientModule("AutoPearl", Category.MISC, state = true, aliases = arrayOf("PearlFollower")) {

    private val angleLimit by int("AngleLimit", 180, 0..180, suffix = "°")
    private val distanceLimit by float("DistanceLimit", 8.0f, 0.0f..10.0f, suffix = "m")

    @Suppress("unused")
    val pearlSpawnHandler = handler<PacketEvent> { event ->
        if (event.packet !is EntitySpawnS2CPacket) {
            return@handler
        }

        if (event.packet.entityType != EntityType.ENDER_PEARL) {
            return@handler
        }

        if (findHotbarItemSlot(Items.ENDER_PEARL) == null) {
            return@handler
        }

        val data = event.packet
        val entity = world.getEntityById(event.packet.entityData)

        if (entity?.id == player.id) {
            return@handler
        }

        val dest = calculatePearlTeleportDestPos(
            owner = entity ?: player,
            velocity = with(data) { Vec3d(velocityX, velocityY, velocityZ) },
            pos = with(data) { Vec3d(x, y, z) }
        ) ?: return@handler

        if (distanceLimit > dest.pos.distanceTo(player.pos)) {
            return@handler
        }

        val rotation = calculatePearlTrajectory(player.eyePos, dest.pos) ?: return@handler
        aimAndThrowPearl(rotation)
    }

    private fun aimAndThrowPearl(rotation: Rotation) {
        val itemSlot = findHotbarItemSlot(Items.ENDER_PEARL)
        val slot = itemSlot?.hotbarSlotForServer ?: return

        RotationManager.aimAt(RotationsConfigurable(this).toAimPlan(rotation),
            Priority.IMPORTANT_FOR_USAGE_3, this)

        SilentHotbar.selectSlotSilently(this, slot, 0)
        useHotbarSlotOrOffhand(itemSlot, 0, rotation.yaw, rotation.pitch)
    }

    private fun calculatePearlTrajectory(startPos: Vec3d, targetPos: Vec3d): Rotation? {
        val diff: Vec3d = targetPos.subtract(startPos)

        val horizontalDistance = MathHelper.sqrt((diff.x * diff.x + diff.z * diff.z).toFloat()).toDouble()

        val initialVelocity = 1.5

        val gravity = 0.03
        val v2 = initialVelocity * initialVelocity
        val v4 = v2 * v2
        val y = diff.y
        val x = horizontalDistance
        val g = gravity

        val sqrt = v4 - g * (g * x * x + 2 * y * v2)

        if (sqrt < 0) {
            return null
        }

        val pitchRad = atan((v2 - sqrt(sqrt)) / (g * x))

        val yawRad = atan2(diff.z, diff.x)

        val pitch = Math.toDegrees(pitchRad).toFloat()
        var yaw = Math.toDegrees(yawRad).toFloat()


        yaw -= 90f
        if (yaw > 180.0f) {
            yaw -= 360.0f
        } else if (yaw < -180.0f) {
            yaw += 360.0f
        }

        return Rotation(yaw, pitch)
    }

    private fun calculatePearlTeleportDestPos(
        owner: Entity,
        velocity: Vec3d,
        pos: Vec3d
    ): HitResult? =
        TrajectoryInfoRenderer(
            owner = owner,
            velocity = velocity,
            pos = pos,
            trajectoryInfo = TrajectoryInfo.GENERIC,
            renderOffset = Vec3d.ZERO
        ).runSimulation(MAX_SIMULATED_TICKS)
}
