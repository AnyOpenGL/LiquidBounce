package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.bot.BotConfigurable
import net.ccbluex.liquidbounce.utils.bot.BotManager
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.movement.MotionStatus
import net.ccbluex.liquidbounce.utils.movement.MovementClass
import net.ccbluex.liquidbounce.utils.movement.MovementManager

object ModuleBotTest : ClientModule("BotTest", Category.MISC) {

    private val botConfigurable = tree(BotConfigurable(this))
    override fun enable() {
        val targetPosition = player.raycast(100.0, 0.0F,false).pos
        chat("New target : ${targetPosition.x} ${targetPosition.y} ${targetPosition.z}")
        BotManager.setTargetPosition(botConfigurable.toBotPlanSimply(targetPosition,MotionStatus.SPRINT,this))

    }

}
