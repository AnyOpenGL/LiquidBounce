/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.config.types.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.events.InteractItemEvent
import net.ccbluex.liquidbounce.event.events.RotationUpdateEvent
import net.ccbluex.liquidbounce.event.events.WorldRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.lang.translation
import net.ccbluex.liquidbounce.render.*
import net.ccbluex.liquidbounce.render.engine.Color4b
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.aiming.data.Rotation
import net.ccbluex.liquidbounce.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.entity.ConstantPositionExtrapolation
import net.ccbluex.liquidbounce.utils.entity.PlayerSimulationCache
import net.ccbluex.liquidbounce.utils.entity.SimulatedPlayerSnapshot
import net.ccbluex.liquidbounce.utils.inventory.HotbarItemSlot
import net.ccbluex.liquidbounce.utils.inventory.Slots
import net.ccbluex.liquidbounce.utils.inventory.useHotbarSlotOrOffhand
import net.ccbluex.liquidbounce.utils.kotlin.Priority
import net.ccbluex.liquidbounce.utils.math.toBlockPos
import net.ccbluex.liquidbounce.utils.math.toVec3d
import net.ccbluex.liquidbounce.utils.render.trajectory.TrajectoryInfo
import net.minecraft.entity.EntityDimensions
import net.minecraft.item.Items
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d

/**
 * EasyPearl module
 *
 * Throw pearl to where you are looking at.
 **/
@Suppress("MagicNumber")
object ModuleEasyPearl : ClientModule(
    "EasyPearl",
    Category.MISC,
    aliases = arrayOf("Pearl Helper", "Pearl Assist", "Pearl TP"),
) {
    private val aimOffThreshold by float("AimOffThreshold", 2f, 0.5f..10f)
    private val reachableCheck by boolean("ReachableCheck", true)

    private object Predict : ToggleableConfigurable(this, "Predict", true) {
        val predictTicks by int("PredictTicks", 1, 1..5)
    }

    private val rotation = tree(RotationsConfigurable(this))
    private var targetPosition: Vec3d? = null
    private var isThrow = false

    private val enderPearlSlot: HotbarItemSlot?
        get() = Slots.OffhandWithHotbar.findSlot(Items.ENDER_PEARL)

    private val simulatedPlayer: SimulatedPlayerSnapshot
        get() =
            PlayerSimulationCache
                .getSimulationForLocalPlayer()
                .getSnapshotAt(if (Predict.enabled) Predict.predictTicks else 0)

    init {
        tree(Predict)
    }

    @Suppress("unused")
    private val interactItemHandler =
        handler<InteractItemEvent> { event ->
            if (!holdingPearl() || !mc.options.useKey.isPressed) {
                return@handler
            }

            // While reachable check is enabled, we will check if the player is looking at a block father than pearl can reach
            if (reachableCheck &&
                getTargetRotation(getPositionPlayerLookAt()) == null &&
                player
                    .raycast(
                        1000.0,
                        0.0f,
                        false,
                    ).type != HitResult.Type.MISS
            ) {
                chat(translation("liquidbounce.module.easyPearl.messages.noInReachWarning"))
                event.cancelEvent()
                return@handler
            }

            // if target position != null, that means we throw the pearl before,so we should duel it
            if (targetPosition != null) {
                if (isThrow && isRotationDone(targetPosition!!)) {
                    isThrow = false
                    return@handler
                }
                return@handler
            }

            // if target position== null and isThrow = false, that means we are throwing the pearl by hand, so we should set the target position
            targetPosition = getPositionPlayerLookAt()

            // check if we are rotating to the target position correctly
            if (isRotationDone(targetPosition!!)) {
                targetPosition = null
                isThrow = false
            } else {
                event.cancelEvent()
            }
        }

    @Suppress("unused")
    private val rotationHandler =
        handler<RotationUpdateEvent> {
            /**
             * handler for rotation update event,and rotate to the target rotation
             */
            val finalTargetRotation = getTargetRotation(targetPosition ?: return@handler) ?: return@handler

            RotationManager.setRotationTarget(
                rotation.toRotationTarget(finalTargetRotation),
                Priority.IMPORTANT_FOR_PLAYER_LIFE,
                this@ModuleEasyPearl,
            )
        }

    @Suppress("unused")
    private val tickHandler =
        tickHandler {
            /**
             * handler for tick event,and check if we are rotating to the target rotation correctly,if yes,throw the pearl
             */
            val currentTargetRotation = getTargetRotation(targetPosition ?: return@tickHandler) ?: return@tickHandler

            if (isRotationDone(targetPosition ?: return@tickHandler)) {
                useHotbarSlotOrOffhand(enderPearlSlot ?: return@tickHandler, 0, currentTargetRotation.yaw, currentTargetRotation.pitch)
                targetPosition = null
                isThrow = true
            }
        }

    @Suppress("unused")
    private val worldRenderHandler =
        handler<WorldRenderEvent> { event ->
            /**
             * handler for world render event,and render the target position
             */
            if (!holdingPearl()) return@handler

            val matrixStack = event.matrixStack
            val blockPos = getPositionPlayerLookAt().toBlockPos()
            val state = blockPos.getState() ?: return@handler

            renderEnvironmentForWorld(matrixStack) {
                withDisabledCull {
                    val color =
                        if (getTargetRotation(getPositionPlayerLookAt()) != null) {
                            Color4b(0x20, 0xC2, 0x06)
                        } else {
                            Color4b(0xD7, 0x09, 0x09)
                        }

                    val baseColor = color.with(a = 50)
                    val transparentColor = baseColor.with(a = 0)
                    val outlineColor = color.with(a = 100)

                    withPositionRelativeToCamera(blockPos.toVec3d()) {
                        withColor(baseColor) {
                            drawOutlinedBox(FULL_BOX)
                        }
                        withColor(outlineColor) {
                            drawOutlinedBox(FULL_BOX)
                        }
                        drawGradientSides(1.0, baseColor, transparentColor, FULL_BOX)
                    }
                }
            }
        }

    private fun holdingPearl() = player.mainHandStack.item == Items.ENDER_PEARL || player.offHandStack.item == Items.ENDER_PEARL

    /**
     * check if we are rotating to the target rotation correctly
     * @param targetPosition the target position
     * @return true if we are rotating to the target rotation correctly,false otherwise
     */
    @Suppress("ReturnCount")
    private fun isRotationDone(targetPosition: Vec3d): Boolean {
        return RotationManager.serverRotation.angleTo(
            getTargetRotation(targetPosition) ?: return true,
        ) <= aimOffThreshold
    }

    /**
     * get the position player look at
     * @return the position player look at
     */
    private fun getPositionPlayerLookAt(): Vec3d = player.raycast(1000.0, 0.0f, false).pos

    /**
     * get the target rotation for the target position
     * @param targetPosition the target position
     * @return the target rotation for the target position
     */
    private fun getTargetRotation(targetPosition: Vec3d): Rotation? =
        SituationalProjectileAngleCalculator.calculateAngleFor(
            TrajectoryInfo.GENERIC,
            sourcePos = simulatedPlayer.pos,
            targetPosFunction = ConstantPositionExtrapolation(targetPosition),
            targetShape = EntityDimensions.fixed(1.0F, 0.0F),
        )
}
