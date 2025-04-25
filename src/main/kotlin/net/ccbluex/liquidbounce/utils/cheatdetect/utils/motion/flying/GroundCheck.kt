package net.ccbluex.liquidbounce.utils.cheatdetect.utils.motion.flying

import net.ccbluex.liquidbounce.utils.cheatdetect.PlayerDataRecorder
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.Detector
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.DetectorAlgorithm
import net.ccbluex.liquidbounce.utils.cheatdetect.utils.DetectorCategory

object GroundCheck : FlyingDetectorAlgorithm("GroundCheck", true){

    private var isSameY : Boolean = true
    private var pervoiousY : Double? = null
    override fun detect(entityId: Int, playerDataRecorder: PlayerDataRecorder): Boolean {

        //we can't any information if positionList size is less than 2
        if(playerDataRecorder.positionList.size < 2) return false

        for(i in playerDataRecorder.positionList){
            if(pervoiousY == null){
                pervoiousY = i.y
                continue
            }
            if(isSameY) {
                if (i.y != pervoiousY) {
                    isSameY = false
                }
            }
        }

        if(!isSameY){
            return false
        }

        if(playerDataRecorder.groundList.last() == true){
            return false
        }

        return true

    }
}
