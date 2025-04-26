package net.ccbluex.liquidbounce.utils.cheatdetect.detectors

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleCheatDetector
import net.ccbluex.liquidbounce.utils.cheatdetect.PlayerDataRecorder
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.PlayerFlagRecorder

abstract class Detector(val detectorName: String, val category: DetectorCategory, val isEnable: Boolean) :
    ToggleableConfigurable(parent = ModuleCheatDetector, name = detectorName, enabled = isEnable) {

    open var algorithmList: List<DetectorAlgorithm> = listOf()

    val parentModule = ModuleCheatDetector


    fun register() {


        for (i in algorithmList) {
            tree(i)
        }

    }

    val minFlags by int("MinFlags", 1, 1..10)
    val flagsInterval by int("FlagsInterval", 10, 1..100)

    val triggeredFlagCondition by int("TriggeredFlagCondition", 1, 1..10)


    open fun detect(entityId: Int, playerDataRecorder: PlayerDataRecorder): Boolean {
        var flags = 0
        for (i in algorithmList) {
            if (i.enabled) {
                if (i.detect(entityId, playerDataRecorder)) {
                    flags++
                }
            }
        }

        return flags >= triggeredFlagCondition
    }

    fun update(entityId: Int, playerDataRecorder: PlayerDataRecorder): PlayerFlagRecorder {
        val isFlagged = detect(entityId, playerDataRecorder)

        return PlayerFlagRecorder(entityId, category, isFlagged)
    }
}

