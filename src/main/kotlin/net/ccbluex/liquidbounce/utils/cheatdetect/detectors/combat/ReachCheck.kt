package net.ccbluex.liquidbounce.utils.cheatdetect.detectors.combat

import net.ccbluex.liquidbounce.utils.cheatdetect.PlayerDataRecorder
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.DetectorCategory
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.WorldUtils.getEntityById
import net.minecraft.entity.player.PlayerEntity

object ReachCheck : Detector("Reach", DetectorCategory.Combat, true) {


    override fun detect(entityId: Int, playerDataRecorder: PlayerDataRecorder): Boolean {

        //if playerAttackEventRecordList is empty, we can't detect anything
        if (playerDataRecorder.playerAttackEventRecordList.size == 0) return false

        //get the last playerAttackEventRecord
        val lastPlayerAttackEventRecord = playerDataRecorder.playerAttackEventRecordList.last()

        //get the last playerEntity
        val lastPlayerEntity =
            if (playerDataRecorder.playerEntityRecorderList.filter { it.gameTick == lastPlayerAttackEventRecord.attackTick }.size == 0) {
                return false
            } else {
                playerDataRecorder.playerEntityRecorderList.filter { it.gameTick == lastPlayerAttackEventRecord.attackTick }
                    .last().playerEntity
            }

        //get the target entity
        val targetEntityRecorderList =
            if (getEntityById(lastPlayerAttackEventRecord.attackPacket.entityId) !is PlayerEntity && getEntityById(
                    lastPlayerAttackEventRecord.attackPacket.entityId
                ) != null && lastPlayerAttackEventRecord.attackTick == mc.world!!.time
            ) {
                null
            } else if (playerDataRecorder.playerEntityRecorderList.filter { it.gameTick == lastPlayerAttackEventRecord.attackTick }.size != 0) {
                playerDataRecorder.playerEntityRecorderList.filter { it.gameTick == lastPlayerAttackEventRecord.attackTick }
            } else {
                return false
            }


        val targetEntity = if (targetEntityRecorderList == null) {
            getEntityById(lastPlayerAttackEventRecord.attackPacket.entityId)
        }
        else if(targetEntityRecorderList.size != 0){
            targetEntityRecorderList.last().playerEntity
        }
        else{
            return false
        }

        if (lastPlayerEntity.distanceTo(targetEntity) > 3f) {
            return true
        } else {
            return false
        }

    }
}
