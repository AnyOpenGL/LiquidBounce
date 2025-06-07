package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.utils.MCPListStringToContextTranslator.toContextList
import net.ccbluex.liquidbounce.utils.client.world

object MCPToolGetWorldStatus : MCPFactory {
    override fun addComponent(server: Server) {
        server.addTool(
            name = "Get world status",
            description = "Get world status",
            inputSchema = Tool.Input(),
        ) { request ->

            val worldStatusList =
                listOf<String>(
                    "world tick time:" + world.time,
                    "world time" + if (world.isDay) "day" else "night",
                    "world weather:" + if (world.isRaining) "raining" else "sunny",
                    "world difficulty:" + world.difficulty,
                    "world spawn point:" + world.spawnPos,
                    "world spawn angle:" + world.spawnAngle,
                )
            CallToolResult(
                content = worldStatusList.toContextList(),
            )
        }
    }
}
