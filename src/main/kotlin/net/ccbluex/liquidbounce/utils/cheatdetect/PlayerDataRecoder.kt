package net.ccbluex.liquidbounce.utils.cheatdetect

import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.Detector
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Vec3d

class PlayerDataRecorder(
    val entityId: Int
) {
    var playerEntityList: MutableList<PlayerEntity> = mutableListOf()
        set(value) = if (value.size > CheatDetect.maxKeepEntity) {
            field = value.subList(value.size - CheatDetect.maxKeepEntity, value.size)

        }
        else {
            field = value
        }

    var flagList: MutableList<Flags> = CheatDetect.detectors.mapTo(mutableListOf()) { Flags(it) }
    val positionList: MutableList<Vec3d> get() = playerEntityList.mapTo(mutableListOf()) { it.pos }

    val groundList: MutableList<Boolean> get() = playerEntityList.mapTo(mutableListOf()) { it.isOnGround }

}


data class PlayerData(
    val cheatId: Int,
) {
    companion object {
        val Flying = PlayerData(1)
        val Reach = PlayerData(2)
    }
}


class Flags(
    val detector: Detector
) {
    var flags: Int = 0
}


