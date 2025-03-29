package net.ccbluex.liquidbounce.features.inventoryPresets.filters.rules.conditions

interface RuleCondition<T> {
    val conditionType: ConditionType
    val humanizedName: String
    val testCondition: (lhs: T, rhs: T) -> Boolean
}
