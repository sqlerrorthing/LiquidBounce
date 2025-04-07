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

import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.CleanupPlanTemplate.CleanupPlanRestrictions.RestrictionType
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.CleanupPlanTemplate.SlotContentPreference
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.ItemPacker.ItemAmountContraintEnforcer.SatisfactionStatus
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.items.ItemFacet
import net.ccbluex.liquidbounce.utils.inventory.ItemSlot
import net.ccbluex.liquidbounce.utils.item.isNothing
import net.ccbluex.liquidbounce.utils.sorting.ComparatorChain

class CleanupPlanGenerator(
    private val template: CleanupPlanTemplate,
    private val availableItems: List<ItemSlot>,
) : ItemPacker.ItemAmountContraintEnforcer {
    private val swaps: ArrayList<InventorySwap> = ArrayList()
    private val currentLimit = HashMap<ItemNumberContraintGroup, Int>()

    fun generatePlan(): InventoryCleanupPlan {
        val allItemFacets = getAvailableItemFacetsToFillIn()
        val availableItemFacets = allItemFacets.filter {
            this.template.restrictions.getRestrictionFor(it.itemSlot) < RestrictionType.FORBID_REPLACING
        }

        val wishOrganizer = WishOrganizer(this.template)
        val itemDispenserRack = ItemDispenserRack(wishOrganizer, availableItemFacets)

        val alreadyFilledSlots = HashSet<ItemSlot>()

        val usefulItems = HashSet<ItemSlot>()

        // Consider all slots that we aren't allowed to fill in as already filled in.
        alreadyFilledSlots.addAll(template.restrictions.getSlotsWithAtLeast(RestrictionType.FORBID_REPLACING))
        // Consider all slots that may not be touched at all as useful.
        usefulItems.addAll(template.restrictions.getSlotsWithAtLeast(RestrictionType.FORBID_TAMPERING))

        for (wish in wishOrganizer.organizedWishes) {
            // If a better wish was already fulfilled, skip this second wish.
            if (wish.targetSlot in alreadyFilledSlots) {
                continue
            }

            val availableItem = itemDispenserRack.nextItemForGroup(wish.id)

            if (availableItem != null && availableItem.itemSlot != wish.targetSlot) {
                alreadyFilledSlots.add(wish.targetSlot)

                this.swaps.add(
                    InventorySwap(
                        from = availableItem.itemSlot,
                        to = wish.targetSlot,
                        priority = availableItem.category.type.allocationPriority
                    )
                )

                usefulItems.add(availableItem.itemSlot)
            }
        }

        val facetsGroupedByCategory = allItemFacets
            .groupBy { it.category }
            .entries
            .sortedBy { this.template.itemAmountConstraintProvider.getAllocationPriority(it.key) }

        for ((_, facetsInCategory) in facetsGroupedByCategory) {
            for (facet in facetsInCategory.sortedDescending()) {
                val satisfactionStatus = this.getSatisfactionStatus(facet)

                when (satisfactionStatus) {
                    SatisfactionStatus.NOT_SATISFIED -> {
                        this.addItem(facet)

                        usefulItems.add(facet.itemSlot)
                    }

                    SatisfactionStatus.SATISFIED -> {}
                    SatisfactionStatus.OVERSATURATED -> {
                        // TODO: Implement oversaturated behaviour.
                        //  Currently this is a feature that is not used in the UI.
                    }
                }
            }
        }

        return InventoryCleanupPlan(
            usefulItems = usefulItems,
            swaps = swaps,
            mergeableItems = groupItemsByType(),
        )
    }

    /**
     * Discovers all facets from the available slots. Filters out any slot that has been restricted
     */
    private fun getAvailableItemFacetsToFillIn(): List<ItemFacet> {
        val categorizer = ItemCategorization(availableItems)

        val availableItemFacets = availableItems
            .flatMap { categorizer.getItemFacets(it).asIterable() }

        return availableItemFacets
    }

    private fun groupItemsByType(): HashMap<ItemId, MutableList<ItemSlot>> {
        val itemsByType = HashMap<ItemId, MutableList<ItemSlot>>()

        for (availableSlot in this.availableItems) {
            val stack = availableSlot.itemStack

            if (stack.isNothing()) {
                continue
            }
            if (!stack.isStackable || stack.count >= stack.maxCount) {
                continue
            }

            val itemType = ItemId(stack.item, stack.components)
            val stacksOfType = itemsByType.computeIfAbsent(itemType) { mutableListOf() }

            stacksOfType.add(availableSlot)
        }

        return itemsByType
    }

    override fun getSatisfactionStatus(item: ItemFacet): SatisfactionStatus {
        val constraints = this.template.itemAmountConstraintProvider.getApplyingConstraints(item)

        constraints.sortBy { it.group.priority }

        for (constraintInfo in constraints) {
            val currentCount = this.currentLimit[constraintInfo.group] ?: 0

            if (currentCount > constraintInfo.group.acceptableRange.last) {
                return SatisfactionStatus.OVERSATURATED
            } else if (currentCount < constraintInfo.group.acceptableRange.first) {
                return SatisfactionStatus.NOT_SATISFIED
            }
        }

        return SatisfactionStatus.SATISFIED
    }

    override fun addItem(item: ItemFacet) {
        val constraints = this.template.itemAmountConstraintProvider.getApplyingConstraints(item)

        for (constraintInfo in constraints) {
            val current = this.currentLimit.getOrDefault(constraintInfo.group, 0)

            this.currentLimit[constraintInfo.group] = current + constraintInfo.amountAddedByItem
        }
    }
}

