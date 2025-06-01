package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.utils

import io.modelcontextprotocol.kotlin.sdk.TextContent

object MCPListStringToContextTranslator {
    fun List<String>.toContextList(): List<TextContent> = this.map { TextContent(it) }.toList()
}
