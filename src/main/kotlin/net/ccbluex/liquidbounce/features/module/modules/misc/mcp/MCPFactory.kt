package net.ccbluex.liquidbounce.features.module.modules.misc.mcp

import io.modelcontextprotocol.kotlin.sdk.server.Server

interface MCPFactory {
    fun addComponent(server: Server)
}
