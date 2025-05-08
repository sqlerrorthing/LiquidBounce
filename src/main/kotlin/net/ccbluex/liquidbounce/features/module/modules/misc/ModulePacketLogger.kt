/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
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
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.TransferOrigin
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.client.MessageMetadata
import net.ccbluex.liquidbounce.utils.client.asText
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.collection.Filter
import net.ccbluex.liquidbounce.utils.kotlin.EventPriorityConvention.READ_FINAL_STATE
import net.ccbluex.liquidbounce.utils.mappings.EnvironmentRemapper
import net.minecraft.network.packet.Packet
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.max

private typealias PacketClass = Class<out Packet<*>>

/**
 * Module PacketLogger
 *
 * Prints all packets and their fields.
 *
 * @author ccetl, sqlerrorthing
 */
object ModulePacketLogger : ClientModule("PacketLogger", Category.MISC) {
    init {
        doNotIncludeAlways()

        tree(PacketBound("Client", TransferOrigin.INCOMING, setOf(), false))
        tree(PacketBound("Server", TransferOrigin.OUTGOING, setOf(), true))
    }
}

private class PacketBound(
    name: String,
    private val origin: TransferOrigin,
    packets: Set<PacketClass>,
    enabled: Boolean
) : ToggleableConfigurable(ModulePacketLogger, name, enabled) {
    private val classNames = packets.associateWith { it.getPacketName() }
    private val fieldNames = ConcurrentHashMap<Field, String>()

    private val selectedPackets by multiStringChoice("Packets", choices = classNames.values.sorted().toSet())
    private val filter by enumChoice("Filter", Filter.BLACKLIST)

    @Suppress("unused")
    private val packetHandler = handler<PacketEvent>(priority = READ_FINAL_STATE) { event ->
        if (event.origin != origin) {
            return@handler
        }

        val name = classNames[event.packet::class.java] ?: return@handler
        if (!filter(name, selectedPackets)) {
            return@handler
        }

        buildLog(event.packet, name, event.isCancelled).also { log ->
            chat(log, metadata = MessageMetadata(prefix = false))
        }
    }

    private fun buildLog(packet: Packet<*>, packetName: String, cancelled: Boolean): MutableText {
        return Text.empty().formatted(Formatting.WHITE).apply {
            append(ModulePacketLogger.message(if (origin == TransferOrigin.INCOMING) "receive" else "send"))
            append(" $packetName")

            if (cancelled) {
                append(" (".asText().formatted(Formatting.RED))
                append(ModulePacketLogger.message("cancelled").formatted(Formatting.RED))
                append(")".asText().formatted(Formatting.RED))
            }

            appendFields(packet::class.java, packet)
        }
    }

    private fun MutableText.appendFields(clazz: PacketClass, packet: Packet<*>) {
        var start = true

        var currentClass: Class<*>? = clazz

        while (currentClass.isNotRoot()) {
            currentClass.declaredFields.forEach { field ->
                if (Modifier.isStatic(field.modifiers)) {
                    return@forEach
                }

                field.isAccessible = true

                if (start) {
                    append(":")
                    start = false
                }

                append("\n")

                val name = field.remappedFieldName(currentClass.name)
                append("-$name: ".asText().formatted(Formatting.GRAY))
                append(field.getValue(packet).asText().formatted(Formatting.GRAY))
            }

            currentClass = currentClass.superclass
        }
    }

    private fun Field.remappedFieldName(className: String): String {
        return fieldNames.computeIfAbsent(this) {
            EnvironmentRemapper.remapField(className, this.name)
        }
    }

    private fun Field.getValue(instance: Any): String {
        return runCatching {
            get(instance)?.toString()
        }.getOrDefault("null") ?: "null"
    }
}

private fun PacketClass.getPacketName(): String {
    val classNames = ArrayDeque<CharSequence>()
    classNames.add(this.getClassName())

    var superclass: Class<*>? = superclass

    while (superclass.isNotRoot()) {
        classNames.addFirst(superclass.getClassName())
        superclass = superclass.superclass
    }

    return classNames.joinToString(".")
}

private fun Class<*>.getClassName(): CharSequence {
    val remapClassName = EnvironmentRemapper.remapClass(this)
    val lastDotIndex = remapClassName.lastIndexOf('.')
    val lastDollarIndex = remapClassName.lastIndexOf('$')
    return remapClassName.subSequence(max(lastDotIndex, lastDollarIndex) + 1, remapClassName.length)
}

@OptIn(ExperimentalContracts::class)
private fun Class<*>?.isNotRoot(): Boolean {
    contract {
        returns(true) implies (this@isNotRoot != null)
    }
    return !(this == null || this === Record::class.java || this.superclass == null)
}
