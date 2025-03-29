package net.ccbluex.liquidbounce.features.inventoryPresets.filter.match

sealed interface MatchCondition<L, R> {
    val test: (lhs: L, rhs: R) -> Boolean
}

sealed interface RegularMatchCondition<T> : MatchCondition<T, T>

enum class NumberConditions(
    override val test: (lhs: Double, rhs: Double) -> Boolean
) : RegularMatchCondition<Double> {
    EQUALS({ lhs: Double, rhs: Double -> lhs == rhs }),
    DOESNT_EQUAL({ lhs: Double, rhs: Double -> lhs != rhs }),
    GREATER_THAN({ lhs: Double, rhs: Double -> lhs > rhs }),
    LESS_THAN({ lhs: Double, rhs: Double -> lhs < rhs }),
    GREATER_THAN_OR_EQUAL({ lhs: Double, rhs: Double -> lhs >= rhs }),
    LESS_THAN_OR_EQUAL({ lhs: Double, rhs: Double -> lhs <= rhs });
}

enum class StringConditions(
    override val test: (lhs: String, rhs: String) -> Boolean
) : RegularMatchCondition<String> {
    EQUALS({ lhs, rhs -> lhs == rhs }),
    DOESNT_EQUAL({ lhs, rhs -> lhs != rhs }),
    MATCHES_REGEX({ lhs, rhs -> rhs.toRegex().matches(lhs) }),
    DOESNT_MATCH_REGEX({ lhs, rhs -> !rhs.toRegex().matches(lhs) }),
    CONTAINS({ lhs, rhs -> lhs.contains(rhs) }),
    DOESNT_CONTAINS({ lhs, rhs -> !lhs.contains(rhs) });
}

enum class SetMatchConditions(
    override val test: (lhs: Set<Any>, rhs: Any) -> Boolean
) : MatchCondition<Set<Any>, Any> {
    IN({ lhs, rhs -> rhs in lhs}),
    NOT_IN({ lhs, rhs -> rhs !in lhs });
}

enum class MatchValueType {
    REGULAR,
    SET
}

enum class ConditionValueType {
    STRING,
    NUMBER,
    ITEM
}
