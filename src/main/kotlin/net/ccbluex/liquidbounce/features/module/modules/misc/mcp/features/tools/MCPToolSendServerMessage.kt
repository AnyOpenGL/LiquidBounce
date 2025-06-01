package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.ModuleMCP.jsonObjectFormat
import net.ccbluex.liquidbounce.utils.client.network

object MCPToolSendServerMessage : MCPFactory {
    override fun addTool(server: Server) {
        server.addTool(
            name = "Send message to server",
            description = "Send message to server,then other players can see what you say",
            inputSchema = Tool.Input(jsonObjectFormat),
        ) { request ->

            var message = request.arguments.get("message")?.toString() ?: ""

            message = message.removeSurrounding("\"")
            val callToolResult: MutableList<TextContent> = mutableListOf()
            if (message.equals("")) {
                callToolResult.add(TextContent("Empty message,please send again"))
            } else {
                callToolResult.add(TextContent("Message send successfully."))
            }
            network.sendChatMessage(message)
            CallToolResult(
                content = listOf(TextContent("Message send successfully.")),
            )
        }
    }
}
