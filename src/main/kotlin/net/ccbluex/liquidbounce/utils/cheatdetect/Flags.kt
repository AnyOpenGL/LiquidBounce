package net.ccbluex.liquidbounce.utils.cheatdetect

import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.Detector

class Flags(
    val detector: Detector
) {
    var flags: Int = 0
}
