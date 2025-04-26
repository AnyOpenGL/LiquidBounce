package net.ccbluex.liquidbounce.utils.cheatdetect

import net.ccbluex.liquidbounce.utils.cheatdetect.utils.PlayerEntityRecorder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d

class PlayerDataRecorder(
    val entityId: Int
) {
    var playerEntityRecorderList: MutableList<PlayerEntityRecorder> = mutableListOf()
        set(value) = if (value.size > CheatDetect.maxKeepEntity) {
            field = value.subList(value.size - CheatDetect.maxKeepEntity, value.size)

        }
        else {
            field = value
        }

    val playerEntityList: MutableList<PlayerEntity> get() = playerEntityRecorderList.mapTo(mutableListOf()) { it.playerEntity }


    var playerAttackEventRecordList: MutableList<PlayerAttackEventRecord> = mutableListOf()


    var flagList: MutableList<Flags> = CheatDetect.detectors.mapTo(mutableListOf()) { Flags(it) }
    val positionList: MutableList<Vec3d> get() = playerEntityList.mapTo(mutableListOf()) { it.pos }

    val groundList: MutableList<Boolean> get() = playerEntityList.mapTo(mutableListOf()) { it.isOnGround }

}
