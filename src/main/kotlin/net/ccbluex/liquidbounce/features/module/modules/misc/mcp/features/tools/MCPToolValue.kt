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
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.ModuleManager
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleClickGui

object MCPToolValue : MCPFactory {
    override fun addComponent(server: Server) {
        server.addTool(
            name = "Set Value",
            description = "Modified Value",
            inputSchema =
                Tool.Input(
                    buildJsonObject {
                        put("moduleName", "")
                        put("valueName", "")
                        put("valueString", "")
                    },
                ),
        ) { request ->

            val module = request.arguments.get("moduleName") as ClientModule
            val valueName = request.arguments.get("valueName") as String
            val valueString = request.arguments.get("value") as String

            val value =
                module
                    .getContainedValuesRecursively()
                    .filter { !it.name.equals("Bind", true) }
                    .firstOrNull { it.name.equals(valueName, true) }
                    ?: throw Exception("Value not found")

            try {
                value.setByString(valueString)
                ModuleClickGui.reloadView()
                CallToolResult(
                    content = listOf(TextContent("Value set successfully.")),
                )
            } catch (e: Exception) {
                CallToolResult(content = listOf(TextContent("valueError$valueName${e.message}")))
            }
        }

        server.addTool(
            name = "Get Module List",
            description = "Get Module List",
            inputSchema =
                Tool.Input(),
        ) { request ->
            val moduleList =
                ModuleManager.getModules().map { TextContent(it.name) }

            CallToolResult(
                content = listOf(TextContent("Module List:$moduleList")),
            )
        }

        server.addTool(
            name = "Get Value List in Module",
            description = "Get Value List in Module",
            inputSchema =
                Tool.Input(
                    buildJsonObject {
                        put("moduleName", "")
                    },
                ),
        ) { request ->
            val module = request.arguments.get("moduleName") as ClientModule
            val valueList =
                module
                    .getContainedValuesRecursively()
                    .filter { !it.name.equals("Bind", true) }
                    .map { it.name }

            CallToolResult(
                content = listOf(TextContent("Value List:$valueList")),
            )
        }
    }
}
