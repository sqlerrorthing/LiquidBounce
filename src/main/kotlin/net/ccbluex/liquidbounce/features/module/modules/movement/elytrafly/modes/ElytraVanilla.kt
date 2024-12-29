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
package net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.modes

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.ElytraFlyHelper
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.ModuleElytraFly
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.isInConditions
import net.ccbluex.liquidbounce.utils.entity.moving
import net.ccbluex.liquidbounce.utils.entity.set
import net.ccbluex.liquidbounce.utils.entity.strafe

internal object ElytraVanilla : Choice("Vanilla"), ElytraFlyHelper {

    private val instant by boolean("Instant", true)
    private val instantStop by boolean("InstantStop", false)
    private object Speed : ToggleableConfigurable(this, "Speed", true) {
        val vertical by float("Vertical", 0.5f, 0.1f..2f)
        val horizontal by float("Horizontal", 1f, 0.1f..2f)
    }

    override val parent: ChoiceConfigurable<Choice>
        get() = ModuleElytraFly.modes as ChoiceConfigurable<Choice>


    val repeatable = tickHandler {
        if (!isInConditions) {
            return@tickHandler
        }

        if (mc.options.sneakKey.isPressed && instantStop) {
            player.stopGliding()
            return@tickHandler
        }

        // If player is flying
        if (player.isGliding) {
            if (Speed.enabled) {
                if (player.moving) {
                    player.strafe(speed = Speed.horizontal.toDouble())
                }
                player.velocity.y = when {
                    mc.options.jumpKey.isPressed -> Speed.vertical.toDouble()
                    mc.options.sneakKey.isPressed -> -Speed.vertical.toDouble()
                    else -> return@tickHandler
                }
            }
            // If the player has an elytra and wants to fly instead
        } else if (player.input.playerInput.jump) {
            if (instant) {
                // Jump must be off due to abnormal speed boosts
                player.input.set(
                    jump = false
                )
            }
        }
    }


}
