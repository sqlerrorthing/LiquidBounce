package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.render.trajectories.TrajectoryInfo
import net.ccbluex.liquidbounce.features.module.modules.render.trajectories.TrajectoryInfoRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d

private const val MAX_SIMULATED_TICKS = 240

/**
 * Auto pearl module
 *
 * AutoPearl aims and throws a pearl at an enemies pearl trajectory
 *
 * @author sqlerrorthing
 */
object ModuleAutoPearl : ClientModule("AutoPearl", Category.MISC, aliases = arrayOf("PearlFollower")) {

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

        val data = event.packet
        val velocity = Vec3d(data.velocityX, data.velocityY, data.velocityZ)
        val pos = Vec3d(data.x, data.y, data.z)

        val dest = calculatePearlTeleportDestPos(
            owner = mc.player as Entity,
            velocity = velocity,
            pos = pos
        ) ?: return@handler

        if (distanceLimit > dest.pos.distanceTo(mc.player!!.pos)) {
            return@handler
        }

        // TODO: Calc the throw pearl angles & throw the pearl
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
