package net.ccbluex.liquidbounce.utils.cheatdetect

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.cheatdetect.ChatUtils.sendFlagMessage
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.DetectorCategory
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.motion.flying.FlyingDetector
import net.ccbluex.liquidbounce.utils.client.world
import net.ccbluex.liquidbounce.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket

object CheatDetect : EventListener {

    //initialize the object
    val cheatDetect = CheatDetect

    var shouldLogger: Boolean = true

    val worldPlayerRecorder = mutableListOf<PlayerDataRecorder>()

    val detectors = mutableListOf<Detector>(
        FlyingDetector
    )


    private val packetHandler = handler<PacketEvent> { event ->

        if (event.packet is EntityDamageS2CPacket) {

        }
    }


    val tickHandler = handler<GameTickEvent>(priority = FIRST_PRIORITY) {


        //update worldPlayerRecorder
        for (entity in world.players) {
            if (worldPlayerRecorder.none { it.entityId == entity.id }) {

                worldPlayerRecorder.add(PlayerDataRecorder(entity.id))

            }
            //update playerEntityList
            worldPlayerRecorder.filter { it.entityId == entity.id }.first().playerEntityList.add(entity)

        }

        //Detect player
        for (playerDataRecorder in worldPlayerRecorder) {
            for (detector in detectors) {
                val playerFlagRecorder = detector.update(playerDataRecorder.entityId, playerDataRecorder)
                if (playerFlagRecorder.isFlagged) {

                    playerDataRecorder.flagList.get(playerDataRecorder.flagList.indexOfFirst { it.detector.category == playerFlagRecorder.flagType }).flags++

                    flagMessage(playerDataRecorder)
                }
            }


        }

    }


    fun flagMessage(playerDataRecorder: PlayerDataRecorder) {
        for (i in playerDataRecorder.flagList) {
            if (i.flags >= i.detector.minFlags || i.flags % i.detector.flagsInterval == 0) {
                sendFlagMessage(
                    playerDataRecorder.entityId,
                    DetectorCategory.getReadableName(i.detector.category),
                    i.flags
                )
            }
        }
    }
}
