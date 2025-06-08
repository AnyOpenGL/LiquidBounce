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
package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.resources

import io.modelcontextprotocol.kotlin.sdk.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.TextResourceContents
import io.modelcontextprotocol.kotlin.sdk.server.Server
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory

object MCPResourceLiquidbounceOfficalWebdsite : MCPFactory {
    override fun addComponent(server: Server) {
        server.addResource(
            uri = "https://liquidbounce.net/",
            name = "liquidbounce official website",
            description = "liquidbounce official website",
            mimeType = "text/html",
        ) { request ->
            ReadResourceResult(
                contents =
                    listOf(
                        TextResourceContents("Placeholder content for ${request.uri}", request.uri, "text/html"),
                    ),
            )
        }
    }
}
