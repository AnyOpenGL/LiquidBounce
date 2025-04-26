package net.ccbluex.liquidbounce.utils.cheatdetect.detectors

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.utils.cheatdetect.PlayerDataRecorder

open class DetectorAlgorithm(parent: EventListener, override val name: String, enabled: Boolean) :
    ToggleableConfigurable(parent = parent, name = name, enabled = enabled) {


    open fun detect(entityId: Int, playerDataRecorder: PlayerDataRecorder): Boolean {
        return false
    }
}
