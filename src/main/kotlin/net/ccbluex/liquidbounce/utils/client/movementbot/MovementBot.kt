package net.ccbluex.liquidbounce.utils.client.movementbot

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.KeyboardKeyEvent
import net.ccbluex.liquidbounce.event.handler

object MovementBot : EventListener {

    var loadedRecords: MutableList<MovementRecord> = mutableListOf()
        private set

    @Suppress("unused")
    private val keyboardListener = handler<KeyboardKeyEvent> { _ ->
        if (RecordPlayer.currentPlayingRecord != null) {
            RecordPlayer.currentPlayingRecord = null
        }
    }

}
