package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.ModuleMCP.jsonObjectFormat
import net.ccbluex.liquidbounce.utils.client.chat

object MCPToolChatWithClient : MCPFactory {
    override fun addTool(server: Server) {
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
