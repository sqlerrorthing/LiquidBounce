package net.ccbluex.liquidbounce.features.module.modules.render.animations

import net.ccbluex.liquidbounce.config.types.NamedChoice
import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable

object EquipOffset : ToggleableConfigurable(ModuleAnimations, "EquipOffset", true) {
    private val ignore by multiEnumChoice("Ignore",
        Ignores.BLOCKING,
        Ignores.PLACE
    )

    val ignoreBlocking get() = Ignores.BLOCKING in ignore
    val ignorePlace get() = Ignores.PLACE in ignore
    val ignoreAmount get() = Ignores.AMOUNT in ignore

    private enum class Ignores(
        override val choiceName: String
    ) : NamedChoice {
        BLOCKING("Blocking"),
        PLACE("Place"),
        AMOUNT("Amount")
    }
}
