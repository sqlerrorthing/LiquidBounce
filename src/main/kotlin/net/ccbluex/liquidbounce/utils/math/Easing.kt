/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.utils.math

import net.ccbluex.liquidbounce.config.types.NamedChoice
import kotlin.math.pow

/**
 * Functions from https://easings.net.
 */
@Suppress("unused")
enum class Easing(
    override val choiceName: String,
    val transform: (Float) -> Float
) : NamedChoice {

    LINEAR("Linear", { x ->
        x
    }),

    /**
     * https://easings.net/#easeInQuad
     */
    QUAD_IN("QuadIn", { x ->
        x * x
    }),

    /**
     * https://easings.net/#easeOutQuad
     */
    QUAD_OUT("QuadOut", { x ->
        1 - (1 - x) * (1 - x)
    }),

    /**
     * https://easings.net/#easeInOutQuad
     */
    QUAD_IN_OUT("QuadInOut", { x ->
        2 * (1 - x) * x * x + x * x
    }),

    /**
     * https://easings.net/#easeInExpo
     */
    EXPONENTIAL_IN("ExponentialIn", { x ->
        if (x == 0f) 0.0f else 2f.pow(10f * x - 10f)
    }),

    /**
     * https://easings.net/#easeOutExpo
     */
    EXPONENTIAL_OUT("ExponentialOut", { x ->
        if (x == 1f) 1.0f else 1f - 2f.pow(-10f * x)
    }),

    /**
     * https://easings.net/#easeOutExpo
     */
    EXPONENTIAL_IN_OUT("ExponentialInOut", { x ->
        when {
            x == 0f -> 0f
            x == 1f -> 1f
            x < 0.5f -> 2f.pow(20f * x - 10) / 2
            else -> (2f - 2f.pow(-20f * x + 10)) / 2
        }
    }),

    NONE("None", { 0f }) {
        override fun getFactor(startTime: Long, currentTime: Long, time: Float) = 1f
    };

    open fun getFactor(startTime: Long, currentTime: Long, time: Float): Float {
        val delta = currentTime - startTime
        val factor = (delta / time.toDouble()).toFloat().coerceIn(0F..1F)
        return transform(factor)
    }

}
