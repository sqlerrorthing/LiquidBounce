package net.ccbluex.liquidbounce.features.inventoryPresets.filters.rules.conditions

enum class StringConditions(
    override val humanizedName: String,
    override val testCondition: (lhs: String, rhs: String) -> Boolean
) : RuleCondition<String> {
    EQUALS("equals", { lhs, rhs -> lhs == rhs }),
    DOESNT_EQUAL("does not equal", { lhs, rhs -> lhs !== rhs }),
    MATCHES_REGEX("matches regex", { lhs, rhs -> rhs.toRegex().matches(lhs) }),
    DOESNT_MATCH_REGEX("does not match regex", { lhs, rhs -> !rhs.toRegex().matches(lhs) }),
    CONTAINS("contains", { lhs, rhs -> lhs.contains(rhs)}),
    DOESNT_CONTAIN("does not contains", { lhs, rhs -> !lhs.contains(rhs)});

    override val conditionType = ConditionType.STRING
}
