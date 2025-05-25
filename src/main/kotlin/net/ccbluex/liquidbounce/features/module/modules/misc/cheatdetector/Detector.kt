package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.minecraft.network.packet.Packet

abstract class Detector(
    name: String,
    enabled: Boolean,
) : ToggleableConfigurable(ModuleCheatDetector, name, enabled)

interface DetectMovement {
    fun detectMovement(entityRecorder: EntityRecorder)
}

interface DetectPacket {
    fun detectPacket(
        entityRecorder: EntityRecorder,
        packet: Packet<*>,
    )
}
