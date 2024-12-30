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
package net.ccbluex.liquidbounce.features.module.modules.render.nametags

import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.OverlayRenderEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleESP
import net.ccbluex.liquidbounce.render.FontManager
import net.ccbluex.liquidbounce.render.RenderEnvironment
import net.ccbluex.liquidbounce.render.engine.Vec3
import net.ccbluex.liquidbounce.render.renderEnvironmentForGUI
import net.ccbluex.liquidbounce.utils.combat.shouldBeShown
import net.ccbluex.liquidbounce.utils.kotlin.EventPriorityConvention
import net.ccbluex.liquidbounce.utils.math.sq
import net.minecraft.entity.Entity
import kotlin.math.abs

/**
 * Nametags module
 *
 * Makes player name tags more visible and adds useful information.
 */
object ModuleNametags : ClientModule("Nametags", Category.RENDER) {

    /**
     * Contains the list of toggleable options for the [NametagTextFormatter]
     */
    object ShowOptions : Configurable("Show") {
        val health by boolean("Health", true)
        val distance by boolean("Distance", true)
        val ping by boolean("Ping", true)
        val items by boolean("Items", true)
        val itemInfo by boolean("ItemInfo", true)
    }

    init {
        tree(ShowOptions)
    }

    val background by boolean("Background", true)
    val border by boolean("Border", true)
    val scale by float("Scale", 2F, 0.25F..4F)
    private val maximumDistance by float("MaximumDistance", 100F, 1F..256F)
    private var nametagsToRender: List<Nametag>? = null

    val fontRenderer
        get() = FontManager.FONT_RENDERER

    @Suppress("unused")
    val updateHandler = handler<GameTickEvent> {
        nametagsToRender = collectAndSortNametagsToRender()
    }

    @Suppress("unused")
    val worldChangeHandler = handler<WorldChangeEvent> {
        nametagsToRender = null
    }

    override fun disable() {
        nametagsToRender = null
    }

    override fun enable() {
        nametagsToRender = null
    }

    @Suppress("unused")
    val overlayRenderHandler = handler<OverlayRenderEvent>(priority = EventPriorityConvention.FIRST_PRIORITY) { event ->
        renderEnvironmentForGUI {
            val nametagRenderer = NametagRenderer()

            try {
                drawNametags(nametagRenderer, event.tickDelta)
            } finally {
                nametagRenderer.commit(this)
            }
        }
    }

    private fun RenderEnvironment.drawNametags(nametagRenderer: NametagRenderer, tickDelta: Float) {
        val nametagsToRender = nametagsToRender ?: return
        nametagsToRender.forEach { it.calculatePosition(tickDelta) }
        val filteredNameTags = nametagsToRender.filter { it.position != null }
        val nametagsCount = filteredNameTags.size.toFloat()

        filteredNameTags.forEachIndexed { index, nametagInfo ->
            val pos = nametagInfo.position!!

            // We want nametags that are closer to the player to be rendered above nametags that are further away.
            val renderZ = index / nametagsCount * 1000.0F

            nametagRenderer.drawNametag(this, nametagInfo, Vec3(pos.x, pos.y, renderZ))
        }
    }

    /**
     * Collects all entities that should be rendered, gets the screen position, where the name tag should be displayed,
     * add what should be rendered ([Nametag]). The nametags are sorted in order of rendering.
     */
    private fun collectAndSortNametagsToRender(): List<Nametag> {
        val nametagsToRender = mutableListOf<Nametag>()

        val maximumDistanceSquared = maximumDistance.sq()

        for (entity in ModuleESP.findRenderedEntities()) {
            if (entity.squaredDistanceTo(mc.cameraEntity) > maximumDistanceSquared) {
                continue
            }

            nametagsToRender += Nametag(entity)
        }

        nametagsToRender.sortByDescending { abs(it.entity.z - player.pos.z) }

        return nametagsToRender
    }

    /**
     * Should [ModuleNametags] render nametags above this [entity]?
     */
    @JvmStatic
    fun shouldRenderNametag(entity: Entity) = entity.shouldBeShown()

}
