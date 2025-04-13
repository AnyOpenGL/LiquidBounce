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
import net.ccbluex.liquidbounce.render.FULL_BOX
import net.ccbluex.liquidbounce.render.drawGradientSides
import net.ccbluex.liquidbounce.render.drawOutlinedBox
import net.ccbluex.liquidbounce.render.engine.Color4b
import net.ccbluex.liquidbounce.render.renderEnvironmentForWorld
import net.ccbluex.liquidbounce.render.withColor
import net.ccbluex.liquidbounce.render.withDisabledCull
import net.ccbluex.liquidbounce.render.withPositionRelativeToCamera
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.RotationsConfigurable
import net.ccbluex.liquidbounce.utils.aiming.data.Rotation
import net.ccbluex.liquidbounce.utils.aiming.projectiles.SituationalProjectileAngleCalculator
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.entity.ConstantPositionExtrapolation
import net.ccbluex.liquidbounce.utils.entity.SimulatedPlayer
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


@Suppress("MagicNumber")
object ModuleEasyPearl : ClientModule("EasyPearl", Category.MISC) {
    private val aimOffThreshold by float("AimOffThreshold", 2f, 0.5f..10f)
    private val onlyInReach by boolean("OnlyInReach", true)
    private val importantForPlayerLife by boolean("ImportantForPlayerLife", false)

    private object Predict : ToggleableConfigurable(this, "Predict", true) {
        val predictTicks by int("PredictTicks", 1, 1..5)
    }

    private val rotation = tree(RotationsConfigurable(this))
    private var targetPosition: Vec3d? = null
    private var isThrow = false

    private val enderPearlSlot: HotbarItemSlot?
        get() = Slots.OffhandWithHotbar.findSlot(Items.ENDER_PEARL)

    init {
        tree(Predict)
    }

    @Suppress("unused")
    private val interactItemHandler = handler<InteractItemEvent> { event ->
        if (player.inventory.mainHandStack.item != Items.ENDER_PEARL || !mc.options.useKey.isPressed) {
            return@handler
        }

         if (onlyInReach && getTargetRotation(getPositionPlayerLookAt()) == null && player.raycast(
                1000.0,
                0.0f,
                false
            ).type != HitResult.Type.MISS && !isThrow
        ) {
            chat(translation("liquidbounce.module.easyPearl.messages.noInReachWarning"))
            event.cancelEvent()
            return@handler
        }

        if (targetPosition != null) {
            if (isThrow && isRotationDone(targetPosition!!)) {
                isThrow = false
                return@handler
            }
        }

        targetPosition = getPositionPlayerLookAt()

        if (isRotationDone(targetPosition!!)) {
            targetPosition = null
            isThrow = false
        } else {
            event.cancelEvent()
        }
    }

    @Suppress("unused")
    private val rotationHandler = handler<RotationUpdateEvent> {
        val currentTargetPosition = targetPosition ?: return@handler
        val finalTargetRotation = getTargetRotation(currentTargetPosition) ?: return@handler
        val priority = if (importantForPlayerLife) {
            Priority.IMPORTANT_FOR_PLAYER_LIFE
        } else {
            Priority.IMPORTANT_FOR_USAGE_3
        }
        RotationManager.setRotationTarget(
            rotation.toRotationTarget(finalTargetRotation),
            priority,
            this@ModuleEasyPearl
        )
    }

    @Suppress("unused")
    private val tickHandler = tickHandler {
        val currentPosition = targetPosition ?: return@tickHandler
        val currentTargetRotation = getTargetRotation(currentPosition) ?: return@tickHandler
        val slot = enderPearlSlot ?: return@tickHandler

        if (isRotationDone(currentPosition)) {
            useHotbarSlotOrOffhand(slot, 0, currentTargetRotation.yaw, currentTargetRotation.pitch)
            targetPosition = null
            isThrow = true
        }
    }

    @Suppress("unused")
    private val worldRenderHandler = handler<WorldRenderEvent> { event ->
        if (player.inventory.mainHandStack.item != Items.ENDER_PEARL) return@handler
        val matrixStack = event.matrixStack
        val position = getPositionPlayerLookAt()
        val blockPos = position.toBlockPos()
        val state = blockPos.getState() ?: return@handler

        renderEnvironmentForWorld(matrixStack) {
            withDisabledCull {
                val color = if (getTargetRotation(getPositionPlayerLookAt()) != null) {
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

    @Suppress("ReturnCount")
    private fun isRotationDone(targetPosition: Vec3d): Boolean {
        val currentTargetRotation = getTargetRotation(targetPosition) ?: return true
        val rotationDifference = RotationManager.serverRotation.angleTo(currentTargetRotation)
        return rotationDifference <= aimOffThreshold
    }

    private fun getPositionPlayerLookAt(): Vec3d = player.raycast(1000.0, 0.0f, false).pos

    private fun getTargetRotation(targetPosition: Vec3d): Rotation? {
        if (Predict.enabled) {
            val nextTickPlayer =
                SimulatedPlayer.fromClientPlayer(SimulatedPlayer.SimulatedPlayerInput.guessInput(player))
            repeat(Predict.predictTicks - 1) {
                nextTickPlayer.tick()
            }
            return SituationalProjectileAngleCalculator.calculateAngleFor(
                TrajectoryInfo.GENERIC,
                sourcePos = nextTickPlayer.pos,
                targetPosFunction = ConstantPositionExtrapolation(targetPosition),
                targetShape = EntityDimensions.fixed(1.0F, 0.0F)
            )
        }
        return SituationalProjectileAngleCalculator.calculateAngleForStaticTarget(
            TrajectoryInfo.GENERIC,
            targetPosition,
            EntityDimensions.fixed(1.0F, 0.0F)
        )
    }
}
