package net.ccbluex.liquidbounce.config.types

import net.ccbluex.liquidbounce.features.inventoryPresets.InventoryPreset
import net.ccbluex.liquidbounce.features.inventoryPresets.items.NonePresetItem

class InventoryPresetsValue : Value<List<InventoryPreset>>("InventoryPresets",
    defaultValue = emptyList(),
    valueType = ValueType.INVENTORY_PRESETS,
    listType = ListValueType.InventoryPreset
) {
    /**
     * Combines the [InventoryPreset] list into a single preset, applying priority rules:
     * - Items from presets with a lower index in the list have the highest priority
     * - [NonePresetItem] can be replaced by an item from a lower priority preset
     * - Non-[NonePresetItem] items are protected from being overwritten
     */
    fun merged(): InventoryPreset? {
        val presets = get()

        return when {
            presets.isEmpty() -> null
            presets.size == 1 -> presets[0]
            else -> presets.merge()
        }
    }

    @Suppress("MagicNumber")
    private fun List<InventoryPreset>.merge(): InventoryPreset {
        require(isNotEmpty()) {
            "At least one preset should be provided."
        }

        val mergedItems = Array(10) { index ->
            this.firstNotNullOfOrNull { preset ->
                preset.items.getOrNull(index)?.takeIf { it != NonePresetItem }
            } ?: NonePresetItem
        }

        val mergedThrows = this
            .flatMap { it.throws }
            .toSet()

        return InventoryPreset(mergedItems, mergedThrows)
    }
}
