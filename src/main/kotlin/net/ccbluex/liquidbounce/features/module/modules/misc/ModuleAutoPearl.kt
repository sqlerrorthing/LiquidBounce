package net.ccbluex.liquidbounce.features.module.modules.misc

import com.oracle.truffle.runtime.collection.ArrayQueue
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.HotbarItemSlot
import net.ccbluex.liquidbounce.features.module.modules.render.trajectories.TrajectoryInfo
import net.ccbluex.liquidbounce.features.module.modules.render.trajectories.TrajectoryInfoRenderer
import net.ccbluex.liquidbounce.utils.aiming.Rotation
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.client.toRadians
import net.ccbluex.liquidbounce.utils.combat.CombatManager
import net.ccbluex.liquidbounce.utils.entity.interpolateCurrentPosition
import net.ccbluex.liquidbounce.utils.inventory.OFFHAND_SLOT
import net.ccbluex.liquidbounce.utils.inventory.useHotbarSlotOrOffhand
import net.ccbluex.liquidbounce.utils.item.findHotbarItemSlot
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.math.minus
import net.ccbluex.liquidbounce.utils.math.plus
import net.ccbluex.liquidbounce.utils.math.times
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.projectile.thrown.EnderPearlEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.*

private const val MAX_SIMULATED_TICKS = 240

/**
 * Auto pearl module
 *
 * AutoPearl aims and throws a pearl at an enemies pearl trajectory
 *
 * @author sqlerrorthing
 */
object ModuleAutoPearl : ClientModule("AutoPearl", Category.MISC, aliases = arrayOf("PearlFollower")) {

    private object Limits : Configurable("Limits") {
        val angle by int("Angle", 180, 0..180, suffix = "°")
        val activationDistance by float("MinDistance", 8.0f, 0.0f..10.0f, suffix = "m")
        val destDistance by float("DestinationDistance", 8.0f, 0.0f..30.0f, suffix = "m")
    }

    private object Rotate : ToggleableConfigurable(this, "Rotate", true) {
        val rotations = tree(RotationsConfigurable(this))
    }

    private val combatPauseTime by int("CombatPauseTime", 0, 0..40, "ticks")
    private val slotResetDelay by intRange("SlotResetDelay", 0..0, 0..40, "ticks")

    private val queue = ArrayQueue<Rotation>()

    init {
        tree(Limits)
        tree(Rotate)
    }

    override fun disable() {
        queue.clear()
        super.disable()
    }

    @Suppress("unused")
    val pearlSpawnHandler = handler<PacketEvent> { event ->
        if (event.packet !is EntitySpawnS2CPacket) {
            return@handler
        }

        if (event.packet.entityType != EntityType.ENDER_PEARL) {
            return@handler
        }

        if (enderPearlSlot == null) {
            return@handler
        }

        val data = event.packet
        val entity = data.entityType.create(world) as EnderPearlEntity
        entity.onSpawnPacket(data)

        proceedPearl(
            pearl = entity,
            // entity.velocity & entity.pos doesnt work, dont use it
            velocity = with(data) { Vec3d(velocityX, velocityY, velocityZ) },
            pearlPos = with(data) { Vec3d(x, y, z) }
        )
    }

    @Suppress("unused")
    private val repeatable = tickHandler {
        val rotation = queue.poll() ?: return@tickHandler
        val itemSlot = enderPearlSlot ?: return@tickHandler

        CombatManager.pauseCombatForAtLeast(combatPauseTime)
        if (Rotate.enabled) {
            waitUntil {
                RotationManager.aimAt(
                    Rotate.rotations.toAimPlan(rotation),
                    Priority.IMPORTANT_FOR_USAGE_3,
                    this@ModuleAutoPearl
                )

                val serverRotations = transformAngles(RotationManager.serverRotation)

                val yawDiff = abs(serverRotations.yaw - rotation.yaw)
                val pitchDiff = abs(serverRotations.pitch - rotation.pitch)

                yawDiff <= 0.2 && pitchDiff <= 0.2
            }
        }

        val (yaw, pitch) = if (Rotate.enabled) {
            RotationManager.serverRotation.yaw to RotationManager.serverRotation.pitch
        } else {
            rotation.yaw to rotation.pitch
        }

        useHotbarSlotOrOffhand(itemSlot, slotResetDelay.random(), yaw, pitch)
    }

