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
import net.ccbluex.liquidbounce.utils.client.world
import net.minecraft.entity.Entity
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import java.util.*

object ModuleCheatDetector : ClientModule("CheatDetector", Category.MISC) {
    // Setting
    private val minFlags by int("MinFlags", 1, 0..10)
    private val reportFlagsInterval by int("ReportFlagsInterval", 10, 0..100)

    val detectorsMovement =
        listOf<DetectMovement>(
            DetectorSimulation,
            DetectorTeleport,
        )

    val detectorsPacket =
        listOf<DetectPacket>(
            DetectorReach,
        )

    val detectorEventsList = mutableListOf<DetectorEvent>()

    val worldEntityRecorder = mutableMapOf<UUID, EntityRecorder>()

    init {

        tree(DetectorSimulation)
        tree(DetectorTeleport)
        tree(DetectorReach)
    }

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

            worldEntityRecorder.forEach { entityRecorder ->
                detectMovement(entityRecorder.value)
                chatFlags(entityRecorder.value)
                if (entityRecorder.key in detectorEventsList.map { it.uuid }) {
                    detectorEventsList.filter { entityRecorder.key == it.uuid }.forEach {
                        if (entityRecorder.key.equals(it.uuid)) {
                            detectSpecial(entityRecorder.value, it.packet)
                        }
                    }
                }
            }

            detectorEventsList.clear()
        }

    @Suppress("unused")
    private val worldChangeHandler =
        handler<WorldChangeEvent> {
            worldEntityRecorder.clear()
        }

    @Suppress("unused")
    private val packetHandler =
        handler<PacketEvent> { event ->
            when (event.packet) {
                is EntityVelocityUpdateS2CPacket -> {
                    if (event.packet.entityId == player.id) {
                        return@handler
                    }
                    worldEntityRecorder
                        .filter {

                            if (it.value.entityList.isEmpty()) return@handler
                            it.value.entityList
                                .last()
                                .id == event.packet.entityId
                        }.forEach {
                            if (it.value.entityList.size > 1) {
                                it.value.entityList.clear()
                            }
                        }
                }

                is EntityDamageS2CPacket ->
                    if (event.packet.sourceDirectId == event.packet.sourceCauseId && event.packet.sourceDirectId != -1) {
                        detectorEventsList.add(
                            DetectorEvent(
                                world.getEntityById(event.packet.sourceDirectId)!!.uuid,
                                event.packet,
                            ),
                        )
                    }

                is EntityAnimationS2CPacket ->
                    if (event.packet.animationId == 0) {
                        detectorEventsList.add(
                            DetectorEvent(
                                world.getEntityById(event.packet.entityId)!!.uuid,
                                event.packet,
                            ),
                        )
                    }
            }
        }

    fun getEntityByUUID(uuid: UUID): Entity? {
        val world = mc.world ?: return null
        return world.entities.firstOrNull { it.uuid.equals(uuid) }
    }

    private fun detectMovement(entityRecorder: EntityRecorder) {
        worldEntityRecorder.forEach {
            if (it.value.entityList.size > 1) {
                for (detector in detectorsMovement) {
                    detector.detectMovement(entityRecorder)
                }
            }
        }
    }

    private fun chatFlags(entityRecorder: EntityRecorder) {
        var isChat = false
        if (!entityRecorder.isReported) {
            entityRecorder.flagsList.forEach {
                if (it.value >= minFlags && (it.value - minFlags) % reportFlagsInterval == 0) {
                    chat(
                        """§c§l[§r§c§lCheatDetector§r§c§l] §r§c§lPlayer §r§c§l${entityRecorder.entityList.last().name} §r§c§lwas
                            |simulated ${it.key.name}(VL:${it.value}).
                        """.trim().lines().joinToString(" "),
                    )

                    isChat = true
                }
            }
        }

        if (isChat) {
            entityRecorder.isReported = false
        }
    }

    private fun detectSpecial(
        entityRecorder: EntityRecorder,
        packet: Packet<*>,
    ) {
        for (detector in detectorsPacket) {
            detector.detectPacket(entityRecorder, packet)
        }
    }
}

data class EntityRecorder(
    val entityList: MutableList<PlayerEntityStatus>,
    val flagsList: MutableMap<FlagTypes, Int> =
        mutableMapOf(
            FlagTypes.SIMULATION to 0,
            FlagTypes.TELEPORT to 0,
            FlagTypes.REACH to 0,
        ),
) {
    var isReported = false
}

data class DetectorEvent(
    val uuid: UUID,
    val packet: Packet<*>,
    val time: Int = world.time.toInt(),
)

enum class FlagTypes(
    name: String,
) {
    SIMULATION("simulation"),
    TELEPORT("teleport"),
    REACH("reach"),
}
