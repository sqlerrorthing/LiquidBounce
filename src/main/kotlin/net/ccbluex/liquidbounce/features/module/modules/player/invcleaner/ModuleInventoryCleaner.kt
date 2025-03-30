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
package net.ccbluex.liquidbounce.features.module.modules.player.invcleaner

import net.ccbluex.liquidbounce.event.events.ScheduleInventoryActionEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.inventoryPresets.InventoryPreset
import net.ccbluex.liquidbounce.features.inventoryPresets.items.NonePresetItem
import net.ccbluex.liquidbounce.features.inventoryPresets.items.PresetItem
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.items.ItemFacet
import net.ccbluex.liquidbounce.features.module.modules.player.offhand.ModuleOffhand
import net.ccbluex.liquidbounce.utils.inventory.*
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.kotlin.component1
import net.ccbluex.liquidbounce.utils.kotlin.component2

/**
 * InventoryCleaner module
 *
 * Automatically throws away useless items and sorts them.
 */
object ModuleInventoryCleaner : ClientModule("InventoryCleaner", Category.PLAYER,
    aliases = arrayOf("InventoryManager")
) {

    private val inventoryConstraints = tree(PlayerInventoryConstraints())

    @Suppress("unused")
    private val inventoryPresets = inventoryPresets()

    private val affectedSlots = Slots.Hotbar + Slots.OffHand + Slots.Inventory

    private val offHandItem by enumChoice("OffHandItem", ItemSortChoice.SHIELD)
    private val slotItem1 by enumChoice("SlotItem-1", ItemSortChoice.WEAPON)
    private val slotItem2 by enumChoice("SlotItem-2", ItemSortChoice.BOW)
    private val slotItem3 by enumChoice("SlotItem-3", ItemSortChoice.PICKAXE)
    private val slotItem4 by enumChoice("SlotItem-4", ItemSortChoice.AXE)
    private val slotItem5 by enumChoice("SlotItem-5", ItemSortChoice.NONE)
    private val slotItem6 by enumChoice("SlotItem-6", ItemSortChoice.POTION)
    private val slotItem7 by enumChoice("SlotItem-7", ItemSortChoice.FOOD)
    private val slotItem8 by enumChoice("SlotItem-8", ItemSortChoice.BLOCK)
    private val slotItem9 by enumChoice("SlotItem-9", ItemSortChoice.BLOCK)

    private val maxBlocks by int("MaximumBlocks", 512, 0..2500)
    private val maxArrows by int("MaximumArrows", 128, 0..2500)
    private val maxThrowables by int("MaximumThrowables", 64, 0..600)
    private val maxFoods by int("MaximumFoodPoints", 200, 0..2000)

    private val isGreedy by boolean("Greedy", true)

    val cleanupTemplateFromSettings: CleanupPlanPlacementTemplate
        get() {
            val slotTargets = hashMapOf<ItemSlot, ItemSortChoice>(
                Pair(OffHandSlot, offHandItem),
                Pair(Slots.Hotbar[0], slotItem1),
                Pair(Slots.Hotbar[1], slotItem2),
                Pair(Slots.Hotbar[2], slotItem3),
                Pair(Slots.Hotbar[3], slotItem4),
                Pair(Slots.Hotbar[4], slotItem5),
                Pair(Slots.Hotbar[5], slotItem6),
                Pair(Slots.Hotbar[6], slotItem7),
                Pair(Slots.Hotbar[7], slotItem8),
                Pair(Slots.Hotbar[8], slotItem9),
            )

            val forbiddenSlots = slotTargets
                .filterValues { it == ItemSortChoice.IGNORE }
                .keys.toHashSet()

            // Disallow tampering with armor slots since auto armor already handles them
            forbiddenSlots += Slots.Armor

            if (ModuleOffhand.isOperating()) {
                // Disallow tampering with off-hand slot when AutoTotem is active
                forbiddenSlots.add(OffHandSlot)
            }

            val forbiddenSlotsToFill = setOfNotNull(
                // Disallow tampering with off-hand slot when AutoTotem is active
                if (ModuleOffhand.isOperating()) OffHandSlot else null
            )

            val constraintProvider = AmountConstraintProvider(
                desiredItemsPerCategory = hashMapOf(
                    Pair(ItemSortChoice.BLOCK.category!!, maxBlocks),
                    Pair(ItemSortChoice.THROWABLES.category!!, maxThrowables),
                    Pair(ItemCategory(ItemType.ARROW, 0), maxArrows),
                ),
                desiredValuePerFunction = hashMapOf(
                    Pair(ItemFunction.FOOD, maxFoods),
                    Pair(ItemFunction.WEAPON_LIKE, 1),
                )
            )

            return CleanupPlanPlacementTemplate(
                slotTargets,
                itemAmountConstraintProvider = constraintProvider::getConstraints,
                forbiddenSlots = forbiddenSlots,
                forbiddenSlotsToFill = forbiddenSlotsToFill,
                isGreedy = isGreedy,
            )
        }

    @Suppress("unused")
    private val handleInventorySchedule = handler<ScheduleInventoryActionEvent> { event ->
        val preset = inventoryPresets.merged() { presetItem ->
            presetItem != NonePresetItem && affectedSlots.find { slot ->
                presetItem.satisfies(slot.itemStack)
            } != null
        } ?: return@handler

        event.swapToHotbar(preset)

        for (slot in findItemsToThrowOut(preset)) {
            event.schedule(
                inventoryConstraints,
                ClickInventoryAction.performThrow(null, slot),
                Priority.NOT_IMPORTANT
            )
        }
    }

    @Suppress("LoopWithTooManyJumpStatements")
    private fun ScheduleInventoryActionEvent.swapToHotbar(preset: InventoryPreset) {
        for (i in preset.items.indices) {
            val presetItem = preset.items[i]
            val slotItem = preset.itemAsHotbarItemSlot(i)

            val candidate = presetItem.findCandidates().takeIf { it.isNotEmpty() }
                ?.findCandidate(presetItem) ?: continue

            if (candidate == slotItem) {
                continue
            }

            if (i > 0) {
                var found = false
                for (prevIndex in 0..i) {
                    if (candidate is HotbarItemSlot
                        && preset.items[prevIndex].satisfies(candidate.itemStack)
                    ) {
                        found = true
                        break
                    }
                }

                if (found) {
                    continue
                }
            }

            schedule(
                inventoryConstraints,
                ClickInventoryAction.performSwap(null, candidate, slotItem)
            )
        }
    }

    private fun List<ItemSlot>.findCandidate(presetItem: PresetItem) = sortedWith { a, b ->
        presetItem.comparatorChain.compare(a.itemStack, b.itemStack)
    }.firstOrNull()

    private fun PresetItem.findCandidates() =
        affectedSlots.filter { slot -> satisfies(slot.itemStack) }

    private fun findItemsToThrowOut(
        preset: InventoryPreset
    ) = affectedSlots
        .filter { !it.itemStack.isEmpty }
        .filter { it.itemStack.item in preset.throws }

    fun findItemsToThrowOut(
        cleanupPlan: InventoryCleanupPlan,
        itemsInInv: List<ItemSlot>,
    ) = itemsInInv.filter { it !in cleanupPlan.usefulItems }

    private class AmountConstraintProvider(
        val desiredItemsPerCategory: Map<ItemCategory, Int>,
        val desiredValuePerFunction: Map<ItemFunction, Int>,
    ) {
        fun getConstraints(facet: ItemFacet): ArrayList<ItemConstraintInfo> {
            val constraints = ArrayList<ItemConstraintInfo>()

            if (facet.providedItemFunctions.isEmpty()) {
                val defaultDesiredAmount = if (facet.category.type.oneIsSufficient) 1 else Integer.MAX_VALUE
                val desiredAmount = this.desiredItemsPerCategory[facet.category] ?: defaultDesiredAmount

                val info = ItemConstraintInfo(
                    group = ItemCategoryConstraintGroup(
                        desiredAmount..Integer.MAX_VALUE,
                        10,
                        facet.category
                    ),
                    amountAddedByItem = facet.itemStack.count
                )

                constraints.add(info)
            } else {
                for ((function, amountAdded) in facet.providedItemFunctions) {
                    val info = ItemConstraintInfo(
                        group = ItemFunctionCategoryConstraintGroup(
                            desiredValuePerFunction.getOrDefault(function, 1)..Integer.MAX_VALUE,
                            10,
                            function
                        ),
                        amountAddedByItem = amountAdded
                    )

                    constraints.add(info)
                }
            }

            return constraints
        }
    }
}
