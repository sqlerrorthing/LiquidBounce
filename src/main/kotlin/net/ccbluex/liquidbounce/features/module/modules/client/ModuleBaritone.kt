package net.ccbluex.liquidbounce.features.module.modules.client

import baritone.api.BaritoneAPI
import baritone.api.Settings
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.config.types.Value
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.render.engine.toColor
import net.ccbluex.liquidbounce.render.engine.toColor4b
import net.ccbluex.liquidbounce.utils.client.baritone.BaritoneUtil
import java.awt.Color

/**
 * Baritone settings in GUI
 *
 * @author sqlerrorthing
 */
object ModuleBaritone : ClientModule("Baritone", Category.CLIENT, disableActivation = true, hide = true) {
    init {
        if (BaritoneUtil.isAvailable) {
            with(BaritoneAPI.getSettings()) {
                createSetting("Break", allowBreak)
                createSetting("Place", allowPlace)
            }

            treeAll(
                Movement,
                Penalties,
                Mining,
                Items,
                Elytra
            )
        }
    }

    private object Mining : Configurable("Mining") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("MinMineYLevel", minYLevelWhileMining, maxRangedValue = 2031)
                    createSetting("MaxMineYLevel", maxYLevelWhileMining, maxRangedValue = 2031)
                    createSetting("MineGoalUpdateInterval", mineGoalUpdateInterval, maxRangedValue = 20)
                    createSetting("MineMaxOreLocationsCount", mineGoalUpdateInterval, maxRangedValue = 64)
                    createSetting("PauseMiningForFallingBlocks", pauseMiningForFallingBlocks)
                    createSetting("ForceInternalMining", forceInternalMining)
                    createSetting("OnlyExposedOres", allowOnlyExposedOres)
                    createSetting("UseSword", useSwordToMine)
                    createSetting("NotificationOnMineFail", notificationOnMineFail)
                }

                treeAll(
                    LegitMine
                )
            }
        }

        private object LegitMine : Configurable("LegitMine") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Allow", legitMine)
                        createSetting("IncludeDiagonals", legitMineIncludeDiagonals)
                        createSetting("YLevel", legitMineYLevel, -64, 320)
                    }
                }
            }
        }
    }

    private object Movement : Configurable("Movement") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("Sprint", allowSprint)
                    createSetting("JumpAt256", allowJumpAt256)
                }

                treeAll(
                    Parkour
                )
            }
        }

        private object Parkour : Configurable("Parkour") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Allow", allowParkour)
                        createSetting("Place", allowParkourPlace)
                        createSetting("Ascend", allowParkourAscend)
                    }
                }
            }
        }
    }

    private object Penalties : Configurable("Penalties") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("BlockPlacement", blockPlacementPenalty, maxRangedValue = 300)
                    createSetting("BlockBreakAdditionalPenalty", blockBreakAdditionalPenalty)
                    createSetting("BreakCorrectBlockPenaltyMultiplier",
                        breakCorrectBlockPenaltyMultiplier, maxRangedValue = 20)
                    createSetting("PlaceIncorrectBlockPenaltyMultiplier", breakCorrectBlockPenaltyMultiplier)
                    createSetting("WalkOnWaterOnePenalty", walkOnWaterOnePenalty)
                    createSetting("JumpPenalty", jumpPenalty)
                }
            }
        }
    }

    private object Elytra : Configurable("Elytra") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("SimulationTicks", elytraSimulationTicks, 1, 100)
                    createSetting("PitchRange", elytraPitchRange, 1, 100)
                    createSetting("MinimumAvoidance", elytraMinimumAvoidance, -10)
                    createSetting("PredictTerrain", elytraPredictTerrain)
                    createSetting("EmergencyLand", elytraAllowEmergencyLand)
                    createSetting("LandOnNetherFortress", elytraAllowLandOnNetherFortress)
                }

                treeAll(
                    Auto,
                    Firework,
                    Render
                )
            }
        }

        private object Auto : Configurable("Auto") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("AutoJump", elytraAutoJump)
                        createSetting("Swap", elytraAutoSwap)
                    }
                }
            }
        }

        private object Firework : Configurable("Firework") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Speed", elytraFireworkSpeed)
                        createSetting("SetbackUseDelay", elytraFireworkSetbackUseDelay)
                        createSetting("Conserve", elytraConserveFireworks)
                    }
                }
            }
        }

        private object Render : Configurable("Render") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Raytraces", elytraRenderRaytraces)
                        createSetting("HitboxRaytraces", elytraRenderHitboxRaytraces)
                        createSetting("Simulation", elytraRenderSimulation)
                    }
                }
            }
        }
    }

    private object Items : Configurable("Items") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("Inventory", allowInventory)
                    createSetting("AutoTool", autoTool)
                }
            }
        }
    }
}

private inline fun <reified T> Configurable.createSetting(
    name: String,
    setting: Settings.Setting<T>,
    minRangedValue: Int = 0,
    maxRangedValue: Int = 10
): Value<*> = when (val value = setting.value) {
    is Boolean -> boolean(name, value)
        .onChanged { setting.value = it as T }

    is Double -> float(name, value.toFloat(), minRangedValue.toFloat()..maxRangedValue.toFloat())
        .onChanged { setting.value = it.toDouble() as T }

    is Float -> float(name, value, minRangedValue.toFloat()..maxRangedValue.toFloat())
        .onChanged { setting.value = it as T }

    is Int -> int(name, value, 0..maxRangedValue)
        .onChanged { setting.value = it as T }

    is Long -> int(name, value.toInt(), minRangedValue..maxRangedValue)
        .onChanged { setting.value = it.toLong() as T }

    is String -> text(name, value)
        .onChanged { setting.value = it as T }

    is Color -> color(name, value.toColor4b())
        .onChanged { setting.value = it.toColor() as T }

    else -> throw NotImplementedError("Undefined baritone setting class!")
}
