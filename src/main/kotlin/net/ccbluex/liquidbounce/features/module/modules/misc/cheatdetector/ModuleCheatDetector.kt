package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.PacketEvent
import net.ccbluex.liquidbounce.event.events.WorldChangeEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.PlayerEntityStatus
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.PlayerEntityStatus.Companion.getStatus
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.entity.Entity
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import java.util.*

object ModuleCheatDetector : ClientModule("CheatDetector", Category.MISC) {
    // Setting
    private val minFlags by int("MinFlags", 1, 0..10)
    private val reportFlagsInterval by int("ReportFlagsInterval", 10, 0..100)

    val detectors =
        listOf<Detector>(
            DetectorSimulation,
            DetectorTeleport,
        )

    val worldEntityRecorder = mutableMapOf<UUID, EntityRecorder>()

    @Suppress("unused")
    private val tickHandler =
        handler<GameTickEvent> {
            val world = mc.world ?: return@handler

            val worldEntities = world.players.toMutableList()

            val currentWorldEntityRecorder = worldEntityRecorder.toMutableMap()

            // refresh worldEntityRecorder
            currentWorldEntityRecorder.forEach { entityRecorder ->
                world.players.forEach { playerEntity ->

                    if (playerEntity.uuid.equals(entityRecorder.key)) {
                        // add playerEntityStatus into list
                        worldEntityRecorder[playerEntity.uuid]!!.entityList.add(playerEntity.getStatus())

                        // remove the entity from worldEntities
                        worldEntities.remove(playerEntity)
                    }
                }
            }

            worldEntityRecorder.forEach {
                if (it.value.entityList.size > 100) {
                    it.value.entityList
                        .subList(0, it.value.entityList.size - 100)
                        .clear()
                }
            }

            if (worldEntities.isNotEmpty()) {
                worldEntities.forEach {
                    worldEntityRecorder.put(it.uuid, EntityRecorder(mutableListOf(it.getStatus())))
                }
            }

            detectMovement()
            chatFlags()
        }

    @Suppress("unused")
    private val worldChangeHandler =
        handler<WorldChangeEvent> {
            worldEntityRecorder.clear()
        }

    @Suppress("unused")
    private val packetHandler =
        handler<PacketEvent> { event ->

            if (event.packet is EntityVelocityUpdateS2CPacket) {

                if (event.packet.entityId == player.id) {

                    return@handler
                }

                worldEntityRecorder
                    .filter {
                        it.value.entityList
                            .last()
                            .id == event.packet.entityId
                    }.forEach {
                        if (it.value.entityList.size > 1) {
                            it.value.entityList.clear()
                        }
                    }
            }
        }

    fun getEntityByUUID(uuid: UUID): Entity? {
        val world = mc.world ?: return null
        return world.entities.firstOrNull { it.uuid.equals(uuid) }
    }

    private fun detectMovement() {
        var currentTickPlayerEntity: PlayerEntityStatus? = null
        var lastTickPlayerEntity: PlayerEntityStatus? = null

        var simulateFailTimes = 0
        worldEntityRecorder.forEach {
            if (it.value.entityList.size > 1) {
                for (detector in detectors) {
                    detector.detect(it.value)
                }
            }
        }
    }

    private fun chatFlags() {
        worldEntityRecorder.forEach {
            val entityRecorder = it

            var isChat = false

            if (!it.value.isReported) {
                it.value.flagsList.forEach {
                    if (it.value >= minFlags && (it.value - minFlags) % reportFlagsInterval == 0) {
                        chat(
                            """§c§l[§r§c§lCheatDetector§r§c§l] §r§c§lPlayer §r§c§l${entityRecorder.value.entityList.last().name} §r§c§lwas
                            |simulated ${it.key.name}(VL:${it.value}).
                        """.trim().lines().joinToString(" "),
                        )

                        isChat = true
                    }
                }
            }

            if (isChat) {
                it.value.isReported = false
            }
        }
    }

    fun resetPlayerEntityStatusRecorder(uuid: UUID) {
        if (worldEntityRecorder.contains(uuid)) {
            worldEntityRecorder.get(uuid)!!.entityList.clear()
        }
    }
}

data class EntityRecorder(
    val entityList: MutableList<PlayerEntityStatus>,
    val flagsList: MutableMap<FlagTypes, Int> =
        mutableMapOf(
            FlagTypes.SIMULATION to 0,
            FlagTypes.TELEPORT to 0,
        ),
) {
    var isReported = false
}

enum class FlagTypes(
    name: String,
) {
    SIMULATION("simulation"),
    TELEPORT("teleport"),
}
