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
 * #### Keep in mind!
 * You can use any method from this interface UNLESS it causes
 * the game to crash due to a missing baritone.
 * Be vigilant, and if you need a strict check for a Baritone, use [PathManager.isBaritone].
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
 * - This interface supports basic pathing operations such as moving to a specific position, pausing, resuming,
 *   and stopping an active path.
 */
interface PathManager {
    /**
     * Indicates whether the system is currently executing a pathfinding operation.
     *
     * By default, returns `false` as no pathing operation is active unless overridden by an implementation.
     *
     * @return `true` if there is an active pathing operation, `false` otherwise.
     */
    val isPathing: Boolean
        get() = false

    /**
     * Indicates whether the current implementation of the PathManager is [BaritonePathManager]
     *
     * This is useful to determine if advanced pathfinding features (provided by Baritone) are available.
     * By default, this is `false` unless overridden by an implementation that integrates with Baritone.
     *
     * @return `true` if Baritone is being used, `false` otherwise.
     */
    val isBaritone: Boolean
        get() = false

    /**
     * Initiates movement to a specified position in the world.
     *
     * The movement can optionally ignore the Y-coordinate (elevation) if `ignoreY` is set to `true`.
     * This is useful for scenarios where only horizontal movement is desired.
     *
     * @param pos The target position [BlockPos] to move to.
     * @param ignoreY A flag indicating whether to ignore the Y-coordinate during movement. Defaults to `false`.
     */
    fun moveTo(pos: BlockPos, ignoreY: Boolean = false) {}

    /**
     * Initiates movement in a specific direction based on the provided yaw angle.
     *
     * @param yaw The yaw angle (in degrees) representing the direction to move in. `-180 <= yaw <= 180`
     */
    fun moveInDirection(yaw: Float) {}

    /**
     * Pauses the currently active pathing operation, if any.
     *
     * This temporarily halts movement without completely stopping the path. Useful for scenarios where
     * movement needs to be suspended but may be resumed later.
     */
    fun pause() {}

    /**
     * Resumes a previously paused pathing operation, if any.
     *
     * This continues movement along the path that was paused. If there is no paused path, this method
     * has no effect.
     */
    fun resume() {}

    /**
     * Stops the currently active pathing operation and clears the path.
     *
     * This completely halts movement and discards any active or paused paths. This is useful when the
     * current pathing task is no longer needed.
     */
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
