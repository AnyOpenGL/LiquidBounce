package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import baritone.api.BaritoneAPI
import baritone.api.pathing.goals.GoalBlock
import com.llamalad7.mixinextras.utils.Blackboard.put
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.utils.client.chat

object MCPToolBaritoneGoto : MCPFactory {
    override fun addTool(server: Server) {
        server.addTool(
            name = "goto",
            description = "goto someplace",
            inputSchema =
                Tool.Input(
                    buildJsonObject {
                        put("x", 0)
                        put("y", 0)
                        put("z", 0)
                    },
                ),
        ) { request ->

            var chatMessage = ""
            runCatching {
                val x =
                    request.arguments
                        .get("x")
                        ?.toString()
                        ?.toInt() ?: throw Exception("x is null")
                val y =
                    request.arguments
                        .get("y")
                        ?.toString()
                        ?.toInt() ?: throw Exception("y is null")
                val z =
                    request.arguments
                        .get("z")
                        ?.toString()
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
