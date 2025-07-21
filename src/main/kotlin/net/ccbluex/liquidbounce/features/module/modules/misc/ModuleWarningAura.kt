package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.events.OverlayRenderEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.tickHandler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.lang.translation
import net.ccbluex.liquidbounce.utils.combat.TargetTracker
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Colors

object ModuleWarningAura : ClientModule("WarningAura", Category.MISC) {

    private val onlyUnseen by boolean("OnlyUnseen", false)
    private val onlyPlayers by boolean("OnlyPlayers", false)
    private val distance by float("Distance", 4.0f, 1.0f..6.0f)

    private val targetTracker = TargetTracker()

    private var needWarning = false
    private var targetDistance: Float? = null

    @Suppress("unused")
    private val tickHandler = tickHandler {
        val entities = world.entities.filter { it.distanceTo(player) < distance && it != player }
            .filter { if (onlyPlayers) it is PlayerEntity else it is LivingEntity }
            .filter { if (onlyUnseen) !player.canSee(it) else true }
            .sortedBy { it.distanceTo(player) }

        entities.forEach { it ->
            if (targetTracker.validate(it as LivingEntity)) {
                needWarning = true
                targetDistance = it.distanceTo(player)
                return@tickHandler
            }
        }

        needWarning = false
        targetDistance = null
    }

    @Suppress("unused")
    private val renderHandler = handler<OverlayRenderEvent> { event ->
        if (!needWarning || targetDistance == null) return@handler
        val text = translation("liquidbounce.module.warningaura.warning").string + targetDistance
        event.context.drawText(textRenderer,
            text,
            (event.context.scaledWindowWidth - textRenderer.getWidth(text)) / 2,
            (event.context.scaledWindowHeight - textRenderer.fontHeight) / 2,
            Colors.RED,
            false
        )
    }
}
