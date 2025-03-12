package net.ccbluex.liquidbounce.features.module.modules.combat.criticals.modes

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ModuleCriticals.modes

abstract class ModeCritical(name: String) : Choice(name) {
    override val parent get() = modes
}
