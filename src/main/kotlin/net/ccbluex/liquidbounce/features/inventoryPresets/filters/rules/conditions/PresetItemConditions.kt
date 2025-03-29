package net.ccbluex.liquidbounce.features.inventoryPresets.filters.rules.conditions

import net.ccbluex.liquidbounce.features.inventoryPresets.items.PresetItem

enum class PresetItemConditions(
    override val humanizedName: String,
    override val testCondition: (lhs: Set<PresetItem>, rhs: Set<PresetItem>) -> Boolean
) : RuleCondition<Set<PresetItem>> {
    IS_IN("is in", { lhs, rhs -> lhs.containsAll(rhs) }),
    NOT_IN("not in", { lhs, rhs -> !lhs.containsAll(rhs) }),;

    override val conditionType = ConditionType.MATERIAL
}
