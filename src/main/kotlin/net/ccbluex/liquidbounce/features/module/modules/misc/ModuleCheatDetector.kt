package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.cheatdetect.CheatDetect
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.DetectorCategory
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.motion.flying.FlyingDetector
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.motion.flying.GroundCheck

object ModuleCheatDetector : ClientModule("CheatDetector", Category.MISC, notActivatable = true) {

    val shouldLogger by boolean("Logger", true).onChanged { CheatDetect.shouldLogger = it }

   private object flying : Detector("Flying", DetectorCategory.Motion,true) {

    }




    init {
       tree(FlyingDetector)
    }



}
