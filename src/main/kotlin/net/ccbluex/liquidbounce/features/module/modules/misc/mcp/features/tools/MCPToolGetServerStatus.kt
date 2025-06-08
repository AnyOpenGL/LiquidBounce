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