    private fun proceedPearl(
        pearl: EnderPearlEntity,
        velocity: Vec3d,
        pearlPos: Vec3d
    ) {
        if (Limits.angle < RotationManager.rotationDifference(pearl)) {
            return
        }

        if (pearl.ownerUuid == player.uuid) {
            return
        }

        val dest = calculatePearlTeleportDestPos(
            owner = pearl.owner ?: player,
            velocity = velocity,
            pos = pearlPos
        ) ?: return

        if (Limits.activationDistance > dest.pos.distanceTo(player.pos)) {
            return
        }

        val rotation = calculatePearlTrajectory(player.eyePos, dest.pos) ?: return

        if (!canThrow(rotation, dest.pos)) {
            return
        }

        if (queue.size() == 0) {
            queue.add(rotation)
        }
    }

    private val enderPearlSlot: HotbarItemSlot? get() {
        if (OFFHAND_SLOT.itemStack.item == Items.ENDER_PEARL) {
            return OFFHAND_SLOT
        }

        return findHotbarItemSlot(Items.ENDER_PEARL)
    }

    private fun canThrow(
        angles: Rotation,
        destination: Vec3d
    ): Boolean {
        val info = TrajectoryInfo.GENERIC
        val yawRadians = angles.yaw / 180f * Math.PI.toFloat()
        val pitchRadians = angles.pitch / 180f * Math.PI.toFloat()

        val interpolatedOffset = player.interpolateCurrentPosition(mc.renderTickCounter.getTickDelta(true)) - player.pos

        var velocity = Vec3d(
            -sin(yawRadians) * cos(pitchRadians).toDouble(),
            -sin((angles.pitch + info.roll).toRadians()).toDouble(),
            cos(yawRadians) * cos(pitchRadians).toDouble()
        ).normalize() * info.initialVelocity

        velocity += Vec3d(
            player.velocity.x,
            if (player.isOnGround) 0.0 else player.velocity.y,
            player.velocity.z
        )

        val dest = TrajectoryInfoRenderer(
            owner = player,
            velocity = velocity,
            pos = with(player) { Vec3d(x, eyeY - 0.10000000149011612, z) },
            trajectoryInfo = info,
            renderOffset = interpolatedOffset + Vec3d(-cos(yawRadians) * 0.16, 0.0, -sin(yawRadians) * 0.16)
        ).runSimulation(MAX_SIMULATED_TICKS)?.pos ?: return false

        return Limits.destDistance > destination.distanceTo(dest)
    }

    private fun transformAngles(rotation: Rotation): Rotation {
        var transformedYaw = rotation.yaw
        var transformedPitch = rotation.pitch

        transformedYaw = (transformedYaw % 360)
        if (transformedYaw > 180) {
            transformedYaw -= 360
        } else if (transformedYaw < -180) {
            transformedYaw += 360
        }

        transformedPitch = (transformedPitch % 360)
        if (transformedPitch > 180) {
            transformedPitch -= 360
        } else if (transformedPitch < -180) {
            transformedPitch += 360
        }

        if (transformedPitch > 90) {
            transformedPitch = 180 - transformedPitch
            transformedYaw += 180
            transformedYaw = (transformedYaw % 360)
            if (transformedYaw > 180) {
                transformedYaw -= 360
            } else if (transformedYaw < -180) {
                transformedYaw += 360
            }

        } else if (transformedPitch < -90) {
            transformedPitch = -180 - transformedPitch
            transformedYaw += 180
            transformedYaw = (transformedYaw % 360)
            if (transformedYaw > 180) {
                transformedYaw -= 360
            } else if (transformedYaw < -180) {
                transformedYaw += 360
            }
        }

        return Rotation(transformedYaw, transformedPitch)
    }

    private fun calculatePearlTrajectory(startPos: Vec3d, targetPos: Vec3d): Rotation? {
        val diff: Vec3d = targetPos.subtract(startPos)

        val horizontalDistance = MathHelper.sqrt((diff.x * diff.x + diff.z * diff.z).toFloat()).toDouble()

        val velocity = 1.5
        val gravity = 0.03

        val velocity2 = velocity * velocity
        val velocity4 = velocity2 * velocity2
        val y = diff.y

        val sqrt = velocity4 - gravity * (gravity * horizontalDistance * horizontalDistance + 2 * y * velocity2)

        if (sqrt < 0) {
            return null
        }

        val pitchRad = atan((velocity2 - sqrt(sqrt)) / (gravity * horizontalDistance))

        val yawRad = atan2(diff.z, diff.x)

        val pitch = Math.toDegrees(pitchRad).toFloat()
        var yaw = Math.toDegrees(yawRad).toFloat()


        yaw -= 90f
        if (yaw > 180.0f) {
            yaw -= 360.0f
        } else if (yaw < -180.0f) {
            yaw += 360.0f
        }

        return Rotation(yaw, -pitch)
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
