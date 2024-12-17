/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.event.events.BaritonePathCancelEverythingEvent
import net.ccbluex.liquidbounce.event.events.MovementInputEvent
import net.ccbluex.liquidbounce.event.events.NotificationEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.client.baritone.PathManager
import net.ccbluex.liquidbounce.utils.client.notification
import net.ccbluex.liquidbounce.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY

/**
 * AutoWalk module
 *
 * Automatically makes you walk.
 */
object ModuleAutoWalk : ClientModule("AutoWalk", Category.PLAYER) {

    private val modes = choices(this, "Mode", Smart, arrayOf(Smart, Simple)).apply { tagBy(this) }

    private object Smart : Choice("Smart") {
        override val parent: ChoiceConfigurable<*>
            get() = modes

        override fun enable() {
            if(!PathManager) {
                notification(
                    this.name,
                    "Baritone is not installed! Install it first.",
                    NotificationEvent.Severity.ERROR
                )

                ModuleAutoWalk.enabled = false
                return
            }

            PathManager.moveInDirection(player.yaw)
        }

        @Suppress("unused")
        private val cancelHandler = handler<BaritonePathCancelEverythingEvent> { _ ->
            ModuleAutoWalk.enabled = false
        }

        override fun disable() {
            if(PathManager.hasPath) {
                PathManager.stop()
            }
        }
    }

    private object Simple : Choice("Simple") {
        override val parent: ChoiceConfigurable<*>
            get() = modes

        @Suppress("unused")
        private val moveInputHandler = handler<MovementInputEvent>(priority = FIRST_PRIORITY) { event ->
            event.directionalInput = event.directionalInput.copy(forwards = true)
        }
    }
}
