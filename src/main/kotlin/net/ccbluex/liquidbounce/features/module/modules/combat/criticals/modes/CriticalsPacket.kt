/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
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
package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.modes

import net.ccbluex.liquidbounce.config.types.NamedChoice
import net.ccbluex.liquidbounce.event.events.AttackEntityEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.CriticalsMode
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ModuleCriticals
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ModuleCriticals.VisualsConfigurable.showCriticals
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ModuleCriticals.canDoCriticalHit
import net.ccbluex.liquidbounce.utils.client.MovePacketType
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity

/**
 * Packet criticals mode
 */
object CriticalsPacket : CriticalsMode("Packet") {

    private val mode by enumChoice("Mode", Mode.NO_CHEAT_PLUS)
    private val packetType by enumChoice("PacketType", MovePacketType.FULL)

    @Suppress("unused")
    private val attackHandler = handler<AttackEntityEvent> { event ->
        if (event.isCancelled || event.entity !is LivingEntity) {
            return@handler
        }

        val ignoreSprinting = ModuleCriticals.WhenSprinting.shouldAttemptCritWhileSprinting()

        if (!canDoCriticalHit(true, ignoreSprinting)) {
            return@handler
        }

        if (mode.doCriticalHit(event.entity)) {
            showCriticals(event.entity)
        }
    }

    private fun sendPacket(mod: Double, onGround: Boolean = false) {
        network.sendPacket(packetType.generatePacket().apply {
            this.y += mod
            this.onGround = onGround
        })
    }

    @Suppress("unused", "MagicNumber")
    enum class Mode(
        override val choiceName: String,
        val doCriticalHit: (target: Entity) -> Boolean
    ) : NamedChoice {
        VANILLA("Vanilla", { _ ->
            sendPacket(0.2)
            sendPacket(0.01)
            true
        }),
        NO_CHEAT_PLUS("NoCheatPlus", { _ ->
            sendPacket(0.11)
            sendPacket(
                0.1100013579
            )
            sendPacket(0.0000013579)
            true
        }),
        FALLING("Falling", { _ ->
            sendPacket(0.0625)
            sendPacket(0.0625013579)
            sendPacket(0.0000013579)
            true
        }),
        LOW("Low", { _ ->
            sendPacket(1e-9)
            sendPacket(0.0)
            true
        }),
        DOWN("Down", { _ ->
            sendPacket(-1e-9)
            true
        }),
        GRIM("Grim", { _ ->
            player.isOnGround.apply {
                if (this) {
                    // If player is in air, go down a little bit.
                    // Vanilla still crits and movement is too small
                    // for simulation checks.

                    // Requires packet type to be .FULL
                    sendPacket(-0.000001)
                }
            }
        }),
        BLOCKSMC("BlocksMC", { _ ->
            (player.age % 4 == 0).apply {
                if (this) {
                    sendPacket(0.0011, true)
                    sendPacket(0.0)
                }
            }
        }),
    }
}
