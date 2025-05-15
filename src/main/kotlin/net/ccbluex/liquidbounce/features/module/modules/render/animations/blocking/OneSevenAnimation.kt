package net.ccbluex.liquidbounce.features.module.modules.render.animations.blocking

import net.ccbluex.liquidbounce.features.module.modules.render.animations.AnimationChoice
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.math.RotationAxis

/**
 * This animation is based on the 1.7 animation. It is the closest to the original animation
 * if not altered by the user.
 *
 * This animation is used in the ViaFabricPlus project.
 * https://github.com/ViaVersion/ViaFabricPlus/blob/9eb2adf6265cf0ac9d2a17921791642f2b0cdd2c/src/main/java/de/florianmichael/viafabricplus/injection/mixin/fixes/minecraft/item/MixinHeldItemRenderer.java#L50-L60
 */
@Suppress("MagicNumber")
object OneSevenAnimation : AnimationChoice("1.7") {
    private val translateY by float("Y", 0.1f, 0.05f..0.3f)
    private val swingProgressScale by float("SwingScale", 0.9f, 0.1f..1.0f)

    override fun transform(matrices: MatrixStack, arm: Arm, equipProgress: Float, swingProgress: Float) {
        matrices.translate(if (arm == Arm.RIGHT) -0.1f else 0.1f, translateY, 0.0f)
        applySwingOffset(matrices, arm, swingProgress * swingProgressScale)
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
