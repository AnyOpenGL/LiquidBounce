package net.ccbluex.liquidbounce.utils.cheatdetect

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.DisconnectEvent
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.misc.ModuleCheatDetector
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.DetectorCategory
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.combat.ReachCheck
import net.ccbluex.liquidbounce.utils.cheatdetect.detectors.motion.flying.FlyingDetector
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.ChatUtils.sendFlagMessage
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.PlayerEntityRecorder
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.client.world
import net.ccbluex.liquidbounce.utils.kotlin.EventPriorityConvention.FIRST_PRIORITY
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket


object CheatDetect : EventListener {

    var shouldLogger: Boolean = true

    var worldPlayerRecorder = mutableListOf<PlayerDataRecorder>()

    val detectors = mutableListOf<Detector>(
        FlyingDetector,
        ReachCheck,
    )


    val maxKeepEntity get() = ModuleCheatDetector.maxKeepEntity


    private val packetHandler = handler<PacketEvent> { event ->


        if (mc.world == null) return@handler
        if (event.packet is EntityDamageS2CPacket) {
            //if event.packet.sourceDirectId() == event.packet.sourceCauseId(),that means the player is attacked by projectile,so we don't detect it.
            if (event.packet.sourceDirectId() != event.packet.sourceCauseId()) return@handler
            worldPlayerRecorder.filter { it.entityId == event.packet.sourceDirectId }
                .first().playerAttackEventRecordList.add(PlayerAttackEventRecord(mc.world!!.time, event.packet))
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
            worldPlayerRecorder.filter { it.entityId == entity.id }.first().playerEntityRecorderList.add(
                PlayerEntityRecorder(
                    entity,
                    mc.world!!.time
                )
            )


        }

        //Detect player
        for (playerDataRecorder in worldPlayerRecorder) {

            if (playerDataRecorder.playerEntityList.size > maxKeepEntity) {
                playerDataRecorder.playerEntityRecorderList = playerDataRecorder.playerEntityRecorderList.subList(
                    playerDataRecorder.playerEntityRecorderList.size - maxKeepEntity,
                    playerDataRecorder.playerEntityRecorderList.size
                )
            }
            for (detector in detectors) {
                if (!detector.enabled) continue
                val playerFlagRecorder = detector.update(playerDataRecorder.entityId, playerDataRecorder)
                if (playerFlagRecorder.isFlagged) {

                    playerDataRecorder.flagList.get(playerDataRecorder.flagList.indexOfFirst { it.detector.category == playerFlagRecorder.flagType }).flags++

                    flagMessage(playerDataRecorder)
                }
            }


        }

    }

    fun reset() {
        worldPlayerRecorder.clear()
    }

    val disconnectHandler = handler<DisconnectEvent>(priority = FIRST_PRIORITY) {
        reset()
    }

    val changeWorldHandler = handler<WorldChangeEvent>(priority = FIRST_PRIORITY) {
        reset()
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
