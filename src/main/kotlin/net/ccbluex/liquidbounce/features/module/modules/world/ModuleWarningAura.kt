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

package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.events.OverlayRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.lang.translation
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.ccbluex.liquidbounce.utils.combat.getEntitiesInCuboid
import net.ccbluex.liquidbounce.utils.math.toFixed
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Colors

object ModuleWarningAura : ClientModule("WarningAura", Category.WORLD, disableOnQuit = true) {

    private val onlyUnseen by boolean("OnlyUnseen", false)
    private val onlyPlayers by boolean("OnlyPlayers", false)
    private val autoDisconnect by boolean("AutoDisconnect", false)
    private val distance by float("Distance", 4.0f, 1.0f..6.0f)

    private val targetTracker = tree(TargetTracker())

    private var targetDistance: Float? = null

    override fun onDisabled() {
        targetDistance = null
    }

    @Suppress("unused")
    private val tickHandler = handler<GameTickEvent> {
        val entities = world.getEntitiesInCuboid(player.eyePos, distance.toDouble()) {
            it !== player && (if (onlyPlayers) it is PlayerEntity else it is LivingEntity)
                && (!onlyUnseen || player.canSee(it))
        }
        entities.sortBy { player.squaredDistanceTo(it) }

        val closestEntity = entities.firstOrNull {
            targetTracker.validate(it as LivingEntity)
        }

        if (closestEntity == null) {
            targetDistance = null
        } else {
            if (autoDisconnect) {
                world.disconnect()
            }
            targetDistance = closestEntity.distanceTo(player)
        }
    }

    @Suppress("unused")
    private val renderHandler = handler<OverlayRenderEvent> { event ->
        val targetDistance = targetDistance ?: return@handler

        val text = translation("liquidbounce.module.warningAura.warning")
            .append(targetDistance.toFixed(1))
        event.context.drawText(textRenderer,
            text,
            (event.context.scaledWindowWidth - textRenderer.getWidth(text)) / 2,
            (event.context.scaledWindowHeight - textRenderer.fontHeight) / 2,
            Colors.RED,
            false
        )
    }
}
