package net.ccbluex.liquidbounce.utils.client.baritone

import baritone.api.pathing.goals.GoalGetToBlock
import baritone.api.pathing.goals.GoalXZ
import baritone.api.process.IBaritoneProcess
import baritone.api.process.PathingCommand
import baritone.api.process.PathingCommandType
import net.ccbluex.liquidbounce.utils.client.baritone.BaritoneUtil.baritone
import net.ccbluex.liquidbounce.utils.client.baritone.goals.GoalDirection
import net.minecraft.util.math.BlockPos

/**
 * PathManager for baritone use safety.
 *
 * If [net.ccbluex.liquidbounce.utils.client.baritone.BaritoneUtil.isAvailable] is true,
 * then the PathManager in the singleton will be [BaritonePathManager], otherwise [UnknownPathManager] as a placeholder.
 *
 * #### Usage tips:
 * ```
 * if (!PathManager) { ... }
 * ```
 * and
 * ```
 * if (!PathManager.isBaritone) { ... }
 * ```
 * are exactly the same.
 */
interface PathManager {
    val isPathing: Boolean
        get() = false

    val isBaritone: Boolean
        get() = false

    fun moveTo(pos: BlockPos, ignoreY: Boolean = false) {}
    fun moveInDirection(yaw: Float) {}

    fun pause() {}
    fun resume() {}
    fun stop() {}

    companion object : PathManager {
        private val Default by lazy {
            if (BaritoneUtil.isAvailable) {
                BaritonePathManager
            } else {
                UnknownPathManager
            }
        }

        override val isPathing get() = Default.isPathing

        override val isBaritone get() = Default.isBaritone

        override fun moveTo(pos: BlockPos, ignoreY: Boolean) = Default.moveTo(pos, ignoreY)

        override fun moveInDirection(yaw: Float) = Default.moveInDirection(yaw)

        override fun pause() = Default.pause()

        override fun resume() = Default.resume()

        override fun stop() = Default.stop()

        operator fun not(): Boolean = !this.isBaritone
    }
}

object UnknownPathManager : PathManager

object BaritonePathManager : PathManager {
    init {
        if (BaritoneUtil.isAvailable) {
            baritone.pathingControlManager.registerProcess(BaritoneProcess)
        }
    }

    private var pathingPaused = false

    override val isBaritone = true

    override val isPathing get() = baritone.pathingBehavior.isPathing

    override fun moveTo(pos: BlockPos, ignoreY: Boolean) = if(ignoreY) {
        baritone.customGoalProcess.setGoalAndPath(GoalXZ(pos.x, pos.z))
    } else {
        baritone.customGoalProcess.setGoalAndPath(GoalGetToBlock(pos))
    }

    override fun moveInDirection(yaw: Float) {
        baritone.customGoalProcess.setGoalAndPath(GoalDirection(yaw))
    }

    override fun pause() {
        pathingPaused = true
    }

    override fun resume() {
        pathingPaused = false
    }

    override fun stop() {
        baritone.pathingBehavior.cancelEverything()
    }

    private object BaritoneProcess : IBaritoneProcess {
        override fun isActive() = pathingPaused

        override fun onTick(calcFailed: Boolean, isSafeToCancel: Boolean): PathingCommand {
            baritone.inputOverrideHandler.clearAllKeys()
            return PathingCommand(null, PathingCommandType.REQUEST_PAUSE)
        }

        override fun isTemporary() = true

        @Suppress("EmptyFunctionBlock")
        override fun onLostControl() {}

        override fun displayName0() = "LiquidBounce"
    }
}
