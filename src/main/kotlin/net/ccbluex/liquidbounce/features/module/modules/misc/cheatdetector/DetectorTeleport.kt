package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.utils.cheatdetector.PlayerEntityStatus

object DetectorTeleport : Detector("Teleport", true), DetectMovement {
    private val minTeleportDistance by float("MinTeleportDistance", 1f, 0f..10f)

    var currentTickPlayerEntity: PlayerEntityStatus? = null
    var lastTickPlayerEntity: PlayerEntityStatus? = null

    override fun detectMovement(playerStatusRecorder: PlayerStatusRecorder) {
        if (playerStatusRecorder.entityList.size > 1) {
            lastTickPlayerEntity = playerStatusRecorder.entityList.getOrNull(playerStatusRecorder.entityList.size - 2) ?: return

            currentTickPlayerEntity = playerStatusRecorder.entityList.last()

            if (lastTickPlayerEntity!!.pos.distanceTo(currentTickPlayerEntity!!.pos) > minTeleportDistance) {
                playerStatusRecorder.flagsList[FlagTypes.TELEPORT]!!.flagsCounter =
                    playerStatusRecorder.flagsList[FlagTypes.TELEPORT]!!.flagsCounter.plus(1)
                playerStatusRecorder.flagsList[FlagTypes.TELEPORT]!!.isReported = false
                if (playerStatusRecorder.entityList.size > 1) {
                    playerStatusRecorder.entityList.clear()
                }
                return
            }
        }
    }
}
