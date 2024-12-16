package net.ccbluex.liquidbounce.utils.client.baritone.goals

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.Goal
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.client.player
import net.ccbluex.liquidbounce.utils.client.world
import net.minecraft.world.tick.TickManager
import java.lang.Math.toRadians
import kotlin.math.*

private val SQRT_2: Double = sqrt(2.0)

class GoalDirection (
    private val yaw: Float
) : Goal {

    private val destination: Pair<Int, Int> get() {
        val theta = toRadians(yaw.toDouble()).toFloat()
        val pos = player.pos

        return floor(pos.x - sin(theta) * 100).toInt() to floor(pos.z + cos(theta) * 100).toInt()
    }

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
