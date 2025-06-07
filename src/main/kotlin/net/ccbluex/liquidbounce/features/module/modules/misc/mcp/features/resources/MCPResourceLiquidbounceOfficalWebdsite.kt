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
