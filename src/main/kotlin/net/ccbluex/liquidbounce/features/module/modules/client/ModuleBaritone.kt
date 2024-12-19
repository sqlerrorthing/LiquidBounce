package net.ccbluex.liquidbounce.features.module.modules.client

import baritone.api.BaritoneAPI
import baritone.api.Settings
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.config.types.Value
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.injection.mixins.baritone.MixinSetCommand
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

    /**
     * This `init` block initializes settings and configurations related to Baritone.
     * The check `BaritoneUtil.isAvailable` is **mandatory** to prevent potential errors
     * if Baritone is not present (e.g class baritone.api.BaritoneAPI not found error (CNDF)).
     *
     * Without this check, the code within this block will still
     * execute, potentially causing `(C)lass (N)ot (D)ef (F)ound` or other issues due to
     * the absence of the Baritone API.
     *
     * If `BaritoneUtil.isAvailable` evaluates to `false`, this entire block will be skipped,
     * preventing attempts to interact with the Baritone API when it is not loaded.
     */
    init {
        if (BaritoneUtil.isAvailable) {
            with(BaritoneAPI.getSettings()) {
                createSetting("Place", allowPlace)
                createToggleableConfigurableSetting(Mining, allowBreak)
            }

            treeAll(
                Rotations,
                Movement,
                Assumptions,
                Penalties,
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
                        createSetting("ToPlace", colorBlocksToPlace)
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

    private object Mining : ToggleableConfigurable(this, "Mining", false /* no matter. */) {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createSetting("MinYLevel", minYLevelWhileMining, maxRangedValue = 2031)
                    createSetting("MaxYLevel", maxYLevelWhileMining, maxRangedValue = 2031)
                    createSetting("GoalUpdateInterval", mineGoalUpdateInterval, maxRangedValue = 20)
                    createSetting("MaxOreLocationsCount", mineMaxOreLocationsCount, maxRangedValue = 64)
                    createSetting("PauseForFallingBlocks", pauseMiningForFallingBlocks)
                    createSetting("ForceInternal", forceInternalMining)
                    createSetting("OnlyExposedOres", allowOnlyExposedOres)
                    createSetting("UseSword", useSwordToMine)
                    createSetting("NotificationOnFail", notificationOnMineFail)
                    createToggleableConfigurableSetting("LegitMine", legitMine) {
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
                    createToggleableConfigurableSetting("Parkour", allowParkour) {
                        createSetting("Place", allowParkourPlace)
                    }
                }

                treeAll(
                    Ascends,
                    Descends
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
                        createToggleableConfigurableSetting("Diagonal", allowDiagonalDescend) {
                            createSetting("Overshoot", allowOvershootDiagonalDescend)
                        }
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
                        createSetting("PlaceIncorrectBlockMultiplier", placeIncorrectBlockPenaltyMultiplier)
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
                    createSetting("AutoJump", elytraAutoJump)
                    createSetting("AutoSwap", elytraAutoSwap)
                }

                treeAll(
                    Firework,
                    Render
                )
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
                        createToggleableConfigurableSetting("Raytraces", elytraRenderRaytraces) {
                            createSetting("Hitbox", elytraRenderHitboxRaytraces)
                        }
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

/**
 * A mutable map for controlling Baritone settings through client-side wrapper ([ModuleBaritone]) settings.
 *
 * The map key represents a Baritone setting ([Settings.Setting]),
 * and the value is a lambda function that serves as a setter for updating the setting's value.
 *
 * The lambda accepts the current setting value of type [Any], corresponding to
 * the generic type T of the original [Settings.Setting.value].
 *
 * Used for dynamic management and synchronization of Baritone settings
 * through intermediate client-side settings.
 *
 * @see Settings.Setting
 * @see MixinSetCommand
 */
private val controlledBaritoneSettingsMutableMap = mutableMapOf<Settings.Setting<*>, (Any) -> Unit>()

/**
 * Provides read-only access to the controlled Baritone settings map.
 *
 * This getter exposes the internal [controlledBaritoneSettingsMutableMap]
 * as an immutable [Map], preventing direct modifications from outside the class.
 *
 * Contains Baritone settings mapped to their respective setter lambdas,
 * where each lambda can update the corresponding setting's value.
 *
 * @return An immutable map of Baritone settings and their setter functions
 */
val controlledBaritoneSettings: Map<Settings.Setting<*>, (Any) -> Unit> get() = controlledBaritoneSettingsMutableMap

private fun Configurable.createToggleableConfigurableSetting(
    reference: ToggleableConfigurable,
    setting: Settings.Setting<Boolean>,
    addToTree: Boolean = true
) {
    reference.enabled = setting.value
    reference.onChangedListener = {
        setting.value = it
    }

    if (addToTree) {
        this.tree(reference)
    }

    controlledBaritoneSettingsMutableMap[setting] = {
        reference.enabled = it as Boolean
    }
}

private inline fun Configurable.createToggleableConfigurableSetting(
    name: String,
    setting: Settings.Setting<Boolean>,
    crossinline block: ToggleableConfigurable.() -> Unit
) {
    val reference = object : ToggleableConfigurable(ModuleBaritone, name, setting.value) {
        init {
            block(this)
        }
    }

    this.createToggleableConfigurableSetting(reference, setting)
}

private inline fun <reified T> Configurable.createSetting(
    name: String,
    setting: Settings.Setting<T>,
    minRangedValue: Int = 0,
    maxRangedValue: Int = 10
): Value<*> = when (val value = setting.value) {
    is Boolean -> boolean(name, value)
        .onChanged { setting.value = it as T }
        .apply { controlledBaritoneSettingsMutableMap[setting] = {
            set(it as Boolean)
        } }

    is Double -> float(name, value.toFloat(), minRangedValue.toFloat()..maxRangedValue.toFloat())
        .onChanged { setting.value = it.toDouble() as T }
        .apply { controlledBaritoneSettingsMutableMap[setting] = {
            set((it as Double).toFloat())
        } }

    is Float -> float(name, value, minRangedValue.toFloat()..maxRangedValue.toFloat())
        .onChanged { setting.value = it as T }
        .apply { controlledBaritoneSettingsMutableMap[setting] = {
            set(it as Float)
        } }

    is Int -> int(name, value, 0..maxRangedValue)
        .onChanged { setting.value = it as T }
        .apply { controlledBaritoneSettingsMutableMap[setting] = {
            set(it as Int)
        } }

    is Long -> int(name, value.toInt(), minRangedValue..maxRangedValue)
        .onChanged { setting.value = it.toLong() as T }
        .apply { controlledBaritoneSettingsMutableMap[setting] = {
            set((it as Long).toInt())
        } }

    is String -> text(name, value)
        .onChanged { setting.value = it as T }
        .apply { controlledBaritoneSettingsMutableMap[setting] = {
            set(it as String)
        } }

    is Color -> color(name, value.toColor4b())
        .onChanged { setting.value = it.toColor() as T }
        .apply { controlledBaritoneSettingsMutableMap[setting] = {
            set((it as Color).toColor4b())
        } }

    else -> throw NotImplementedError("Undefined baritone setting class! ${T::class.java.name}")
}
