package net.ccbluex.liquidbounce.utils.cheatdetect.detectors.motion.flying

import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.DetectorAlgorithm
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.DetectorCategory

object FlyingDetector : Detector("Flying", DetectorCategory.Motion, true) {

    override var algorithmList: List<DetectorAlgorithm> = listOf(
        GroundCheck
    )


    init {
        register()
    }


}

open class FlyingDetectorAlgorithm(override val name: String, enabled: Boolean) :
    DetectorAlgorithm(parent = FlyingDetector.parentModule, name = name, enabled = enabled) {

}
