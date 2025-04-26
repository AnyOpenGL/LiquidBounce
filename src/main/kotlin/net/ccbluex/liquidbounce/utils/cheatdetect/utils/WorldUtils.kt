package net.ccbluex.liquidbounce.utils.cheatdetect.utils

import net.ccbluex.liquidbounce.utils.client.mc
import net.minecraft.entity.Entity

object WorldUtils {

    fun getEntityById(id : Int) : Entity? = if (mc.world == null) null else mc.world!!.getEntityById(id)
}
