package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.bot.BotConfigurable
import net.ccbluex.liquidbounce.utils.bot.BotManager
import net.ccbluex.liquidbounce.utils.bot.BotManager.botChat
import net.ccbluex.liquidbounce.utils.bot.utils.CalculatePath
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.movement.MotionStatus
import net.ccbluex.liquidbounce.utils.movement.MovementClass
import net.ccbluex.liquidbounce.utils.movement.MovementManager
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object ModuleBotTest : ClientModule("BotTest", Category.MISC) {

    private val botConfigurable = tree(BotConfigurable(this))
    override fun enable() {
        val targetPosition = player.raycast(100.0, 0.0F,false).pos
        chat("New target : ${targetPosition.x} ${targetPosition.y} ${targetPosition.z}")

        val pathResult = CalculatePath.findPath(player.pos, Vec3d(174.0,63.0,-122.0))
        botChat("Path Result: ${pathResult.path.size}" +
            "\nTurn Points: ${pathResult.turnPoints.size}" +
            pathResult.path.toString())
    }


    fun goto(vec3d: Vec3d) {
        BotManager.setTargetPosition(botConfigurable.toBotPlan(vec3d,MotionStatus.SPRINT,this@ModuleBotTest))
        botChat("New target : ${vec3d.x} ${vec3d.y} ${vec3d.z}")
    }

}
