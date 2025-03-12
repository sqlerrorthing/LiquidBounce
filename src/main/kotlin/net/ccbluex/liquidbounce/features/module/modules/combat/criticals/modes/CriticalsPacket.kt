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
import net.ccbluex.liquidbounce.utils.math.component1
import net.ccbluex.liquidbounce.utils.math.component2
import net.ccbluex.liquidbounce.utils.math.component3
import net.minecraft.block.CarpetBlock
import net.minecraft.block.SnowBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
import net.minecraft.stat.Stats
import net.minecraft.util.math.Vec3d

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

        if (mode.doCriticalHit()) {
            showCriticals(event.entity)
        }
    }

    @Suppress("MagicNumber")
    override fun shouldWaitForCriticalHit(target: Entity, ignoreState: Boolean) = when {
        mode == Mode.SNOW -> player.velocity.y > -0.08
        else -> false
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
        val doCriticalHit: () -> Boolean
    ) : NamedChoice {
        VANILLA("Vanilla", {
            sendPacket(0.2)
            sendPacket(0.01)
            true
        }),
        NO_CHEAT_PLUS("NoCheatPlus", {
            sendPacket(0.11)
            sendPacket(
                0.1100013579
            )
            sendPacket(0.0000013579)
            true
        }),
        FALLING("Falling", {
            sendPacket(0.0625)
            sendPacket(0.0625013579)
            sendPacket(0.0000013579)
            true
        }),
        LOW("Low", {
            sendPacket(1e-9)
            sendPacket(0.0)
            true
        }),
        DOWN("Down", {
            sendPacket(-1e-9)
            true
        }),
        GRIM("Grim", {
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
        BLOCKSMC("BlocksMC", {
            (player.age % 4 == 0).apply {
                if (this) {
                    sendPacket(0.0011, true)
                    sendPacket(0.0)
                }
            }
        }),
        SNOW("Snow", {
            val block = world.getBlockState(player.blockPos).block

            (player.isOnGround && (block is CarpetBlock || block is SnowBlock)).apply {
                if (this) {
                    network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY))
                    network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_SPRINTING))

                    val (x, _, z) = player.velocity
                    player.velocity = Vec3d(
                        x * 1.12,
                        0.20,
                        z * 1.12
                    )

                    network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY))
                    network.sendPacket(ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.STOP_SPRINTING))

                    player.incrementStat(Stats.JUMP)
                }
            }
        })
    }
}
