package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable

abstract class Detector(
    name: String,
    enabled: Boolean,
) : ToggleableConfigurable(ModuleCheatDetector, name, enabled) {
    abstract fun detect(entityRecorder: EntityRecorder)
}
