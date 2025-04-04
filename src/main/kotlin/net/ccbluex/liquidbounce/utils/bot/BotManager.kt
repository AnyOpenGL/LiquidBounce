package net.ccbluex.liquidbounce.utils.bot

import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.aiming.data.Rotation
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.player
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.movement.MotionStatus
import net.ccbluex.liquidbounce.utils.movement.MovementClass
import net.ccbluex.liquidbounce.utils.movement.MovementManager
import net.minecraft.util.math.Vec3d

object BotManager : EventListener {
    private val targetPositionPlan
        get() = targetPositionPlanHandler.getActiveRequestValue()
    private var targetPositionPlanHandler = BotRequestHandler<BotTargetPlan>()

    private val targetRotation : Rotation?
        get() {
            if (targetPositionPlan == null) return null
            val currentTargetPositionPlan = targetPositionPlan!!
            val currentTargetPosition = currentTargetPositionPlan.targetPositionList.first()
            currentTargetPosition.y = player.y
            val currentLookingAtPosition = Vec3d(currentTargetPosition.x, player.y, currentTargetPosition.z)
            return Rotation.lookingAt(currentLookingAtPosition, player.eyePos)
        }


    fun updatePositionsList(botTargetPlan: BotTargetPlan) {
        var nextBotTargetPlan = botTargetPlan
        nextBotTargetPlan.targetPositionList.removeFirst()
        if(nextBotTargetPlan.targetPositionList.isEmpty()){
            targetPositionPlanHandler.removeRequestValue(nextBotTargetPlan.provider)
            return
        }
        BotManager.setTargetPosition(nextBotTargetPlan)
    }

    fun setTargetPosition(botTargetList: BotTargetPlan) {
        targetPositionPlanHandler.request(
            BotRequestHandler.Request(
                botTargetList.priority.priority,
                botTargetList.provider,
                botTargetList
            )
        )
    }

    fun update() {
        if (targetPositionPlan == null) return
        val currentTargetPositionPlan = targetPositionPlan!!
        val currentTargetPosition = currentTargetPositionPlan.targetPositionList.first()
        currentTargetPosition.y = player.y


        if (RotationManager.serverRotation.angleTo(targetRotation ?: return ) > 10f) return

        /**
         * We update Movement every tick,so atleastTick is 1.
         */
        MovementManager.setMovement(
            MovementClass(
                0.0,
                false,
                currentTargetPositionPlan.moveStatus,
            ),
            1,
            currentTargetPositionPlan.priority,
            currentTargetPositionPlan.provider
        )


        if (player.pos.distanceTo(currentTargetPosition) < currentTargetPositionPlan.cancelInRange){
            updatePositionsList(currentTargetPositionPlan)
            chat("[Bot] Reached goal")
        }

    }

    private val tickHandler = handler<GameTickEvent> {
        update()
    }

    private val onRotation = handler<RotationUpdateEvent>{
        if (targetPositionPlan == null) return@handler
        val currentTargetPositionPlan = targetPositionPlan!!
        val currentTargetPosition = currentTargetPositionPlan.targetPositionList.first()
        currentTargetPosition.y = player.y
        val currentLookingAtPosition = Vec3d(currentTargetPosition.x, player.y, currentTargetPosition.z)
        RotationManager.setRotationTarget(
            currentTargetPositionPlan.rotationsConfigurable.toRotationTarget(
                Rotation.lookingAt(currentLookingAtPosition, player.eyePos)
            ),
            currentTargetPositionPlan.priority,
            currentTargetPositionPlan.provider
        )
    }

}


data class BotTargetPlan(
    var targetPositionList: MutableList<Vec3d>,
    val moveStatus: MotionStatus,
    val cancelInRange: Float,
    val rotationsConfigurable: RotationsConfigurable,
    val priority: Priority,
    val provider: ClientModule
) {

}
