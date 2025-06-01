package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.utils

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object MCPCommonMessageObject {
    val jsonObjectFormat =
        buildJsonObject {
            put("message", "")
        }
}
