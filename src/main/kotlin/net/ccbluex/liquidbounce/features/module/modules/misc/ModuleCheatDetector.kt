package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.cheatdetect.CheatDetect
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.combat.ReachCheck
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.motion.flying.FlyingDetector

object ModuleCheatDetector : ClientModule("CheatDetector", Category.MISC, notActivatable = true) {

    val shouldLogger by boolean("Logger", true).onChanged { CheatDetect.shouldLogger = it }

    val maxKeepEntity by int("MaxKeepEntity", 100, 1..100)


    init {
        tree(FlyingDetector)
        tree(ReachCheck)
        CheatDetect.shouldLogger = shouldLogger
    }
}
