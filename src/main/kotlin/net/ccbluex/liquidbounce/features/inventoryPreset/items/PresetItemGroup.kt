package net.ccbluex.liquidbounce.features.inventoryPreset.items

import net.ccbluex.liquidbounce.features.inventoryPreset.items.types.NonePresetItem
import net.ccbluex.liquidbounce.features.inventoryPreset.items.types.PresetItem

class PresetItemGroup(
    val items: Array<PresetItem>
) {
    init {
        if (items.isNotEmpty()) {
            require(items.find { it is NonePresetItem } != null) {
                "The array contains an element that cannot be contained in it - NonePresetItem. " +
                "if you need to indicate that there is NonePresetItem, pass an empty array."
            }
        }
    }
}
