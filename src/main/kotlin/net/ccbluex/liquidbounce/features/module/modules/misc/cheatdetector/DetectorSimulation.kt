package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.utils.entity.SimulatedPlayer
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput
import net.minecraft.entity.player.PlayerEntity

object DetectorSimulation : Detector("Simulation", true), DetectMovement {
    private val maxDistanceDiff by float("MaxDistanceDiff", 1f, 0f..10f)

    var simulatePlayer: SimulatedPlayer? = null
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

    override fun detectMovement(entityRecorder: EntityRecorder) {
        lastTickPlayerEntity = entityRecorder.entityList.getOrNull(entityRecorder.entityList.size - 2) ?: return

        currentTickPlayerEntity = entityRecorder.entityList.last()
        val playerEntity = world.getEntityById(entityRecorder.entityList.last().id) as? PlayerEntity ?: return

        for (directionalInput in directionalInputList) {
            simulatePlayer =
                SimulatedPlayer(
                    playerEntity,
                    SimulatedPlayer.SimulatedPlayerInput(
                        directionalInput,
                        if ((currentTickPlayerEntity!!.getY() - lastTickPlayerEntity!!.getY()) > 0.0) true else false,
                        lastTickPlayerEntity!!.sprinting,
                        lastTickPlayerEntity!!.sneaking,
                    ),
                    lastTickPlayerEntity!!.pos,
                    lastTickPlayerEntity!!.velocity,
                    lastTickPlayerEntity!!.boundingBox,
                    currentTickPlayerEntity!!.yaw,
                    currentTickPlayerEntity!!.pitch,
                    lastTickPlayerEntity!!.sprinting,
                    lastTickPlayerEntity!!.fallDistance,
                    lastTickPlayerEntity!!.jumpingCooldown,
                    if ((currentTickPlayerEntity!!.getY() - lastTickPlayerEntity!!.getY()) > 0.0) true else false,
                    false,
                    lastTickPlayerEntity!!.onGround,
                    lastTickPlayerEntity!!.horizontalCollision,
                    lastTickPlayerEntity!!.verticalCollision,
                    lastTickPlayerEntity!!.touchingWater,
                    lastTickPlayerEntity!!.isSwimming,
                    lastTickPlayerEntity!!.submergedInWater,
                    lastTickPlayerEntity!!.fluidHeight,
                    lastTickPlayerEntity!!.submergedFluidTag.toHashSet(),
                )

            simulatePlayer!!.tick()

            if (currentTickPlayerEntity!!.pos.distanceTo(simulatePlayer!!.pos) > maxDistanceDiff) {
                simulateFailTimes++
            }
        }

        if (simulateFailTimes == 9) {
            entityRecorder.flagsList[FlagTypes.SIMULATION] = entityRecorder.flagsList[FlagTypes.SIMULATION]!!.plus(1)
            entityRecorder.flagsList.forEach { if (it.key == FlagTypes.SIMULATION) it.key.isReported = false }
        }

        simulateFailTimes = 0
    }
}
