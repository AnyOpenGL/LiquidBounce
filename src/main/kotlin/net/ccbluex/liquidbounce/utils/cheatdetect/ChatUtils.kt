package net.ccbluex.liquidbounce.utils.cheatdetect

import com.nimbusds.openid.connect.sdk.federation.entities.EntityID
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.DetectorCategory
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.world

object ChatUtils {

    fun sendFlagMessage(entityId : Int,flagType : String, flag : Int){
        chat("[CheatDetect]:" + world.entities.filter { it.id == entityId } + "stimulate" + flagType + "for " + flag + "times")
    }
}
