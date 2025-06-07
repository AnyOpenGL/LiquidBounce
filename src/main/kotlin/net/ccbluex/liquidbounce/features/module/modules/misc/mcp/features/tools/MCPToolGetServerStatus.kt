package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.utils.MCPListStringToContextTranslator.toContextList
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.client.world

object MCPToolGetServerStatus : MCPFactory {
    override fun addComponent(server: Server) {
        server.addTool(
            name = "Get server status",
            description = "Get server status",
            inputSchema = Tool.Input(),
        ) { request ->

            val serverStatus: MutableList<String> = mutableListOf()
            if (mc.isInSingleplayer) {
                serverStatus.add("You are not in a server")
            } else {
                serverStatus.add("Server name:" + mc.server!!.name)
                serverStatus.add("Server ip:" + mc.server!!.serverIp)
                serverStatus.add("Server port:" + mc.server!!.serverPort)
                serverStatus.add("Server version:" + mc.server!!.version)
                serverStatus.add("Server players names:" + world.players.joinToString(",") { it.name.string })
            }
            CallToolResult(
                content = serverStatus.toContextList(),
            )
        }
    }
}
