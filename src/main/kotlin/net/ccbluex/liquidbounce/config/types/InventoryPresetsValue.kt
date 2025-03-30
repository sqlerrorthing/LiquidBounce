package net.ccbluex.liquidbounce.config.types

import net.ccbluex.liquidbounce.features.inventoryPresets.InventoryPreset
import net.ccbluex.liquidbounce.features.inventoryPresets.items.NonePresetItem
import net.ccbluex.liquidbounce.features.inventoryPresets.items.PresetItem

class InventoryPresetsValue : Value<List<InventoryPreset>>("InventoryPresets",
    defaultValue = emptyList(),
    valueType = ValueType.INVENTORY_PRESETS,
    listType = ListValueType.InventoryPreset
) {
    /**
     * Combines the [InventoryPreset] list into a single preset, applying priority rules:
     * - Items from presets with a lower index in the list have the highest priority.
     *   If the [predicate] does not contain this item,
     *   then the item can be replaced with an item with a lower priority.
     * - [NonePresetItem] can be replaced by an item from a lower priority preset
     * - Non-[NonePresetItem] items are protected from being overwritten
     */
    fun merged(predicate: (PresetItem) -> Boolean = { true }): InventoryPreset? {
        val presets = get()

        return when {
            presets.isEmpty() -> null
            presets.size == 1 -> presets[0]
            else -> presets.merge(predicate)
        }
    }

    @Suppress("MagicNumber")
    private fun List<InventoryPreset>.merge(predicate: (PresetItem) -> Boolean): InventoryPreset {
        require(isNotEmpty()) {
            "At least one preset should be provided."
        }

        val mergedItems = Array(10) { index ->
            var selectedItem: PresetItem = NonePresetItem

            @Suppress("LoopWithTooManyJumpStatements")
            for (preset in this) {
                if (index >= preset.items.size) {
                    continue
                }

                val item = preset.items[index]

                if (item == NonePresetItem) {
                    if (selectedItem == NonePresetItem) {
                        continue
                    }
                } else {
                    if (predicate(item)) {
                        selectedItem = item
                        break
                    }
                }
            }

            selectedItem
        }

        val mergedThrows = this
            .flatMap { it.throws }
            .toSet()

        return InventoryPreset(mergedItems, mergedThrows)
    }
}
