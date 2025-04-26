package net.ccbluex.liquidbounce.utils.cheatdetect.utils

import net.ccbluex.liquidbounce.utils.cheatdetect.utils.WorldUtils.getEntityById
import net.ccbluex.liquidbounce.utils.client.chat

object ChatUtils {

    fun sendFlagMessage(entityId : Int,flagType : String, flag : Int){
        val entity = getEntityById(entityId) ?:return
        chat("[CheatDetect]:" + entity.name.string + "stimulate" + flagType + "for " + flag + "times")
    }
}
