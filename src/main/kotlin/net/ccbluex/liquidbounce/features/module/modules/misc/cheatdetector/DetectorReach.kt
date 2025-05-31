package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket
import net.minecraft.util.hit.EntityHitResult

object DetectorReach : Detector("Reach", true), DetectEntityDamageS2CPacket, DetectEntityAnimationS2CPacket {
    private val legitReach by float("LegitReach", 3f, 0f..10f)

    override fun detectEntityDamageS2CPacket(
        playerStatusRecorder: PlayerStatusRecorder,
        packet: EntityDamageS2CPacket,
    ) {
        if (playerStatusRecorder.entityList
                .last()
                .pos
                .distanceTo(world.getEntityById(packet.entityId)!!.pos) > legitReach
        ) {
            playerStatusRecorder.flagsList[FlagTypes.REACH]!!.flagsCounter =
                playerStatusRecorder.flagsList[FlagTypes.REACH]!!.flagsCounter.plus(1)
        }
    }

    override fun detectEntityAnimationS2CPacket(
        playerStatusRecorder: PlayerStatusRecorder,
        packet: EntityAnimationS2CPacket,
    ) {
        val sourceCausedEntity = world.getEntityById(packet.entityId) ?: return
        val attackTargetHitResult = sourceCausedEntity.raycast(10.0, 0f, false)
        if (attackTargetHitResult !is EntityHitResult) return

        val attackTargetEntity = attackTargetHitResult.entity

        if (sourceCausedEntity.distanceTo(attackTargetEntity) > legitReach) {
            playerStatusRecorder.flagsList[FlagTypes.REACH]!!.flagsCounter =
                playerStatusRecorder.flagsList[FlagTypes.REACH]!!.flagsCounter.plus(1)
        }
    }
}
