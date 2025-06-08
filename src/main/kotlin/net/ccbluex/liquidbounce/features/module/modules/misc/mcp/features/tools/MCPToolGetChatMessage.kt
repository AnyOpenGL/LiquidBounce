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
package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.ChatReceiveEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.utils.MCPListStringToContextTranslator.toContextList

object MCPToolGetChatMessage : EventListener, MCPFactory {
    val chatMessagesBuffer = mutableListOf<String>()

    override fun addComponent(server: Server) {
        server.addTool(
            name = "Get Chat Message",
            description = "Get Chat Message.Empty JSONObject required.",
            inputSchema =
                Tool.Input(),
        ) { request ->

            CallToolResult(
                content = chatMessagesBuffer.toContextList(),
            )
        }
    }

    @Suppress("unused")
    private val chatMessagesHandler =
        handler<ChatReceiveEvent> { event ->
            chatMessagesBuffer.add(event.message)

            if (chatMessagesBuffer.size > 100) {
                chatMessagesBuffer.subList(0, 50).clear()
            }
        }
}
