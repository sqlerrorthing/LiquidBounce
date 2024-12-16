package net.ccbluex.liquidbounce.utils.client.baritone

import baritone.api.BaritoneAPI
import baritone.api.process.IBaritoneProcess
import baritone.api.process.ICustomGoalProcess
import baritone.api.process.IMineProcess

object BaritoneUtil {
    val isAvailable = runCatching {
        Class.forName("baritone.api.BaritoneAPI")
        true
    }.getOrDefault(false)

    val baritone get() = BaritoneAPI.getProvider().primaryBaritone
        ?: error("Called baritone but it is not available, please make sure to check isBaritoneAvailable")

    val isMining get() = mostResentInControl is IMineProcess

    val isWalking get() = mostResentInControl is ICustomGoalProcess
}

private val mostResentInControl: IBaritoneProcess?
    get() = BaritoneUtil.baritone.pathingControlManager.mostRecentInControl().orElse(null)
