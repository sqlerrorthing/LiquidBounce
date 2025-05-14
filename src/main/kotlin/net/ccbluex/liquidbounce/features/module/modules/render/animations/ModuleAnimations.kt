package net.ccbluex.liquidbounce.features.module.modules.render.animations

import net.ccbluex.liquidbounce.event.events.PlayerStrideEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.combat.ModuleSwordBlock
import net.ccbluex.liquidbounce.features.module.modules.render.animations.blocking.OneSevenAnimation
import net.ccbluex.liquidbounce.features.module.modules.render.animations.blocking.PushdownAnimation

/**
 * Animations module
 *
 * This module affects item animations. It allows the user to customize the animation.
 * If you are looking forward to contributing to this module, please name your animation with a reasonable name.
 * Do not name them after clients or yourself.
 * Please credit from where you got the animation from and make sure they are willing to contribute.
 * If they are not willing to contribute, please do not add the animation to this module.
 */
@Suppress("MagicNumber")
object ModuleAnimations : ClientModule("Animations", Category.RENDER, aliases = arrayOf("ViewModel")) {
    init {
        tree(MainHandConfiguration)
        tree(OffHandConfiguration)
        tree(EquipOffset)
    }

    val swingDuration by int("SwingDuration", 6, 1..20)

    /**
     * A choice that allows the user to choose the animation that will be used during the blocking
     * of a sword.
     * This choice is only used when the [ModuleSwordBlock] module is enabled.
     */
    val blockAnimationChoice = choices(
        "BlockingAnimation", OneSevenAnimation, arrayOf(
            OneSevenAnimation,
            PushdownAnimation
        )
    )

    /**
     * if true, the walk animation will also be applied in the air.
     */
    private val airWalker by boolean("AirWalker", false)

    @Suppress("unused")
    val strideHandler = handler<PlayerStrideEvent> { event ->
        if (airWalker) {
            event.strideForce = 0.1.coerceAtMost(player.velocity.horizontalLength()).toFloat()
        }
    }
}
