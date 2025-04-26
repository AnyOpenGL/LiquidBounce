package net.ccbluex.liquidbounce.utils.cheatdetect

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.DisconnectEvent
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleCheatDetector
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.ChatUtils.sendFlagMessage
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.DetectorCategory
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.motion.flying.FlyingDetector
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.client.world
import net.ccbluex.liquidbounce.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket


object CheatDetect : EventListener {

    var shouldLogger: Boolean = true

    var worldPlayerRecorder = mutableListOf<PlayerDataRecorder>()

    val detectors = mutableListOf<Detector>(
        FlyingDetector
    )


    val maxKeepEntity get() = ModuleCheatDetector.maxKeepEntity


    private val packetHandler = handler<PacketEvent> { event ->

        if (event.packet is EntityDamageS2CPacket) {

        }
    }


    val tickHandler = handler<GameTickEvent>(priority = FIRST_PRIORITY) {


        if (!mc.isRunning) return@handler

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

            if (playerDataRecorder.playerEntityList.size > maxKeepEntity) {
                playerDataRecorder.playerEntityList = playerDataRecorder.playerEntityList.subList(
                    playerDataRecorder.playerEntityList.size - maxKeepEntity,
                    playerDataRecorder.playerEntityList.size
                )
            }
            for (detector in detectors) {
                val playerFlagRecorder = detector.update(playerDataRecorder.entityId, playerDataRecorder)
                if (playerFlagRecorder.isFlagged) {

                    playerDataRecorder.flagList.get(playerDataRecorder.flagList.indexOfFirst { it.detector.category == playerFlagRecorder.flagType }).flags++

                    flagMessage(playerDataRecorder)
                }
            }


        }

    }

    val resetHandler = handler<DisconnectEvent>(priority = FIRST_PRIORITY) {
        worldPlayerRecorder.clear()
    }


    fun flagMessage(playerDataRecorder: PlayerDataRecorder) {
        for (i in playerDataRecorder.flagList) {
            if (i.flags >= i.detector.minFlags && i.flags % i.detector.flagsInterval == 0) {
                sendFlagMessage(
                    playerDataRecorder.entityId,
                    DetectorCategory.getReadableName(i.detector.category),
                    i.flags
                )
            }
        }
    }
}
