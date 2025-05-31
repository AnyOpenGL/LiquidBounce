package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.DetectorReach.detectEntityAnimationS2CPacket
import net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector.DetectorReach.detectEntityDamageS2CPacket
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket

abstract class Detector(
    name: String,
    enabled: Boolean,
) : ToggleableConfigurable(ModuleCheatDetector, name, enabled)

interface DetectMovement {
    fun detectMovement(playerStatusRecorder: PlayerStatusRecorder)
}

interface DetectOtherPacket

object DetectPacket {
    fun detectPacket(
        playerStatusRecorder: PlayerStatusRecorder,
        packet: Packet<*>,
    ) {
        when (packet) {
            is EntityDamageS2CPacket -> detectEntityDamageS2CPacket(playerStatusRecorder, packet)
            is EntityAnimationS2CPacket -> detectEntityAnimationS2CPacket(playerStatusRecorder, packet)
        }
    }
}

interface DetectEntityDamageS2CPacket : DetectOtherPacket {
    fun detectEntityDamageS2CPacket(
        playerStatusRecorder: PlayerStatusRecorder,
        packet: EntityDamageS2CPacket,
    )
}

interface DetectEntityAnimationS2CPacket : DetectOtherPacket {
    fun detectEntityAnimationS2CPacket(
        playerStatusRecorder: PlayerStatusRecorder,
        packet: EntityAnimationS2CPacket,
    )
}
