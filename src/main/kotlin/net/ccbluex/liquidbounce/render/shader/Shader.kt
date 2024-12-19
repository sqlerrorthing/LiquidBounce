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
package net.ccbluex.liquidbounce.render.shader

import com.mojang.blaze3d.platform.GlConst
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.gl.GlProgramManager
import java.io.Closeable

open class Shader(vertex: String, fragment: String, private val uniforms: Array<UniformProvider> = emptyArray()) :
    Closeable {

    var program = 0

    init {
        val vertProgram = compileShader(vertex, GlConst.GL_VERTEX_SHADER)
        val fragProgram = compileShader(fragment, GlConst.GL_FRAGMENT_SHADER)

        this.program = GlStateManager.glCreateProgram()

        GlStateManager.glAttachShader(program, vertProgram)
        GlStateManager.glAttachShader(program, fragProgram)
        GlStateManager.glLinkProgram(program)

        // Checks link status
        if (GlStateManager.glGetProgrami(program, GlConst.GL_LINK_STATUS) == GlConst.GL_FALSE) {
            val log = GlStateManager.glGetProgramInfoLog(program, 1024)
            error("Filed to link shader program! Caused by: $log")
        }

        // cleanup
        GlStateManager.glDeleteShader(vertProgram)
        GlStateManager.glDeleteShader(fragProgram)

        uniforms.forEach { uniform ->
            uniform.init(program)
        }
    }

    private fun compileShader(source: String, type: Int): Int {
        val shader = GlStateManager.glCreateShader(type)
        GlStateManager.glShaderSource(shader, listOf(source))
        GlStateManager.glCompileShader(shader)

        // check compilation status
        if (GlStateManager.glGetShaderi(shader, GlConst.GL_COMPILE_STATUS) == GlConst.GL_FALSE) {
            val log = GlStateManager.glGetShaderInfoLog(shader, 1024)
            error("Filed to compile shader! Caused by: $log")
        }

        return shader
    }

    fun use() {
        GlProgramManager.useProgram(this.program)
        uniforms.forEach { uniform ->
            uniform.set(uniform.pointer)
        }
    }

    fun stop() {
        GlProgramManager.useProgram(0)
    }

    override fun close() {
        GlStateManager.glDeleteProgram(this.program)
    }

}
