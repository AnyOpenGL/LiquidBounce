package net.ccbluex.liquidbounce.utils.cheatdetect.utils

import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.DetectorCategory

data class PlayerFlagRecorder(
    val playerId: Int,
    val flagType: DetectorCategory,
    val isFlagged: Boolean
)
