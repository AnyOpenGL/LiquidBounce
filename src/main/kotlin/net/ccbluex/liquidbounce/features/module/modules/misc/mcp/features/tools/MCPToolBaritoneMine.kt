package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import baritone.api.BaritoneAPI
import baritone.api.utils.BlockOptionalMeta
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory

object MCPToolBaritoneMine : MCPFactory {
    override fun addTool(server: Server) {
        server.addTool(
            name = "Mine",
            description = "Mine",
            inputSchema =
                Tool.Input(
                    buildJsonObject {
                        put("mine type", "")
                        put("counter", "")
                    },
                ),
        ) { request ->

            val mineType =
                request.arguments
                    .get("mine type")
                    ?.toString()
                    ?.removeSurrounding("\"") ?: ""
            val counter =
                request.arguments
                    .get("counter")
                    .toString()
                    .removeSurrounding("\"")
                    .toInt()

            BaritoneAPI
                .getProvider()
                .primaryBaritone.mineProcess
                .mine(counter, BlockOptionalMeta(mineType))
            CallToolResult(
                content = listOf(TextContent("Succeed to mine $mineType")),
            )
        }
    }
}
