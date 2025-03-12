package net.ccbluex.liquidbounce.features.module.modules.combat.criticals

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.features.module.modules.combat.criticals.ModuleCriticals.modes
import net.minecraft.entity.Entity

abstract class CriticalsMode(name: String) : Choice(name) {
    override val parent get() = modes

    open fun shouldWaitForCriticalHit(
        target: Entity,
        ignoreState: Boolean = false
    ) = false
}
