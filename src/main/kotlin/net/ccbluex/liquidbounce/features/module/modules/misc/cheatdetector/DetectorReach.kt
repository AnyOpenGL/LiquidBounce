package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket
import net.minecraft.util.hit.EntityHitResult

object DetectorReach : Detector("Reach", true), DetectPacket {
    private val legitReach by float("LegitReach", 3f, 0f..10f)

    override fun detectPacket(
        entityRecorder: EntityRecorder,
        packet: Packet<*>,
    ) {
        if (packet !is EntityDamageS2CPacket && packet !is EntityAnimationS2CPacket) return

        when (packet) {
            is EntityDamageS2CPacket -> detectEntityDamageS2CPacket(entityRecorder, packet)
            is EntityAnimationS2CPacket -> detectEntityAnimationS2CPacket(entityRecorder, packet)
        }
    }

    fun detectEntityDamageS2CPacket(
        entityRecorder: EntityRecorder,
        packet: EntityDamageS2CPacket,
    ) {
        if (entityRecorder.entityList
                .last()
                .pos
                .distanceTo(world.getEntityById(packet.entityId)!!.pos) > legitReach
        ) {
            entityRecorder.flagsList[FlagTypes.REACH] = entityRecorder.flagsList[FlagTypes.REACH]!!.plus(1)
        }
    }

    fun detectEntityAnimationS2CPacket(
        entityRecorder: EntityRecorder,
        packet: EntityAnimationS2CPacket,
    ) {
        val sourceCausedEntity = world.getEntityById(packet.entityId) ?: return
        val attackTargetHitResult = sourceCausedEntity.raycast(10.0, 0f, false)
        if (attackTargetHitResult !is EntityHitResult) return

        val attackTargetEntity = attackTargetHitResult.entity

        if (sourceCausedEntity.distanceTo(attackTargetEntity) > legitReach) {
            entityRecorder.flagsList[FlagTypes.REACH] = entityRecorder.flagsList[FlagTypes.REACH]!!.plus(1)
        }
    }
}
