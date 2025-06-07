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
    var chatMessagesBuffer = mutableListOf<String>()

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
        }
}
