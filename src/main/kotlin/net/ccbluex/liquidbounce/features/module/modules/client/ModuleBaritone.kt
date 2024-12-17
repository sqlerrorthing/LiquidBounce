package net.ccbluex.liquidbounce.features.module.modules.client

import baritone.api.BaritoneAPI
import baritone.api.Settings
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.config.types.Value
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.render.engine.toColor
import net.ccbluex.liquidbounce.render.engine.toColor4b
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
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
                Rotations,
                Movement,
                Assumptions,
                Penalties,
                Mining,
                Items,
                Elytra,
                Waypoints,
                Colors
            )
        }
    }

    object Rotations : RotationsConfigurable(this)

    private object Colors : Configurable("Colors") {
        init {
            if (BaritoneUtil.isAvailable) {
                treeAll(
                    Path,
                    Blocks,
                    Goal,
                    Selection
                )
            }
        }

        private object Path : Configurable("Path") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Current", colorCurrentPath)
                        createSetting("Next", colorNextPath)
                        createSetting("BestPathSoFar", colorBestPathSoFar)
                        createSetting("MostRecentConsidered", colorMostRecentConsidered)
                    }
                }
            }
        }

        private object Blocks : Configurable("Blocks") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("ToBreak", colorBlocksToBreak)
                        createSetting("Next", colorBlocksToPlace)
                        createSetting("ToWalkInto", colorBlocksToWalkInto)
                    }
                }
            }
        }

        private object Goal : Configurable("Goal") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Color", colorGoalBox)
                        createSetting("Inverted", colorInvertedGoalBox)
                    }
                }
            }
        }

        private object Selection : Configurable("Selection") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Color", colorSelection)
                        createSetting("PosFirst", colorSelectionPos1)
                        createSetting("PosSecond", colorSelectionPos2)
                    }
                }
            }
        }
    }

    private object Waypoints : Configurable("Waypoints") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("Bed", doBedWaypoints)
                    createSetting("Death", doDeathWaypoints)
                }
            }
        }
    }

    private object Assumptions : Configurable("Assumptions") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("Step", assumeStep)
                }

                treeAll(
                    Walk
                )
            }
        }

        private object Walk : Configurable("Walk") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Water", assumeWalkOnWater)
                        createSetting("Lava", assumeWalkOnLava)
                        createSetting("Safe", assumeSafeWalk)
                    }
                }
            }
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
                    Legit
                )
            }
        }

        private object Legit : Configurable("Legit") {
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
                    Ascends,
                    Descends,
                    Parkour
                )
            }
        }

        private object Ascends : Configurable("Ascends") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Diagonal", allowDiagonalAscend)
                        createSetting("WithSprint", sprintAscends)
                        createSetting("Parkour", allowParkourAscend)
                    }
                }
            }
        }

        private object Descends : Configurable("Descends") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Diagonal", allowDiagonalDescend)
                        createSetting("OvershootDiagonal", allowOvershootDiagonalDescend)
                    }
                }
            }
        }

        private object Parkour : Configurable("Parkour") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Allow", allowParkour)
                        createSetting("Place", allowParkourPlace)
                    }
                }
            }
        }
    }

    private object Penalties : Configurable("Penalties") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("WalkOnWater", walkOnWaterOnePenalty)
                    createSetting("Jump", jumpPenalty)
                }

                treeAll(
                    Blocks
                )
            }
        }

        private object Blocks : Configurable("Blocks") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createSetting("Placement", blockPlacementPenalty, maxRangedValue = 300)
                        createSetting("BreakAdditional", blockBreakAdditionalPenalty)
                        createSetting("BreakCorrectBlockMultiplier",
                            breakCorrectBlockPenaltyMultiplier, maxRangedValue = 20)
                        createSetting("PlaceIncorrectBlockMultiplier", breakCorrectBlockPenaltyMultiplier)
                    }
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
                        createSetting("Jump", elytraAutoJump)
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
