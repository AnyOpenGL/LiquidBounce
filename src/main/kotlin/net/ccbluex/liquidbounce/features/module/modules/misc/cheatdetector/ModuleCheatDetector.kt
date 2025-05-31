package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.cheatdetector.PlayerEntityStatus
import net.ccbluex.liquidbounce.utils.cheatdetector.PlayerEntityStatus.Companion.getStatus
import net.ccbluex.liquidbounce.utils.cheatdetector.WorldExtra.getUUIDById
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import java.util.*

object ModuleCheatDetector : ClientModule("CheatDetector", Category.MISC) {
    // Setting
    private val minFlags by int("MinFlags", 1, 0..10)
    private val reportFlagsInterval by int("ReportFlagsInterval", 10, 0..100)

    val detectorList =
        mutableListOf<Detector>(
            DetectorSimulation,
            DetectorTeleport,
            DetectorReach,
        )
    val worldPlayerStatusRecorder = mutableMapOf<UUID, PlayerStatusRecorder>()

    init {
        for (detector in detectorList) {
            tree(detector)
        }
    }

    @Suppress("unused")
    private val tickHandler =
        handler<GameTickEvent> {
            val world = mc.world ?: return@handler

            val worldPlayers = world.players.toMutableList()

            val currentWorldPlayersRecorder = worldPlayerStatusRecorder.toMutableMap()

            // refresh worldEntityRecorder
            currentWorldPlayersRecorder.forEach { entityRecorder ->
                world.players.forEach { playerEntity ->

                    if (playerEntity.uuid.equals(entityRecorder.key)) {
                        // add playerEntityStatus into list
                        worldPlayerStatusRecorder[playerEntity.uuid]!!.entityList.add(playerEntity.getStatus())

                        // remove the entity from worldEntities
                        worldPlayers.remove(playerEntity)
                    }
                }
            }

            // remove outdated entity from worldEntityRecorder
            worldPlayerStatusRecorder.forEach {
                if (it.value.entityList.size > 100) {
                    it.value.entityList
                        .subList(0, it.value.entityList.size - 100)
                        .clear()
                }
            }

            // add new entity into worldEntityRecorder
            if (worldPlayers.isNotEmpty()) {
                worldPlayers.forEach {
                    worldPlayerStatusRecorder.put(it.uuid, PlayerStatusRecorder(it.uuid, mutableListOf(it.getStatus())))
                }
            }

            // detect player
            worldPlayerStatusRecorder.forEach { playersStatusRecorder ->
                for (detector in detectorList) {
                    when (detector) {
                        is DetectMovement -> {
                            if (playersStatusRecorder.value.entityList.size > 1) {
                                detector.detectMovement(playersStatusRecorder.value)
                            }
                        }
                        is DetectPacket -> {
                            playersStatusRecorder.value.packetsList.forEach { packet ->
                                detector.detectPacket(playersStatusRecorder.value, packet)
                            }
                        }
                    }
                }
                chatFlags(playersStatusRecorder.value)
            }
        }

    @Suppress("unused")
    private val worldChangeHandler =
        handler<WorldChangeEvent> {
            worldPlayerStatusRecorder.clear()
        }

    @Suppress("unused")
    private val packetHandler =
        handler<PacketEvent> { event ->
            when (event.packet) {
                is EntityDamageS2CPacket -> addPacket(event.packet.sourceCauseId, event.packet)

                is EntityAnimationS2CPacket -> addPacket(event.packet.entityId, event.packet)

                is EntityVelocityUpdateS2CPacket -> addPacket(event.packet.entityId, event.packet)
            }
        }

    private fun addPacket(
        entityId: Int,
        packet: Packet<*>,
    ) {
        if (worldPlayerStatusRecorder.containsKey(world.getUUIDById(entityId))) {
            worldPlayerStatusRecorder[world.getUUIDById(entityId)]?.packetsList!!.add(packet)
        }
    }

    /**
     * Check the player's flags and report the player.
     */
    private fun chatFlags(playerStatusRecorder: PlayerStatusRecorder) {
        playerStatusRecorder.flagsList.forEach {
            if (it.value.flagsCounter >= minFlags &&
                (it.value.flagsCounter - minFlags) % reportFlagsInterval == 0 &&
                !it.value.isReported
            ) {
                chat(
                    "[CheatDetector] Player ${playerStatusRecorder.entityList.last().name} was simulated ${it.key.name}(VL:${it.value.flagsCounter}).",
                )

                it.value.isReported = true
            }
        }
    }
}

data class PlayerStatusRecorder(
    val uuid: UUID,
    val entityList: MutableList<PlayerEntityStatus>,
    val flagsList: MutableMap<FlagTypes, MutablePair<Int, Boolean>> =
        mutableMapOf(
            FlagTypes.SIMULATION to MutablePair(0, false),
            FlagTypes.TELEPORT to MutablePair(0, false),
            FlagTypes.REACH to MutablePair(0, false),
        ),
    val packetsList: MutableList<Packet<*>> = mutableListOf(),
)

enum class FlagTypes(
    name: String,
) {
    SIMULATION("simulation"),
    TELEPORT("teleport"),
    REACH("reach"),
}

class MutablePair<T, U>(
    var flagsCounter: T,
    var isReported: U,
)
