package net.ccbluex.liquidbounce.features.module.modules.render.animations.blocking

import net.ccbluex.liquidbounce.features.module.modules.render.animations.AnimationChoice
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis

/**
 * Based on the [applySwingOffset] but with a different transformation
 * during swing progress to make it look like the [PushdownAnimation] from LiquidBounce Legacy.
 *
 * This animation is not the same as the original, but it is similar.
 */
@Suppress("MagicNumber")
object PushdownAnimation : AnimationChoice("Pushdown") {
    override fun transform(matrices: MatrixStack, arm: Arm, equipProgress: Float, swingProgress: Float) {
        matrices.translate(if (arm == Arm.RIGHT) -0.1f else 0.1f, 0.1f, 0.0f)

        val g = MathHelper.sin(MathHelper.sqrt(swingProgress) * Math.PI.toFloat())
        matrices.multiply(
            RotationAxis.POSITIVE_Z.rotationDegrees(
                (if (arm == Arm.RIGHT) 1 else -1) * g * 10.0f
            )
        )
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -35.0f))

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25f))
        matrices.multiply(
            (if (arm == Arm.RIGHT) RotationAxis.POSITIVE_Y else RotationAxis.NEGATIVE_Y)
                .rotationDegrees(13.365f)
        )
        matrices.multiply(
            (if (arm == Arm.RIGHT) RotationAxis.POSITIVE_Z else RotationAxis.NEGATIVE_Z)
                .rotationDegrees(78.05f)
        )
    }
}
