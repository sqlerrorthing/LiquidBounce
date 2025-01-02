package net.ccbluex.liquidbounce.utils.client.movementbot

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.utils.client.player


/**
 * RecordPlayer
 *
 * @author sqlerrorthing
 * @since 1/2/2025
 **/
object RecordPlayer : EventListener {

    private var currentTick = 0
    var currentPlayingRecord: MovementRecord? = null
        set(value) {
            field = value
            currentTick = 0
        }

    private val closetData: PositionData? get() {
        val ticks = currentPlayingRecord?.ticks ?: return null

        if (ticks.isEmpty() || currentTick > ticks.maxOf { it.tick }) {
            currentPlayingRecord = null
            return null
        }

        return ticks.filter { it.tick <= currentTick }
            .maxByOrNull { it.tick }?.position
    }


    @Suppress("unused")
    private val tickHandler = tickHandler {
        val closetPositionData = closetData ?: return@tickHandler
        player.replay(closetPositionData)
        currentTick++
    }

    override val running = super.running && currentPlayingRecord != null

}
