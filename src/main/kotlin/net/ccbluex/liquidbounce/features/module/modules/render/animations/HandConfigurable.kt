package net.ccbluex.liquidbounce.features.module.modules.render.animations

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable

@Suppress("MagicNumber")
sealed class HandConfigurable(
    name: String,
    positionOffset: Float
): ToggleableConfigurable(ModuleAnimations, name, false) {
    val itemScale by float("ItemScale", 0f, -5f..5f)
    val x by float("X", 0f, -positionOffset..positionOffset)
    val y by float("Y", 0f, -positionOffset..positionOffset)
    val positiveX by float("PositiveRotationX", 0f, -50f..50f)
    val positiveY by float("PositiveRotationY", 0f, -50f..50f)
    val positiveZ by float("PositiveRotationZ", 0f, -50f..50f)
}

@Suppress("MagicNumber")
object MainHandConfiguration : HandConfigurable("MainHand", 5f)

@Suppress("MagicNumber")
object OffHandConfiguration : HandConfigurable("OffHand", 1f)
