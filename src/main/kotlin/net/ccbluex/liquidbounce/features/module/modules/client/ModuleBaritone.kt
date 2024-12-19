package net.ccbluex.liquidbounce.features.module.modules.client

import baritone.api.BaritoneAPI
import baritone.api.Settings
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.config.types.Value
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.injection.mixins.baritone.MixinSetCommand
import net.ccbluex.liquidbounce.render.engine.Color4b
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
                createValSetting("Place", allowPlace)
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
                        createValSetting("Current", colorCurrentPath)
                        createValSetting("Next", colorNextPath)
                        createValSetting("BestPathSoFar", colorBestPathSoFar)
                        createValSetting("MostRecentConsidered", colorMostRecentConsidered)
                    }
                }
            }
        }

        private object Blocks : Configurable("Blocks") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createValSetting("ToBreak", colorBlocksToBreak)
                        createValSetting("ToPlace", colorBlocksToPlace)
                        createValSetting("ToWalkInto", colorBlocksToWalkInto)
                    }
                }
            }
        }

        private object Goal : Configurable("Goal") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createValSetting("Color", colorGoalBox)
                        createValSetting("Inverted", colorInvertedGoalBox)
                    }
                }
            }
        }

        private object Selection : Configurable("Selection") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createValSetting("Color", colorSelection)
                        createValSetting("PosFirst", colorSelectionPos1)
                        createValSetting("PosSecond", colorSelectionPos2)
                    }
                }
            }
        }
    }

    private object Waypoints : Configurable("Waypoints") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createValSetting("Bed", doBedWaypoints)
                    createValSetting("Death", doDeathWaypoints)
                }
            }
        }
    }

    private object Assumptions : Configurable("Assumptions") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createValSetting("Step", assumeStep)
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
                        createValSetting("Water", assumeWalkOnWater)
                        createValSetting("Lava", assumeWalkOnLava)
                        createValSetting("Safe", assumeSafeWalk)
                    }
                }
            }
        }
    }

    private object Mining : ToggleableConfigurable(this, "Mining", false /* no matter. */) {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createNumSetting("MinYLevel", minYLevelWhileMining, 0..2031)
                    createNumSetting("MaxYLevel", maxYLevelWhileMining, 0..2031)
                    createNumSetting("GoalUpdateInterval", mineGoalUpdateInterval, 0..20)
                    createNumSetting("MaxOreLocationsCount", mineMaxOreLocationsCount, 0..64)
                    createValSetting("PauseForFallingBlocks", pauseMiningForFallingBlocks)
                    createValSetting("ForceInternal", forceInternalMining)
                    createValSetting("OnlyExposedOres", allowOnlyExposedOres)
                    createValSetting("UseSword", useSwordToMine)
                    createValSetting("NotificationOnFail", notificationOnMineFail)
                    createToggleableConfigurableSetting("LegitMine", legitMine) {
                        createValSetting("IncludeDiagonals", legitMineIncludeDiagonals)
                        createNumSetting("YLevel", legitMineYLevel, -64..320)
                    }
                }
            }
        }
    }

    private object Movement : Configurable("Movement") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createValSetting("Sprint", allowSprint)
                    createValSetting("JumpAt256", allowJumpAt256)
                    createToggleableConfigurableSetting("Parkour", allowParkour) {
                        createValSetting("Place", allowParkourPlace)
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
                        createValSetting("Diagonal", allowDiagonalAscend)
                        createValSetting("WithSprint", sprintAscends)
                        createValSetting("Parkour", allowParkourAscend)
                    }
                }
            }
        }

        private object Descends : Configurable("Descends") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createToggleableConfigurableSetting("Diagonal", allowDiagonalDescend) {
                            createValSetting("Overshoot", allowOvershootDiagonalDescend)
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
                    createNumSetting("WalkOnWater", walkOnWaterOnePenalty, 0..10)
                    createNumSetting("Jump", jumpPenalty, 0..10)
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
                        createNumSetting("Placement", blockPlacementPenalty, 0..300)
                        createNumSetting("BreakAdditional", blockBreakAdditionalPenalty, 0..10)
                        createNumSetting("BreakCorrectBlockMultiplier",
                            breakCorrectBlockPenaltyMultiplier, 0..20)
                        createNumSetting("PlaceIncorrectBlockMultiplier", placeIncorrectBlockPenaltyMultiplier, 0..10)
                    }
                }
            }
        }
    }

    private object Elytra : Configurable("Elytra") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createNumSetting("SimulationTicks", elytraSimulationTicks, 1..100)
                    createNumSetting("PitchRange", elytraPitchRange, 1..100)
                    createNumSetting("MinimumAvoidance", elytraMinimumAvoidance, -10..0)
                    createValSetting("PredictTerrain", elytraPredictTerrain)
                    createValSetting("EmergencyLand", elytraAllowEmergencyLand)
                    createValSetting("LandOnNetherFortress", elytraAllowLandOnNetherFortress)
                    createValSetting("AutoJump", elytraAutoJump)
                    createValSetting("AutoSwap", elytraAutoSwap)
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
                        createNumSetting("Speed", elytraFireworkSpeed, 0..10)
                        createNumSetting("SetbackUseDelay", elytraFireworkSetbackUseDelay, 0..10)
                        createValSetting("Conserve", elytraConserveFireworks)
                    }
                }
            }
        }

        private object Render : Configurable("Render") {
            init {
                if (BaritoneUtil.isAvailable) {
                    with(BaritoneAPI.getSettings()) {
                        createToggleableConfigurableSetting("Raytraces", elytraRenderRaytraces) {
                            createValSetting("Hitbox", elytraRenderHitboxRaytraces)
                        }
                        createValSetting("Simulation", elytraRenderSimulation)
                    }
                }
            }
        }
    }

    private object Items : Configurable("Items") {
        init {
            if (BaritoneUtil.isAvailable) {
                with(BaritoneAPI.getSettings()) {
                    createValSetting("Inventory", allowInventory)
                    createValSetting("AutoTool", autoTool)
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

private inline fun <reified T: Number> Configurable.createNumSetting(
    name: String,
    setting: Settings.Setting<T>,
    range: IntRange
): Value<*> {
    return when (setting.value) {
        is Double -> createGenericBaritoneSetting<Float, Double>(
            { n, v -> float(n, v, range.first.toFloat()..range.last.toFloat()) },
            name, setting, Double::toFloat, Float::toDouble
        )
        is Float -> createGenericBaritoneSettingOfSharedType<Float>(
            { n, v -> float(n, v, range.first.toFloat()..range.last.toFloat()) },
            name, setting
        )
        is Long -> createGenericBaritoneSetting<Int, Long>(
            { n, v -> int(n, v, range) },
            name, setting, Long::toInt, Int::toLong,
        )
        is Int -> createGenericBaritoneSettingOfSharedType<Int>(
            { n, v -> int(n, v, range) },
            name, setting
        )
        else -> throw NotImplementedError("Unknown number value type! ${T::class.java.name}")
    }
}

private inline fun <reified T> Configurable.createValSetting(name: String, setting: Settings.Setting<T>): Value<*> {
    return when (setting.value) {
        is Boolean -> createGenericBaritoneSettingOfSharedType(this::boolean, name, setting)
        is String -> createGenericBaritoneSettingOfSharedType(this::text, name, setting)
        is Color -> createGenericBaritoneSetting(this::color, name, setting, Color::toColor4b, Color4b::toColor)
        else -> throw NotImplementedError("Unknown non-number value type! ${T::class.java.name}")
    }
}

/**
 * [createGenericBaritoneSetting] for the case when the type is shared between baritone and LiquidBounce.
 */
private inline fun <reified T: Any> createGenericBaritoneSettingOfSharedType(
    configurableGenerator: (String, T) -> Value<T>,
    name: String,
    setting: Settings.Setting<*>,
): Value<T> {
    return createGenericBaritoneSetting(configurableGenerator, name, setting, { it }, { it })
}

/**
 * Generates a preconfigured LiquidBounce-configurable for a given baritone setting
 *
 * @param L The type of the value in LiquidBounce's type system
 * @param B The type of the value in Baritone's type system
 *
 * @param toLiquidBounceConverter translates the given type from the baritone type system to the LB type system
 * @param toBaritoneConverter translates the given type from the LB type system to the baritone type system
 */
private inline fun <reified L: Any, reified B> createGenericBaritoneSetting(
    configurableGenerator: (String, L) -> Value<L>,
    name: String,
    setting: Settings.Setting<*>,
    crossinline toLiquidBounceConverter: (B) -> L,
    crossinline toBaritoneConverter: (L) -> B
): Value<L> {
    @Suppress("UNCHECKED_CAST")
    val castedSetting = setting as Settings.Setting<B>

    return configurableGenerator(name, toLiquidBounceConverter(setting.value!!))
        .onChanged { castedSetting.value = toBaritoneConverter(it) }
        .apply {
            controlledBaritoneSettingsMutableMap[setting] = {
                set(toLiquidBounceConverter(it as B))
            }
        }
}
