package net.ccbluex.liquidbounce.features.inventoryPreset

import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.annotations.SerializedName
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.CleanupPlanPlacementTemplate
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.GenericItemType
import net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.items.MiningToolItemFacet
import net.minecraft.item.Item

/**
 * Contains the frontend representation of the user defined preference of what should a slot contain.
 */
sealed class FrontendSlotPreference {
    /**
     * Converts the frontend representation of the user
     * configured preset into a version
     * which the [net.ccbluex.liquidbounce.features.module.modules.player.invcleaner.CleanupPlanGenerator] understands.
     */
    abstract fun toBackendRepresentation(): ConvertedSlotPreference
    abstract fun serialize(context: JsonSerializationContext): JsonObject

    class SingleSlotPreference(private val item: Item) : FrontendSlotPreference() {
        override fun toBackendRepresentation(): ConvertedSlotPreference {
            val contentPreference = CleanupPlanPlacementTemplate.SlotContentPreference(
                itemType = GenericItemType.ANY_ITEM,
                subtypes = setOf(item)
            )

            return ConvertedSlotPreference(contentPreference)
        }

        override fun serialize(context: JsonSerializationContext) = JsonObject().apply {
            addProperty("type", "SINGLE")

            add("item", context.serialize(item))
        }
    }

    class GroupSlotPreference(private val itemGroupType: ItemGroupType) : FrontendSlotPreference() {
        override fun toBackendRepresentation(): ConvertedSlotPreference {
            return ConvertedSlotPreference(itemGroupType.preference)
        }

        /**
         * Enum representing item categories used for preset item classification.
         */
        enum class ItemGroupType(val preference: CleanupPlanPlacementTemplate.SlotContentPreference) {
            @SerializedName("ARROWS")
            ARROWS(CleanupPlanPlacementTemplate.SlotContentPreference(GenericItemType.ARROW)),
            @SerializedName("SWORD")
            SWORD(CleanupPlanPlacementTemplate.SlotContentPreference(GenericItemType.SWORD)),
            @SerializedName("WEAPON")
            WEAPON(CleanupPlanPlacementTemplate.SlotContentPreference(GenericItemType.WEAPON)),
            @SerializedName("AXE")
            AXE_TOOL(
                CleanupPlanPlacementTemplate.SlotContentPreference(
                    GenericItemType.TOOL,
                    setOf(MiningToolItemFacet.ItemToolType.AXE)
                )
            ),
            @SerializedName("HOE")
            HOE_TOOL(
                CleanupPlanPlacementTemplate.SlotContentPreference(
                    GenericItemType.TOOL,
                    setOf(MiningToolItemFacet.ItemToolType.HOE)
                )
            ),
            @SerializedName("SHOVEL")
            SHOVEL_TOOL(
                CleanupPlanPlacementTemplate.SlotContentPreference(
                    GenericItemType.TOOL,
                    setOf(MiningToolItemFacet.ItemToolType.SHOVEL)
                )
            ),
            @SerializedName("PICKAXE")
            PICKAXE_TOOL(
                CleanupPlanPlacementTemplate.SlotContentPreference(
                    GenericItemType.TOOL,
                    setOf(MiningToolItemFacet.ItemToolType.PICKAXE)
                )
            ),
            @SerializedName("FOOD")
            FOOD(CleanupPlanPlacementTemplate.SlotContentPreference(GenericItemType.FOOD)),
            @SerializedName("POTION")
            POTION(CleanupPlanPlacementTemplate.SlotContentPreference(GenericItemType.POTION)),
            @SerializedName("BLOCK")
            BLOCK(CleanupPlanPlacementTemplate.SlotContentPreference(GenericItemType.BLOCK)),
            @SerializedName("THROWABLE")
            THROWABLE(CleanupPlanPlacementTemplate.SlotContentPreference(GenericItemType.THROWABLE))
        }

        override fun serialize(context: JsonSerializationContext) = JsonObject().apply {
            addProperty("type", "GROUP")

            add("group", context.serialize(itemGroupType))
        }
    }

    object IgnoreSlotPreference : FrontendSlotPreference() {
        override fun toBackendRepresentation(): ConvertedSlotPreference {
            return ConvertedSlotPreference(null, CleanupPlanPlacementTemplate.CleanupPlanRestrictions.RestrictionType.FORBID_TAMPERING)
        }

        override fun serialize(context: JsonSerializationContext) = JsonObject().apply {
            addProperty("type", "IGNORE")
        }
    }
    object AnySlotPreference : FrontendSlotPreference() {
        override fun toBackendRepresentation(): ConvertedSlotPreference {
            return ConvertedSlotPreference(null, CleanupPlanPlacementTemplate.CleanupPlanRestrictions.RestrictionType.NONE)
        }

        override fun serialize(context: JsonSerializationContext) = JsonObject().apply {
            addProperty("type", "ANY")
        }
    }

    data class ConvertedSlotPreference(
        val contentPreference: CleanupPlanPlacementTemplate.SlotContentPreference?,
        val slotRestriction: CleanupPlanPlacementTemplate.CleanupPlanRestrictions.RestrictionType = CleanupPlanPlacementTemplate.CleanupPlanRestrictions.RestrictionType.NONE
    )
}
