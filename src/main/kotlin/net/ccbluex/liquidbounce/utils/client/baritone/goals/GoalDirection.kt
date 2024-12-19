package net.ccbluex.liquidbounce.utils.client.baritone.goals

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.Goal
import net.ccbluex.liquidbounce.utils.client.player
import java.lang.Math.toRadians
import kotlin.math.*

private val SQRT_2: Double = sqrt(2.0)

/**
 * A goal implementation that calculates destination based on player's yaw (look direction).
 *
 * This class determines a destination point 100 blocks away from the player's current position
 * in the direction they are looking (yaw angle).
 *
 * @property yaw The yaw angle of player's look direction in degrees
 *   * (-180 <= yaw <= 180)
 *
 * @constructor Creates a goal with specified yaw angle
 */
class GoalDirection(private val yaw: Float) : Goal {

    /**
     * Calculates the destination coordinates based on player's position and yaw.
     * Uses trigonometry to find a point 100 blocks away in the look direction.
     *
     * @return Pair of x,z coordinates for the destination
     */
    private val destination: Pair<Int, Int> get() {
        val theta = toRadians(yaw.toDouble()).toFloat()
        val pos = player.pos

        return floor(pos.x - sin(theta) * 100).toInt() to floor(pos.z + cos(theta) * 100).toInt()
    }

    /**
     * Calculates the heuristic cost between two points using a combination of
     * diagonal and straight line distances.
     *
     * @param xDiff The x-axis difference between points
     * @param zDiff The z-axis difference between points
     * @return The calculated movement cost multiplied by the cost heuristic setting
     */
    fun calculate(xDiff: Int, zDiff: Int): Double {
        val x = abs(xDiff)
        val z = abs(zDiff)

        val (straight, diagonal) = if (x < z) {
            (z - x) to x
        } else {
            (x - z) to z
        }

        return ((diagonal * SQRT_2) + straight) * BaritoneAPI.getSettings().costHeuristic.value
    }

    override fun isInGoal(x: Int, y: Int, z: Int): Boolean =
        x == this.destination.first && z == this.destination.second

    override fun heuristic(x: Int, y: Int, z: Int): Double =
        calculate(x - this.destination.first, z - this.destination.second)
}
