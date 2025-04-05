package net.ccbluex.liquidbounce.utils.bot

import com.oracle.truffle.js.builtins.RealmFunctionBuiltins.RealmFunction.owner
import net.ccbluex.liquidbounce.config.types.Configurable
import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.aiming.features.MovementCorrection
import net.ccbluex.liquidbounce.utils.bot.utils.CalculatePath
import net.ccbluex.liquidbounce.utils.client.player
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.math.toVec3d
import net.ccbluex.liquidbounce.utils.movement.MotionStatus
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

open class BotConfigurable(
    owner: EventListener,
    movementCorrection: MovementCorrection = MovementCorrection.SILENT,
    combatSpecific: Boolean = false,
    priority: Priority = Priority.NOT_IMPORTANT
) : Configurable("BotConfigurable") {
    private val rotationsConfigurable = RotationsConfigurable(owner, movementCorrection, combatSpecific)
    private val botPriority = priority

    init {
        tree(rotationsConfigurable)
    }

    private val cancelInRange by float("Cancel", 1f, 0f..10f)

    fun toBotPlanSimply(targetPosition: Vec3d,motionStatus: MotionStatus,provider : ClientModule): BotTargetPlan = BotTargetPlan(
        targetPositionList = mutableListOf(targetPosition),
        moveStatus = motionStatus,
        cancelInRange = cancelInRange,
        rotationsConfigurable = rotationsConfigurable,
        priority = botPriority,
        provider = provider)


    fun toBotPlan(targetPosition: Vec3d, motionStatus: MotionStatus, provider: ClientModule): BotTargetPlan {

        val pathResult = CalculatePath.findPath(player.pos, targetPosition)

        val pathResultList = pathResult.path.toMutableList()
        val finalPathResultList = mutableListOf<Vec3d>()
        for (i in pathResultList) {
            finalPathResultList.add(i.toVec3d())
        }
        return BotTargetPlan(
            targetPositionList = finalPathResultList,
            moveStatus = motionStatus,
            cancelInRange = cancelInRange,
            rotationsConfigurable = rotationsConfigurable,
            priority = botPriority,
            provider = provider
        )
    }

}
