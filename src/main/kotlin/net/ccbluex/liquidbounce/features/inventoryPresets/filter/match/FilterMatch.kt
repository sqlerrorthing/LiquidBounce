package net.ccbluex.liquidbounce.features.inventoryPresets.filter.match

import net.ccbluex.liquidbounce.features.inventoryPresets.FilterMatchType
import net.ccbluex.liquidbounce.features.inventoryPresets.items.PresetItem
import net.ccbluex.liquidbounce.utils.item.attackDamage
import net.ccbluex.liquidbounce.utils.item.durability
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack

sealed class FilterMatch<T : Any>(
    val type: FilterMatchType,
    val conditionValueType: ConditionValueType,
    val condition: MatchCondition<*, *>,
    val valueType: MatchValueType,
    val value: T
) {
    open fun ItemStack.getLhrValue(): T? = TODO("Not yet implemented")

    open fun ItemStack.getLhrValueAsAny(): Any? = TODO("Not yet implemented")

    @Suppress("UNCHECKED_CAST")
    fun matches(itemStack: ItemStack): Boolean {
        return when (valueType) {
            MatchValueType.REGULAR -> (condition as RegularMatchCondition<T>).test(
                itemStack.getLhrValue() ?: return false, value
            )
            MatchValueType.SET -> {(condition as SetMatchConditions).test(
                (value as Set<T>), itemStack.getLhrValueAsAny() ?: return false
            ) }
        }
    }
}

class DurabilityMatch(
    condition: NumberConditions,
    value: Double
) : FilterMatch<Double> (
    type = FilterMatchType.DURABILITY,
    conditionValueType = ConditionValueType.NUMBER,
    condition = condition,
    valueType = MatchValueType.REGULAR,
    value = value
) {
    override fun ItemStack.getLhrValue(): Double = this.durability.toDouble()
}

class AttackDamageMatch(
    condition: NumberConditions,
    value: Double
) : FilterMatch<Double> (
    type = FilterMatchType.ATTACK_DAMAGE,
    conditionValueType = ConditionValueType.NUMBER,
    condition = condition,
    valueType = MatchValueType.REGULAR,
    value = value
) {
    override fun ItemStack.getLhrValue(): Double = this.attackDamage
}

class QuantityMatch(
    condition: NumberConditions,
    value: Double
) : FilterMatch<Double> (
    type = FilterMatchType.QUANTITY,
    conditionValueType = ConditionValueType.NUMBER,
    condition = condition,
    valueType = MatchValueType.REGULAR,
    value = value
) {
    override fun ItemStack.getLhrValue(): Double = this.count.toDouble()
}

class DisplayNameMatch(
    condition: StringConditions,
    value: String
) : FilterMatch<String> (
    type = FilterMatchType.DISPLAY_NAME,
    conditionValueType = ConditionValueType.STRING,
    condition = condition,
    valueType = MatchValueType.REGULAR,
    value = value
) {
    override fun ItemStack.getLhrValue(): String = this.name.string
}

class ItemNameMatch(
    condition: StringConditions,
    value: String
) : FilterMatch<String> (
    type = FilterMatchType.ITEM_NAME,
    conditionValueType = ConditionValueType.STRING,
    condition = condition,
    valueType = MatchValueType.REGULAR,
    value = value
) {
    override fun ItemStack.getLhrValue(): String = this.itemName.string
}

class LoreMatch(
    condition: StringConditions,
    value: String
) : FilterMatch<String> (
    type = FilterMatchType.LORE,
    conditionValueType = ConditionValueType.STRING,
    condition = condition,
    valueType = MatchValueType.REGULAR,
    value = value
) {
    override fun ItemStack.getLhrValue(): String? =
        this.get(DataComponentTypes.LORE)?.styledLines?.firstOrNull()?.string
}

class ItemMatch(
    condition: SetMatchConditions,
    value: Set<PresetItem>
) : FilterMatch<Set<PresetItem>> (
    type = FilterMatchType.ITEM,
    conditionValueType = ConditionValueType.ITEM,
    condition = condition,
    valueType = MatchValueType.SET,
    value = value
) {
    override fun ItemStack.getLhrValueAsAny() = 
}