class ItemDispenserRack(wishOrganizer: WishOrganizer, itemFacets: List<ItemFacet>) {
    private val dispensersForType: Map<WishOrganizer.WishItemGroupId, ItemDispenser>
    private val alreadyDispensedItemSlots = HashSet<ItemSlot>()

    init {
        val wishGroupAvailableFacetMap = HashMap<WishOrganizer.WishItemGroupId, ArrayList<ItemFacet>>()

        for (facet in itemFacets) {
            val wishGroupsForFacet = wishOrganizer.itemCategoryWishGroupMap[facet.category] ?: continue

            for (id in wishGroupsForFacet) {
                wishGroupAvailableFacetMap.computeIfAbsent(id) { ArrayList() }.add(facet)
            }
        }

        wishGroupAvailableFacetMap.values.forEach { facetList -> facetList.sortDescending() }

        this.dispensersForType = wishGroupAvailableFacetMap.mapValues { ItemDispenser(it.value) }
    }

    fun nextItemForGroup(id: WishOrganizer.WishItemGroupId) = this.dispensersForType[id]?.nextItem()

    private inner class ItemDispenser(itemList: List<ItemFacet>) {
        private val itemListIterable: Iterator<ItemFacet> = itemList.iterator()

        fun nextItem(): ItemFacet? {
            while (this.itemListIterable.hasNext()) {
                val currentItem = this.itemListIterable.next()

                // Check if this item slot has already been dispensed.
                // This is possible as an item might appear in multiple dispensers.
                if (alreadyDispensedItemSlots.add(currentItem.itemSlot)) {
                    return currentItem
                }
            }

            return null
        }
    }
}

class WishOrganizer(template: CleanupPlanTemplate) {
    val organizedWishes = ArrayList<OrganizedWish>()
    val itemCategoryWishGroupMap = HashMap<ItemCategory, ArrayList<WishItemGroupId>>()

    init {
        // Deduplicate wishes for performance reasons.
        val wishIdMap = HashMap<SlotContentPreference, WishItemGroupId>()

        for ((slot, content) in template.slotContentMap.entries) {
            content.slotContentPreferences.forEachIndexed { wishIndexInSlot, wish ->
                val id = wishIdMap.computeIfAbsent(wish) { WishItemGroupId() }

                organizedWishes.add(
                    OrganizedWish(
                        id = id,
                        slotPriority = content.priority,
                        indexInSlot = wishIndexInSlot,
                        targetSlot = slot,
                        wish = wish
                    )
                )
            }
        }

        organizedWishes.sortWith(
            ComparatorChain(
            compareBy { it.slotPriority },
            compareBy { it.indexInSlot },
            compareByDescending { it.wish.itemType.allocationPriority }
        ))

        wishIdMap.forEach { (wish, itemGroupId) ->
            for (subtype in wish.subtypes) {
                val itemCategory = ItemCategory(wish.itemType, subtype)

                val wishItemGroups = itemCategoryWishGroupMap.computeIfAbsent(itemCategory) { ArrayList() }

                wishItemGroups.add(itemGroupId)
            }
        }
    }

    fun yieldRemainingWishes(alreadyFilledSlots: HashSet<ItemSlot>) = sequence {
        for (wish in organizedWishes) {
            if (wish.targetSlot in alreadyFilledSlots) {
                continue
            }

            yield(wish)
        }
    }

    data class OrganizedWish(
        val id: WishItemGroupId,
        val targetSlot: ItemSlot,
        val slotPriority: Int,
        val indexInSlot: Int,
        val wish: SlotContentPreference
    )

    class WishItemGroupId
}

interface ItemAmountConstraintProvider {
    fun getConstraints(item: ItemFacet): ArrayList<ItemConstraintInfo>

    /**
     * Returns the priority of the given item category.
     * Categories with values are processed first.
     *
     * This is useful when it comes to finding the minimal number of items required to fulfill the constraints.
     * For example, if the constraints were `egg -> 64, egg, snowball -> 32`, it would be important to process the eggs
     * first so that no snowballs are kept when having > 32 eggs.
     */
    fun getAllocationPriority(itemGroup: ItemCategory): Int

    /**
     * Filters out not applying default configurations.
     *
     * See [ItemConstraintInfo.default] for further information on that.
     */
    fun getApplyingConstraints(item: ItemFacet): ArrayList<ItemConstraintInfo> {
        val constraints = getConstraints(item)

        if (constraints.any { !it.default }) {
            constraints.removeIf { it.default }
        }

        return constraints
    }
}


enum class ItemSlotType {
    HOTBAR,
    OFFHAND,
    ARMOR,
    INVENTORY,

    /**
     * e.g. chests
     */
    CONTAINER,
}
