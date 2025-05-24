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

    private val worldEntityRecorder = mutableSetOf<EntityRecorder>()

    @Suppress("unused")
    private val tickHandler =
        handler<GameTickEvent> {
            val world = mc.world ?: return@handler

            val worldEntities = world.players.toMutableList()

            val currentWorldEntityRecorder = worldEntityRecorder.toMutableSet()

            currentWorldEntityRecorder.forEach {
                val entityUUID = it.uuid
                worldEntities.forEach {
                    val playerEntity = it as PlayerEntity
                    if (it.uuid.equals(entityUUID)) {
                        worldEntityRecorder.first { it.uuid.equals(entityUUID) }.entityList.add(playerEntity.getStatus())
                    }
                }
            }

            worldEntityRecorder.forEach {
                if (it.entityList.size > 100) {
                    it.entityList.subList(0, it.entityList.size - 100).clear()
                }
            }

            if (worldEntities.isNotEmpty()) {
                worldEntities.forEach {
                    worldEntityRecorder.add(EntityRecorder(mutableListOf(it.getStatus()), it.uuid, mutableMapOf()))
                }
            }

            detectMovement()
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
                worldEntityRecorder.filter { it.entityList.last().id == event.packet.entityId }.forEach {
                    it.entityList.clear()
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
                DirectionalInput.NONE,
            )

        var simulateFailTimes = 0
        worldEntityRecorder.forEach {
            if (it.entityList.size > 1) {
                lastTickPlayerEntity =
                    it.entityList.getOrNull(it.entityList.size - 2) ?: return@forEach

                currentTickPlayerEntity = it.entityList.last()

                val playerEntity = getEntityByUUID(it.uuid) as? PlayerEntity ?: return@forEach

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

                if (simulateFailTimes == 5) {
                    chat(
                        "§c§l[§r§c§lCheatDetector§r§c§l] §r§c§lPlayer §r§c§l${it.entityList.last().name.string} §r§c§lwas simulatee Simulation.",
                    )

                    val flags = it.flagsList.get(FlagTypes.SIMULATION)
                }

                simulateFailTimes = 0
            }
        }
    }

    fun resetPlayerEntityStatusRecorder(uuid: UUID) {
        worldEntityRecorder.filter { it.uuid.equals(uuid) }.forEach {
            it.entityList.clear()
        }
    }
}

data class EntityRecorder(
    val entityList: MutableList<PlayerEntityStatus>,
    val uuid: UUID,
    val flagsList: MutableMap<FlagTypes, Int>,
) {
    override fun hashCode(): Int = uuid.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EntityRecorder

        if (uuid != other.uuid) return false

        return true
    }
}

enum class FlagTypes(
    name: String,
) {
    SIMULATION("simulation"),
    TELEPORT("teleport"),
}
