package net.ccbluex.liquidbounce.features.module.modules.render.animations

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.features.module.modules.render.animations.ModuleAnimations.blockAnimationChoice
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis

/**
 * A choice that aims to transform the held item transformation during the swing progress.
 */
abstract class AnimationChoice(name: String) : Choice(name) {
    override val parent: ChoiceConfigurable<*>
        get() = blockAnimationChoice

    protected fun applySwingOffset(matrices: MatrixStack, arm: Arm, swingProgress: Float) {
        val armSide = if (arm == Arm.RIGHT) 1 else -1
        val f = MathHelper.sin(swingProgress * swingProgress * Math.PI.toFloat())
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide.toFloat() * (45.0f + f * -20.0f)))
        val g = MathHelper.sin(MathHelper.sqrt(swingProgress) * Math.PI.toFloat())
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(armSide.toFloat() * g * -20.0f))
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0f))
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(armSide.toFloat() * -45.0f))
    }

    abstract fun transform(matrices: MatrixStack, arm: Arm, equipProgress: Float, swingProgress: Float)
}
