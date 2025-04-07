package net.ccbluex.liquidbounce.config.gson.adapter

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import net.ccbluex.liquidbounce.features.inventoryPreset.InventoryPreset
import net.ccbluex.liquidbounce.features.inventoryPreset.FrontendSlotPreference
import net.ccbluex.liquidbounce.features.inventoryPreset.FrontendItemLimitRules
import net.ccbluex.liquidbounce.utils.kotlin.mapArray
import java.lang.reflect.Type

object InventoryPresetAdapter : JsonSerializer<InventoryPreset>, JsonDeserializer<InventoryPreset> {
    override fun serialize(
        src: InventoryPreset,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement = JsonObject().apply {
        add("items", context.serialize(src.itemRulesToArray()))
        add("maxStacks", context.serialize(src.itemLimitRules))
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): InventoryPreset = with (json.asJsonObject) {
        val items = context.decode<Array<FrontendSlotPreference>>(get("items"))
        val throws = context.decode<Array<FrontendItemLimitRules>>(get("maxStacks"))

        return InventoryPreset(items.mapArray { listOf(it) }, throws)
    }

    private inline fun <reified T> JsonDeserializationContext.decode(element: JsonElement): T {
        return deserialize(element, object: TypeToken<T>() {}.type)
    }
}
