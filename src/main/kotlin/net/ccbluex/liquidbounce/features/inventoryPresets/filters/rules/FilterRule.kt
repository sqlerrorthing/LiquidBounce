package net.ccbluex.liquidbounce.features.inventoryPresets.filters.rules

import net.ccbluex.liquidbounce.features.inventoryPresets.filters.rules.conditions.RuleCondition

sealed class FilterRule<T>(
    val name: String,
    val condition: RuleCondition<T>,
    val ruleType: RuleType,
)
