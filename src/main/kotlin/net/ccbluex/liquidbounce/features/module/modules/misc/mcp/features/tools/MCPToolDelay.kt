package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory

object MCPToolDelay : MCPFactory {
    override fun addComponent(server: Server) {
        server.addTool(
            name = "Delay",
            description = "Delay if there are no thing to do.Delay in milliseconds",
            inputSchema =
                Tool.Input(
                    buildJsonObject {
                        put("delay", null)
                    },
                ),
        ) { request ->
            delay(
                request.arguments
                    .get("delay")
                    ?.toString()
                    ?.toLong() ?: 0L,
            )

            CallToolResult(
                content = listOf(TextContent("Delay successfully.")),
            )
        }
    }
}
