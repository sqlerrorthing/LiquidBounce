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
package net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.modes.ElytraStatic
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.modes.ElytraVanilla


/**
 * ElytraFly module
 *
 * Makes you fly faster on Elytra.
 *
 * @author sqlerrorthing, Razzy52
 */

object ModuleElytraFly : ClientModule("ElytraFly", Category.MOVEMENT) {
    val modes = choices("Mode", ElytraVanilla, arrayOf(
        ElytraStatic,
        ElytraVanilla
    ))
}
