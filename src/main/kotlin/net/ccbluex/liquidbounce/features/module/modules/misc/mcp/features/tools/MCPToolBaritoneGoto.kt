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

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalBlock
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.utils.client.chat

object MCPToolBaritoneGoto : MCPFactory {
    val inputs =
        buildJsonObject {
            put("x", null)
            put("y", null)
            put("z", null)
        }

    override fun addComponent(server: Server) {
        server.addTool(
            name = "goto",
            description = "Goto someplace.It may take a while to reach the goal,you can use delay if there nothing to do",
            inputSchema = Tool.Input(inputs),
        ) { request ->

            var chatMessage = ""
            runCatching {
                val x =
                    request.arguments
                        .get("x")
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?.toInt() ?: throw Exception("x is null")
                val y =
                    request.arguments
                        .get("y")
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?.toInt() ?: throw Exception("y is null")
                val z =
                    request.arguments
                        .get("z")
                        ?.toString()
                        ?.removeSurrounding("\"")
                        ?.toInt() ?: throw Exception("z is null")

                BaritoneAPI
                    .getProvider()
                    .primaryBaritone.customGoalProcess
                    .setGoalAndPath(GoalBlock(x, y, z))
            }.onSuccess {
                chatMessage = "Succeed to goto"
            }.onFailure {
                chatMessage = "Failed to goto,throw:" + it.message
                chat(chatMessage)
            }
            CallToolResult(
                content = listOf(TextContent(chatMessage)),
            )
        }
    }
}
