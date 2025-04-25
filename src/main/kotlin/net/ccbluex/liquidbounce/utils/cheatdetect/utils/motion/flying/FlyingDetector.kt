package net.ccbluex.liquidbounce.utils.cheatdetect.utils.motion.flying

import net.ccbluex.liquidbounce.utils.cheatdetect.utils.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.DetectorAlgorithm
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.DetectorCategory


val GLOBAL_EARLY_OBJECT = FlyingDetector
object FlyingDetector : Detector("Flying", DetectorCategory.Motion,true) {

    override var algorithmList: List<DetectorAlgorithm> = listOf(
        GroundCheck
    )
}

open class FlyingDetectorAlgorithm(override val name: String, enabled: Boolean) :
    DetectorAlgorithm(parent = FlyingDetector.parentModule, name = name, enabled = enabled) {

    }
