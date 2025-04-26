package net.ccbluex.liquidbounce.utils.cheatdetect

import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket

data class PlayerAttackEventRecord(val attackTick: Long,val attackPacket : EntityDamageS2CPacket)
