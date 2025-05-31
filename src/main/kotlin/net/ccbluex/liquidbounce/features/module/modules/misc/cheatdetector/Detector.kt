package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.minecraft.network.packet.Packet

abstract class Detector(
    name: String,
    enabled: Boolean,
) : ToggleableConfigurable(ModuleCheatDetector, name, enabled)

interface DetectMovement {
    fun detectMovement(playerStatusRecorder: PlayerStatusRecorder)
}

interface DetectPacket {
    fun detectPacket(
        playerStatusRecorder: PlayerStatusRecorder,
        packet: Packet<*>,
    )
}
