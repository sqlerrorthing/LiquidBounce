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
import net.ccbluex.liquidbounce.features.module.MinecraftShortcuts
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.modes.ElytraBounce
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.modes.ElytraStatic
import net.ccbluex.liquidbounce.features.module.modules.movement.elytrafly.modes.ElytraVanilla
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket


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
        ElytraVanilla,
        ElytraBounce
    ))
}

internal interface ElytraFlyHelper : MinecraftShortcuts

internal val ElytraFlyHelper.isInConditions: Boolean get() {
    if (player.hasVehicle()) {
        return false
    }

    val itemStack = player.getEquippedStack(EquipmentSlot.CHEST)

    if (itemStack.item != Items.ELYTRA) {
        return false
    }

    if (player.abilities.flying) {
        return false
    }

    if (player.isClimbing) {
        return false
    }

    if (itemStack.willBreakNextUse()) {
        return false
    }

    return itemStack.contains(DataComponentTypes.GLIDER)
}

internal fun ElytraFlyHelper.startFallFlying() {
    network.sendPacket(ClientCommandC2SPacket(
        player,
        ClientCommandC2SPacket.Mode.START_FALL_FLYING
    ))
}
