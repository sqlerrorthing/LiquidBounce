package net.ccbluex.liquidbounce.features.inventoryPreset

/**
 * Represents a group restriction limiting the maximum total stacks for specific items.
 */
class FrontendItemLimitRules(
    val itemCount: Int,
    val items: Set<FrontendSlotPreference> = emptySet()
)
