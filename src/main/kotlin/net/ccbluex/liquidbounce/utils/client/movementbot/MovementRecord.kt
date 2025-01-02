package net.ccbluex.liquidbounce.utils.client.movementbot

import net.ccbluex.liquidbounce.utils.aiming.Rotation
import net.ccbluex.liquidbounce.utils.client.player
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d


data class MovementRecord(
    var name: String,
    var startPosition: PositionData,
    var ticks: MutableList<MovementTick>
)

data class MovementTick(
    var tick: Long,
    var position: PositionData
)

data class PositionData(
    var crouching: Boolean,
    var sprinting: Boolean,
    var motion: Vec3d,
    var rotation: Vec2f
)

inline fun ClientPlayerEntity.replay(
    data: PositionData,
    rotationSetter: (Rotation, ClientPlayerEntity) -> Unit = { rotation, player ->
        player.yaw = rotation.yaw
        player.pitch = rotation.pitch
    }
) {
    this.isSneaking = data.crouching
    this.isSprinting = data.sprinting
    this.velocity = data.motion
    with(data.rotation) {
        rotationSetter(Rotation(x, y), player)
    }
}
