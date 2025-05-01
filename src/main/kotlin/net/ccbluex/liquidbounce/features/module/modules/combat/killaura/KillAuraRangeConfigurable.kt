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
package net.ccbluex.liquidbounce.features.module.modules.combat.killaura

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Dynamic.maxRange
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Dynamic.maxRangeChance
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Dynamic.maxRangeTimesCounter
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Dynamic.normalRange
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Dynamic.resetTime
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Dynamic.wallRange
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Random.range
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Random.realRange
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Random.wallRange
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Static.range
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.KillAuraRangeConfigurable.RangeChoice.Static.wallRange
import net.ccbluex.liquidbounce.utils.client.Chronometer
import net.ccbluex.liquidbounce.utils.entity.airTicks
import net.ccbluex.liquidbounce.utils.kotlin.random
import kotlin.math.max
import kotlin.math.min

/**
 * Configuration manager for KillAura attack range calculation modes.
 *
 * Provides three range calculation strategies:
 * - Static: Fixed attack range
 * - Random: Randomized attack range
 * - Dynamic: Adaptive range based on usage patterns
 */
object KillAuraRangeConfigurable : Configurable("Range") {
    /**
     * Mode selector configuration for range calculation strategies
     *
     * @param parent The parent KillAura module
     * @param name Configuration name
     * @param default Default selected mode
     * @param choices Available range calculation modes
     */
    private val modes =
        choices(
            ModuleKillAura,
            "Modes",
            RangeChoice.Static,
            arrayOf(
                RangeChoice.Static,
                RangeChoice.Random,
                RangeChoice.Dynamic,
            ),
        )

    /**
     * Base sealed class for range calculation strategies
     *
     * @property range Current effective attack range
     * @property wallRange Attack range through obstacles
     * @property currentScanExtraRange Randomized extra scanning range
     */
    private sealed class RangeChoice(
        name: String,
    ) : Choice(name) {
        override val parent: ChoiceConfigurable<*>
            get() = modes

        abstract val range: Float
        abstract val wallRange: Float

        /**
         * Configurable range extension for entity scanning
         *
         * @property scanExtraRange Range bounds for extra scanning (default: 2.0-3.0 blocks)
         * @property currentScanExtraRange Randomized value from scanExtraRange bounds
         */
        private val scanExtraRange by floatRange("ScanExtraRange", 2.0f..3.0f, 0.0f..7.0f).onChanged {
            currentScanExtraRange = it.random()
        }
        var currentScanExtraRange: Float = scanExtraRange.random()
            private set

        /**
         * Static range calculation strategy
         *
         * @property range Fixed attack range (default: 4.2 blocks)
         * @property wallRange Obstacle penetration range (capped at normal range)
         */
        object Static : RangeChoice("Static") {
            override val range by float("Range", 4.2f, 0f..8f)
            override val wallRange by float("WallRange", 3f, 0f..8f).onChange {
                min(range, it)
            }
        }

        /**
         * Randomized range calculation strategy
         *
         * @property realRange Range randomization bounds (default: 3.0-4.2 blocks)
         * @property range Random value from realRange bounds
         * @property wallRange Obstacle penetration range (capped at max random range)
         */
        object Random : RangeChoice("Random") {
            private val realRange by floatRange("Range", 3f..4.2f, 0f..8f)
            override val range get() = realRange.random()
            override val wallRange by float("WallRange", 3f, 0f..8f).onChange {
                min(realRange.endInclusive, it)
            }
        }

        /**
         * Adaptive range calculation strategy
         *
         * @property normalRange Minimum attack range (default: 3.0 blocks)
         * @property maxRange Extended attack range when conditions met (default: 4.2 blocks)
         * @property wallRange Obstacle penetration range (capped at normal range)
         *
         * Uses probabilistic system with usage counters and timers to reduce detection risk:
         * - Tracks max range usage with [maxRangeTimesCounter]
         * - Resets counter after [resetTime] seconds
         * - Applies [maxRangeChance] probability check
         */
        object Dynamic : RangeChoice("Dynamic") {
            private val normalRange by float("NormalRange", 3f, 0f..8f)
            private val maxRange by float("MaxRange", 4.2f, 0f..8f).onChange {
                max(normalRange, it)
            }
            override val wallRange by float("Wall Range", 3f, 0f..8f).onChange {
                min(normalRange, it)
            }

            private val maxRangeTimes by intRange("MaxRangeTimes", 0..20, 0..100)
            private var currentMaxRangeTimes = 0
            private var maxRangeTimesCounter = 0
            private val maxRangeChance by int("MaxRangeChance", 100, 0..100, "%")
            private val maxRangeCooldown by intRange("MaxRangeCooldown", 100..500, 0..500, "ms")
            private val resetTime by int("ResetTime", 10, 0..50, "s")

            private object EscapeCombo : ToggleableConfigurable(this, "EscapeCombo", true) {
                val hurtTime by intRange("HurntTime", 5..10, 0..50, "times")
                val airTicks by intRange("AirTicks", 10..25, 0..30, "ticks")
                var currentHurtTime = 0
                var currentAirTicks = 0

                fun reset() {
                    currentHurtTime = hurtTime.random()
                    currentAirTicks = airTicks.random()
                }
            }

            private val timer = Chronometer()

            /**
             * Dynamic range calculation logic
             * @return normalRange when conditions not met, otherwise maxRange
             *
             * Conditions for maxRange:
             * 1. Usage counter below maxRangeTimes threshold
             * 2. Random check passes maxRangeChance probability
             * 3. Timer hasn't exceeded resetTime interval
             */
            override val range: Float
                get() {
                    if (timer.hasElapsed(resetTime * 1000L)) {
                        resetMaxRange()
                    }
                    if (ModuleKillAura.targetTracker.target != null && EscapeCombo.enabled) {
                        if (ModuleKillAura.targetTracker.target?.hurtTime!! >= EscapeCombo.currentHurtTime &&
                            player.airTicks >= EscapeCombo.currentAirTicks
                        ) {
                            return maxRange
                        }
                    }

                    if (maxRangeTimesCounter > currentMaxRangeTimes ||
                        kotlin.random.Random.nextInt(0, 100) > maxRangeChance ||
                        !timer.hasElapsed(maxRangeCooldown.random().toLong())
                    ) {
                        return normalRange
                    }

                    maxRangeTimesCounter++
                    timer.reset()
                    return maxRange
                }

            fun resetMaxRange() {
                maxRangeTimesCounter = 0
                currentMaxRangeTimes = maxRangeTimes.random()
            }
        }
    }

    /**
     * Get current effective attack range
     * @return Active mode's calculated attack range
     */
    fun getRange(): Float = modes.activeChoice.range

    /**
     * Get current obstacle penetration range
     * @return Active mode's calculated wall range
     */
    fun getWallRange(): Float = modes.activeChoice.wallRange

    /**
     * Get randomized scanning extension range
     * @return Current extra scanning range value
     */
    fun getCurrentScanExtraRange(): Float = modes.activeChoice.currentScanExtraRange
}
