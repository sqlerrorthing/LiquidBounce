package net.ccbluex.liquidbounce.features.inventoryPresets.filters.rules.conditions

enum class NumberConditions(
    override val humanizedName: String,
    override val testCondition: (lhs: Double, rhs: Double) -> Boolean
) : RuleCondition<Double> {
    EQUALS("equals", { lhs, rhs -> lhs == rhs }),
    NOT_EQUALS("not equals", { lhs, rhs -> lhs != rhs }),
    GREATER_THAN("greater than", { lhs, rhs -> lhs > rhs }),
    LESS_THAN("less than", { lhs, rhs -> lhs < rhs }),
    GREATER_THAN_OR_EQUAL("greater than or equal", { lhs, rhs -> lhs >= rhs }),
    LESS_THAN_OR_EQUAL("less than or equal", { lhs, rhs -> lhs <= rhs });

    override val conditionType = ConditionType.NUMBER
}
