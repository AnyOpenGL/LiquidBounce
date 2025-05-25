package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import kotlin.collections.set

object DetectorTeleport : Detector("Teleport", true), DetectMovement {
    private val minTeleportDistance by float("MinTeleportDistance", 1f, 0f..10f)

    var currentTickPlayerEntity: PlayerEntityStatus? = null
    var lastTickPlayerEntity: PlayerEntityStatus? = null

    override fun detectMovement(entityRecorder: EntityRecorder) {
        if (entityRecorder.entityList.size > 1) {
            lastTickPlayerEntity =
                entityRecorder.entityList.getOrNull(entityRecorder.entityList.size - 2) ?: return

            currentTickPlayerEntity = entityRecorder.entityList.last()

            if (lastTickPlayerEntity!!.pos.distanceTo(currentTickPlayerEntity!!.pos) > minTeleportDistance) {
                entityRecorder.flagsList[FlagTypes.TELEPORT] = entityRecorder.flagsList[FlagTypes.TELEPORT]!!.plus(1)
                entityRecorder.isReported = false
                if (entityRecorder.entityList.size > 1) {
                    entityRecorder.entityList.clear()
                }
                return
            }
        }
    }
}
