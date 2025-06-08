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
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.utils.MCPCommonMessageObject.jsonObjectFormat
import net.ccbluex.liquidbounce.utils.client.chat

object MCPToolChatWithClient : MCPFactory {
    override fun addComponent(server: Server) {
        server.addTool(
            name = "Chat with client",
            description = "Chat with client",
            inputSchema = Tool.Input(jsonObjectFormat),
        ) { request ->

            val message = request.arguments.get("message")?.toString() ?: ""

            val callToolResult: MutableList<TextContent> = mutableListOf()
            if (message.equals("")) {
                callToolResult.add(TextContent("Empty message,please send again"))
            } else {
                callToolResult.add(TextContent("Message send successfully."))
            }
            chat(message)
            CallToolResult(
                content = listOf(TextContent("Message send successfully.")),
            )
        }
    }
}
