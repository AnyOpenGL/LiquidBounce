package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.entity.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
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
                        worldEntityRecorder.first { it.uuid.equals(entityUUID) }.entityList.add(playerEntity)
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
                    worldEntityRecorder.add(EntityRecorder(mutableListOf(it), it.uuid))
                }
            }

            detectMovement()
        }

    private fun getEntityByUUID(uuid: UUID): Entity? {
        val world = mc.world ?: return null
        return world.entities.firstOrNull { it.uuid.equals(uuid) }
    }

    private fun detectMovement() {
        var simulatePlayer: SimulatedPlayer

        var currentTickPlayerEntity: PlayerEntity? = null
        var lastTickPlayerEntity: PlayerEntity? = null

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

                for (directionalInput in directionalInputList) {
                    simulatePlayer =
                        SimulatedPlayer(
                            lastTickPlayerEntity,
                            SimulatedPlayer.SimulatedPlayerInput(
                                directionalInput,
                                if ((currentTickPlayerEntity.y - lastTickPlayerEntity.y) > 0.0) true else false,
                                lastTickPlayerEntity.isSprinting,
                                lastTickPlayerEntity.isSneaking,
                            ),
                            lastTickPlayerEntity.pos,
                            lastTickPlayerEntity.velocity,
                            lastTickPlayerEntity.boundingBox,
                            currentTickPlayerEntity.yaw,
                            currentTickPlayerEntity.pitch,
                            lastTickPlayerEntity.isSprinting,
                            lastTickPlayerEntity.fallDistance,
                            lastTickPlayerEntity.jumpingCooldown,
                            if ((currentTickPlayerEntity.y - lastTickPlayerEntity.y) > 0.0) true else false,
                            false,
                            lastTickPlayerEntity.isOnGround,
                            lastTickPlayerEntity.horizontalCollision,
                            lastTickPlayerEntity.verticalCollision,
                            lastTickPlayerEntity.isTouchingWater,
                            lastTickPlayerEntity.isSwimming,
                            lastTickPlayerEntity.isSubmergedInWater,
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
                }

                simulateFailTimes = 0
            }
        }
    }
}

data class EntityRecorder(
    val entityList: MutableList<PlayerEntity>,
    val uuid: UUID,
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
