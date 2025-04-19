package net.ccbluex.liquidbounce.features.module.modules.combat.killaura

import net.ccbluex.liquidbounce.config.types.Choice
import net.ccbluex.liquidbounce.config.types.ChoiceConfigurable
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.utils.client.Chronometer
import net.ccbluex.liquidbounce.utils.kotlin.random
import kotlin.random.Random

object KillAuraRangeConfigurable : Configurable("Range") {
    private val mode = choices(
        ModuleKillAura, "Mode", StaticRange, arrayOf(
            StaticRange, RandomRange,
            DynamicRange
        )
    )

    init {
        tree(mode)
    }

    fun getRange(): Float = when (mode.activeChoice) {
        StaticRange -> StaticRange.range
        RandomRange -> RandomRange.range.random()
        DynamicRange -> DynamicRange.getRange()
        else -> 3f
    }

    fun getWallRange(): Float = when (mode.activeChoice) {
        StaticRange -> StaticRange.wallRange
        RandomRange -> RandomRange.wallRange
        DynamicRange -> DynamicRange.wallRange
        else -> 3f
    }

    fun getCurrentScanExtraRange(): Float = when (mode.activeChoice) {
        StaticRange -> StaticRange.currentScanExtraRange
        RandomRange -> RandomRange.currentScanExtraRange
        DynamicRange -> DynamicRange.currentScanExtraRange
        else -> Random.nextDouble(1.0, 2.0).toFloat()
    }

    object StaticRange : Choice("Static") {
        val range by float("Range", 4.2f, 0f..8f)
        val wallRange by float("WallRange", 3f, 0f..8f).onChange { wallRange ->
            if (wallRange > range) {
                range
            } else {
                wallRange
            }
        }

        private val scanExtraRange by floatRange("ScanExtraRange", 2.0f..3.0f, 0.0f..7.0f).onChanged { range ->
            currentScanExtraRange = range.random()
        }

        var currentScanExtraRange: Float = scanExtraRange.random()

        override val parent: ChoiceConfigurable<*>
            get() = mode
    }

    object RandomRange : Choice("Random") {
        val range by floatRange("Range", 3f..4.2f, 0f..8f)
        val wallRange by float("WallRange", 3f, 0f..8f).onChange { wallRange ->
            if (wallRange > range.endInclusive) {
                range.endInclusive
            } else {
                wallRange
            }
        }

        private val scanExtraRange by floatRange("ScanExtraRange", 2.0f..3.0f, 0.0f..7.0f).onChanged { range ->
            currentScanExtraRange = range.random()
        }
        var currentScanExtraRange: Float = scanExtraRange.random()

        override val parent: ChoiceConfigurable<*>
            get() = mode
    }


    object DynamicRange : Choice("Dynamic") {
        private val normalRange by float("NormalRange", 3f, 0f..8f)
        private val maxRange by float("MaxRange", 4.2f, 0f..8f).onChange { maxRange ->
            if (maxRange < normalRange) {
                normalRange
            }
            else {
                maxRange
            }
        }
        val wallRange by float("Wall Range", 3f, 0f..8f).onChange { wallRange ->
            if (wallRange > normalRange) {
                normalRange
            } else {
                wallRange
            }
        }

        private val scanExtraRange by floatRange("ScanExtraRange", 2.0f..3.0f, 0.0f..7.0f).onChanged { range ->
            currentScanExtraRange = range.random()
        }
        var currentScanExtraRange: Float = scanExtraRange.random()

        private val maxRangeTimes by int("MaxRangeTimes", 0, 0..100)
        private var maxRangeTimesCounter = 0
        private val maxRangeChance by int("MaxRangeChance", 100, 0..100, "%")
        private val resetTime by int("ResetTime", 10, 0..50, "s")
        private var timer = Chronometer()

        fun getRange(): Float {

            if (timer.hasElapsed(resetTime * 1000L)) {
                maxRangeTimesCounter = 0
                timer.reset()
            }

            if (maxRangeTimesCounter > maxRangeTimes || Random.nextInt(0, 100) > maxRangeChance) {
                return normalRange
            }

            maxRangeTimesCounter++
            timer.reset()
            return maxRange

        }

        override val parent: ChoiceConfigurable<Choice>
            get() = mode
    }
}
