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
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.CleanupPlanPlacementTemplate.CleanupPlanRestrictions
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.CleanupPlanPlacementTemplate.CleanupPlanRestrictions.RestrictionType
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.CleanupPlanPlacementTemplate.CleanupPlanSlotContent
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.items.ItemFacet
import net.ccbluex.liquidbounce.features.module.modules.player.offhand.ModuleOffhand
import net.ccbluex.liquidbounce.utils.inventory.*
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.kotlin.component1
import net.ccbluex.liquidbounce.utils.kotlin.component2
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType

/**
 * InventoryManager module
 *
 * Automatically throws away useless items and sorts them.
 */
object ModuleInventoryManager : ClientModule("InventoryManager", Category.PLAYER,
    aliases = arrayOf("InventoryCleaner")
) {

    private val inventoryConstraints = tree(PlayerInventoryConstraints())

    @Suppress("unused")
    private val inventoryPresets by inventoryPreset()

    private val maxBlocks by int("MaximumBlocks", 512, 0..2500)
    private val maxArrows by int("MaximumArrows", 128, 0..2500)
    private val maxThrowables by int("MaximumThrowables", 64, 0..600)
    private val maxFoods by int("MaximumFoodPoints", 200, 0..2000)

    private val offHandItem by enumChoice("OffHandItem", GenericItemSortChoices.ANY)
    private val slotItem1 by enumChoice("SlotItem-1", GenericItemSortChoices.WEAPON)
    private val slotItem2 by enumChoice("SlotItem-2", GenericItemSortChoices.ANY)
    private val slotItem3 by enumChoice("SlotItem-3", GenericItemSortChoices.PICKAXE)
    private val slotItem4 by enumChoice("SlotItem-4", GenericItemSortChoices.AXE)
    private val slotItem5 by enumChoice("SlotItem-5", GenericItemSortChoices.ANY)
    private val slotItem6 by enumChoice("SlotItem-6", GenericItemSortChoices.POTION)
    private val slotItem7 by enumChoice("SlotItem-7", GenericItemSortChoices.FOOD)
    private val slotItem8 by enumChoice("SlotItem-8", GenericItemSortChoices.BLOCK)
    private val slotItem9 by enumChoice("SlotItem-9", GenericItemSortChoices.BLOCK)

    val cleanupTemplateFromSettings: CleanupPlanPlacementTemplate
        get() {
            val specifiedSlotTargets = listOf(
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
            val mapped = specifiedSlotTargets
                .filter { (_, choice) -> choice.category != null }
                .map { (slot, choice) -> slot to CleanupPlanSlotContent(listOf(CleanupPlanPlacementTemplate.ContentWish(choice.category!!.type, setOf(choice.category.subtype))), 0) }
                .toTypedArray()

            val slotTargets = hashMapOf<ItemSlot, CleanupPlanSlotContent>(pairs = mapped)

            val currentRestrictionMap = hashMapOf<ItemSlot, RestrictionType>()

            // Disallow tampering with armor slots since auto armor already handles them
            Slots.Armor.forEach { currentRestrictionMap.put(it, RestrictionType.FORBID_TAMPERING) }

            if (ModuleOffhand.isOperating()) {
                // Disallow tampering with off-hand slot when AutoTotem is active
                currentRestrictionMap[OffHandSlot] = RestrictionType.FORBID_REPLACING
            }

            val constraintProvider = AmountItemAmountConstraintProvider(
                desiredValuePerFunction = hashMapOf(
                    ItemFunction.FOOD to maxFoods,
                    ItemFunction.WEAPON_LIKE to 1,
                ),
                desiredItemsInSpecificCategories = hashMapOf(
                    listOf(ItemCategory(GenericItemType.ANY_ITEM, Items.EGG)) to 64,
                    listOf(ItemCategory(GenericItemType.ANY_ITEM, Items.EGG), ItemCategory(GenericItemType.ANY_ITEM, Items.SNOWBALL)) to 32,
                    Pair(listOf(GenericItemSortChoices.BLOCK.category!!), maxBlocks),
                    Pair(listOf(GenericItemSortChoices.THROWABLES.category!!), maxThrowables),
                    Pair(listOf(ItemCategory(GenericItemType.ARROW, 0)), maxArrows),
                )
            )


            return CleanupPlanPlacementTemplate(
                slotTargets,
                itemAmountConstraintProvider = constraintProvider,
                restrictions = CleanupPlanRestrictions(currentRestrictionMap)
            )
        }

    @Suppress("unused")
    private val handleInventorySchedule = handler<ScheduleInventoryActionEvent> { event ->
        val cleanupPlan = CleanupPlanGenerator(cleanupTemplateFromSettings, findNonEmptySlotsInInventory())
            .generatePlan()

        // Step 1: Move items to the correct slots
        for (hotbarSwap in cleanupPlan.swaps) {
            check(hotbarSwap.to is HotbarItemSlot) { "Cannot swap to non-hotbar-slot" }

            event.schedule(
                inventoryConstraints,
                ClickInventoryAction.performSwap(null, hotbarSwap.from, hotbarSwap.to)
            )

            // todo: run when successful or do not care?
            cleanupPlan.remapSlots(
                hashMapOf(
                    Pair(hotbarSwap.from, hotbarSwap.to),
                    Pair(hotbarSwap.to, hotbarSwap.from),
                )
            )
        }

        // Step 2: Merge stacks
        val stacksToMerge = ItemMerge.findStacksToMerge(cleanupPlan)
        for (slot in stacksToMerge) {
            event.schedule(
                inventoryConstraints,
                ClickInventoryAction.click(null, slot, 0, SlotActionType.PICKUP),
                ClickInventoryAction.click(null, slot, 0, SlotActionType.PICKUP_ALL),
                ClickInventoryAction.click(null, slot, 0, SlotActionType.PICKUP),
            )
        }

        // It is important that we call findItemSlotsInInventory() here again, because the inventory has changed.
        val itemsToThrowOut = findItemsToThrowOut(cleanupPlan, findNonEmptySlotsInInventory())

        for (slot in itemsToThrowOut) {
            event.schedule(
                inventoryConstraints,
                ClickInventoryAction.performThrow(screen = null, slot),
                Priority.NOT_IMPORTANT
            )
        }
    }

    fun findItemsToThrowOut(
        cleanupPlan: InventoryCleanupPlan,
        itemsInInv: List<ItemSlot>,
    ) = itemsInInv.filter { it !in cleanupPlan.usefulItems }

    private class AmountItemAmountConstraintProvider(
        val desiredValuePerFunction: Map<ItemFunction, Int>,
        /**
         * Contains information about specific item groups constraints like `[snowball, egg] -> 32`.
         * In that example, the inventory cleaner would not start throwing out items until at least 32 items of
         * snowballs or eggs are in the inventory.
         */
        desiredItemsInSpecificCategories: Map<List<ItemCategory>, Int>
    ) : ItemAmountConstraintProvider {
        /**
         * Contains all specific item groups in which an item is.
         *
         * For these rules: `[egg, snowball] -> 32, [egg, carrot] -> 64`, this list would look like this:
         * - `egg` -> `[0, 1]`
         * - `snowball` -> `[0]`
         * - `carrot` -> `[1]`
         */
        private val itemSpecificGroupMap: Map<ItemCategory, List<SpecificItemGroup>> = run {
            desiredItemsInSpecificCategories.entries
                .flatMapIndexed { idx, (items, desiredAmount) ->
                    val group = SpecificItemGroup(id = idx, desiredAmount = desiredAmount, priority = idx)

                    items.map { it to group }
                }
                .groupBy { it.first }
                .mapValues { list -> list.value.map { it.second } }
        }

        override fun getConstraints(facet: ItemFacet): ArrayList<ItemConstraintInfo> {
            val constraints = ArrayList<ItemConstraintInfo>()

            for (group in this.itemSpecificGroupMap.getOrDefault(facet.category, emptyList())) {
                val info = ItemConstraintInfo(
                    group = SpecificItemGroupConstraintGroup(
                        acceptableRange = group.desiredAmount..Integer.MAX_VALUE,
                        priority = group.priority,
                        groupId = group.id
                    ),
                    amountAddedByItem = facet.itemStack.count,
                    default = false
                )

                constraints.add(info)
            }

            for ((function, amountAdded) in facet.providedItemFunctions) {
                val configuredDesiredAmount = desiredValuePerFunction[function]

                val (default, desiredAmount) = if (configuredDesiredAmount != null) {
                    false to configuredDesiredAmount
                } else {
                    true to 1
                }

                val info = ItemConstraintInfo(
                    group = ItemFunctionCategoryConstraintGroup(
                        desiredAmount..Integer.MAX_VALUE,
                        1000,
                        function
                    ),
                    amountAddedByItem = amountAdded,
                    default = default
                )

                constraints.add(info)
            }

            if (facet.providedItemFunctions.isEmpty() && facet.category.type != GenericItemType.ANY_ITEM) {
                val defaultDesiredAmount = if (facet.category.type.oneIsSufficient) 1 else Integer.MAX_VALUE

                val info = ItemConstraintInfo(
                    group = ItemCategoryConstraintGroup(
                        defaultDesiredAmount..Integer.MAX_VALUE,
                        1000,
                        facet.category
                    ),
                    amountAddedByItem = facet.itemStack.count,
                    default = true
                )

                constraints.add(info)
            }

            return constraints
        }

        override fun getAllocationPriority(itemGroup: ItemCategory): Int {
            return -(this.itemSpecificGroupMap[itemGroup]?.maxBy { it.priority }?.priority ?: 0)
        }

        private class SpecificItemGroup(val id: Int, val desiredAmount: Int, val priority: Int)
    }
}
