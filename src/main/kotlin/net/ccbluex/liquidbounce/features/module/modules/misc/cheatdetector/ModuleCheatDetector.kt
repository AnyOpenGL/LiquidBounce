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
import net.ccbluex.liquidbounce.utils.entity.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket
import java.util.*

object ModuleCheatDetector : ClientModule("CheatDetector", Category.MISC) {
    // Setting
    private val maxDistanceDiff by float("MaxDistanceDiff", 1f, 0f..10f)
    private val minTeleportDistance by float("MinTeleportDistance", 1f, 0f..10f)

    private val minFlags by int("MinFlags", 1, 0..10)
    private val reportFlagsInterval by int("ReportFlagsInterval", 10, 0..100)

    private val worldEntityRecorder = mutableMapOf<UUID, EntityRecorder>()

    @Suppress("unused")
    private val tickHandler =
        handler<GameTickEvent> {
            val world = mc.world ?: return@handler

            val worldEntities = world.players.toMutableList()

            val currentWorldEntityRecorder = worldEntityRecorder.toMutableMap()

            currentWorldEntityRecorder.forEach {
                val entityUUID = it.key
                worldEntities.forEach {
                    val playerEntity = it as PlayerEntity
                    if (it.uuid.equals(entityUUID)) {

                        worldEntityRecorder[it.uuid]!!.entityList.add(playerEntity.getStatus())
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
                        it.value.entityList.clear()
                    }
            }
        }

    private fun getEntityByUUID(uuid: UUID): Entity? {
        val world = mc.world ?: return null
        return world.entities.firstOrNull { it.uuid.equals(uuid) }
    }

    private fun detectMovement() {
        var simulatePlayer: SimulatedPlayer

        var currentTickPlayerEntity: PlayerEntityStatus? = null
        var lastTickPlayerEntity: PlayerEntityStatus? = null

        val directionalInputList =
            setOf<DirectionalInput>(
                DirectionalInput.FORWARDS,
                DirectionalInput.BACKWARDS,
                DirectionalInput.LEFT,
                DirectionalInput.RIGHT,
                DirectionalInput.FORWARDS_LEFT,
                DirectionalInput.FORWARDS_RIGHT,
                DirectionalInput.BACKWARDS_LEFT,
                DirectionalInput.BACKWARDS_RIGHT,
                DirectionalInput.NONE,
            )

        var simulateFailTimes = 0
        worldEntityRecorder.forEach {
            if (it.value.entityList.size > 1) {
                lastTickPlayerEntity =
                    it.value.entityList.getOrNull(it.value.entityList.size - 2) ?: return@forEach

                currentTickPlayerEntity = it.value.entityList.last()

                val playerEntity = getEntityByUUID(it.key) as? PlayerEntity ?: return@forEach

                if (lastTickPlayerEntity.pos.distanceTo(currentTickPlayerEntity.pos) > minTeleportDistance) {
                    it.value.flagsList[FlagTypes.TELEPORT] = it.value.flagsList[FlagTypes.TELEPORT]!!.plus(1)
                    it.value.isReported = false
                    resetPlayerEntityStatusRecorder(it.key)
                    return@forEach
                }

                for (directionalInput in directionalInputList) {
                    simulatePlayer =
                        SimulatedPlayer(
                            playerEntity,
                            SimulatedPlayer.SimulatedPlayerInput(
                                directionalInput,
                                if ((currentTickPlayerEntity.getY() - lastTickPlayerEntity.getY()) > 0.0) true else false,
                                lastTickPlayerEntity.sprinting,
                                lastTickPlayerEntity.sneaking,
                            ),
                            lastTickPlayerEntity.pos,
                            lastTickPlayerEntity.velocity,
                            lastTickPlayerEntity.boundingBox,
                            currentTickPlayerEntity.yaw,
                            currentTickPlayerEntity.pitch,
                            lastTickPlayerEntity.sprinting,
                            lastTickPlayerEntity.fallDistance,
                            lastTickPlayerEntity.jumpingCooldown,
                            if ((currentTickPlayerEntity.getY() - lastTickPlayerEntity.getY()) > 0.0) true else false,
                            false,
                            lastTickPlayerEntity.onGround,
                            lastTickPlayerEntity.horizontalCollision,
                            lastTickPlayerEntity.verticalCollision,
                            lastTickPlayerEntity.touchingWater,
                            lastTickPlayerEntity.isSwimming,
                            lastTickPlayerEntity.submergedInWater,
                            lastTickPlayerEntity.fluidHeight,
                            lastTickPlayerEntity.submergedFluidTag.toHashSet(),
                        )

                    simulatePlayer.tick()

                    if (currentTickPlayerEntity.pos.distanceTo(simulatePlayer.pos) > maxDistanceDiff) {
                        simulateFailTimes++
                    }
                }

                if (simulateFailTimes == 9) {
                    it.value.flagsList[FlagTypes.SIMULATION] = it.value.flagsList[FlagTypes.SIMULATION]!!.plus(1)
                    it.value.isReported = false
                }

                simulateFailTimes = 0
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
