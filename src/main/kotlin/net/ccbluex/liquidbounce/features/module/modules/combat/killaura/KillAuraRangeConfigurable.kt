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
import net.ccbluex.liquidbounce.utils.client.Chronometer
import net.ccbluex.liquidbounce.utils.kotlin.random
import kotlin.math.max
import kotlin.math.min

object KillAuraRangeConfigurable : Configurable("Range") {
    private val mode = choices(
        ModuleKillAura, "Mode", RangeChoice.Static, arrayOf(
            RangeChoice.Static, RangeChoice.Random,
            RangeChoice.Dynamic
        )
    )

    private sealed class RangeChoice(name: String) : Choice(name) {
        override val parent: ChoiceConfigurable<*>
            get() = mode

        abstract val range: Float

        abstract val wallRange: Float

        private val scanExtraRange by floatRange("ScanExtraRange", 2.0f..3.0f, 0.0f..7.0f).onChanged {
            currentScanExtraRange = it.random()
        }
        var currentScanExtraRange: Float = scanExtraRange.random()
            private set

        object Static : RangeChoice("Static") {
            override val range by float("Range", 4.2f, 0f..8f)
            override val wallRange by float("WallRange", 3f, 0f..8f).onChange {
                min(range, it)
            }
        }

        object Random : RangeChoice("Random") {
            private val realRange by floatRange("Range", 3f..4.2f, 0f..8f)
            override val range get() = realRange.random()
            override val wallRange by float("WallRange", 3f, 0f..8f).onChange {
                min(realRange.endInclusive, it)
            }
        }

        object Dynamic : RangeChoice("Dynamic") {
            private val normalRange by float("NormalRange", 3f, 0f..8f)
            private val maxRange by float("MaxRange", 4.2f, 0f..8f).onChange {
                max(normalRange, it)
            }
            override val wallRange by float("Wall Range", 3f, 0f..8f).onChange {
                min(normalRange, it)
            }

            private val maxRangeTimes by int("MaxRangeTimes", 0, 0..100)
            private var maxRangeTimesCounter = 0
            private val maxRangeChance by int("MaxRangeChance", 100, 0..100, "%")
            private val resetTime by int("ResetTime", 10, 0..50, "s")
            private val timer = Chronometer()

            override val range: Float get() {
                if (timer.hasElapsed(resetTime * 1000L)) {
                    maxRangeTimesCounter = 0
                    timer.reset()
                }

                if (maxRangeTimesCounter > maxRangeTimes || kotlin.random.Random.nextInt(0, 100) > maxRangeChance) {
                    return normalRange
                }

                maxRangeTimesCounter++
                timer.reset()
                return maxRange
            }
        }
    }

    fun getRange(): Float = mode.activeChoice.range

    fun getWallRange(): Float = mode.activeChoice.wallRange

    fun getCurrentScanExtraRange(): Float = mode.activeChoice.currentScanExtraRange
}
