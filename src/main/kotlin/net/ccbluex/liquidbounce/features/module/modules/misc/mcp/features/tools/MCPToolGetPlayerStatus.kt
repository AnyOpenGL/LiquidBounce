package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.utils.MCPListStringToContextTranslator.toContextList
import net.ccbluex.liquidbounce.utils.client.player

object MCPToolGetPlayerStatus : MCPFactory {
    val jsonObjectFormat =
        buildJsonObject {
        }

    override fun addTool(server: Server) {
        server.addTool(
            name = "Get player status",
            description = "Get player status",
            inputSchema = Tool.Input(jsonObjectFormat),
        ) { request ->

            val playerStatus =
                listOf<String>(
                    "name:" + player.name.string,
                    "position" + player.pos.toString(),
                    "health:" + player.health.toString(),
                    "hunger:" + player.hungerManager.foodLevel.toString(),
                    "saturation:" + player.hungerManager.saturationLevel.toString(),
                    "mainHandItem:" + player.mainHandStack.name,
                )

            CallToolResult(
                content = playerStatus.toContextList(),
            )
        }
    }
}
