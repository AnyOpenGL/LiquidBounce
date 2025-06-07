package net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools

import baritone.api.BaritoneAPI
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.ccbluex.liquidbounce.event.EventListener
import net.ccbluex.liquidbounce.event.events.GameTickEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.MCPFactory
import net.minecraft.block.Blocks
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object MCPToolBaritoneMine : EventListener, MCPFactory {
    private var mineRequest: Pair<String, Int>? = null

    override fun addComponent(server: Server) {
        server.addTool(
            name = "Mine",
            description = "Mine.It may take a while to reach the goal,you can use delay if there nothing to do",
            inputSchema =
                Tool.Input(
                    buildJsonObject {
                        put("mine block", "")
                        put("counter", "")
                    },
                ),
        ) { request ->

            val mineType =
                request.arguments
                    .get("mine block")
                    ?.toString()
                    ?.removeSurrounding("\"") ?: ""
            val counter =
                request.arguments
                    .get("counter")
                    .toString()
                    .removeSurrounding("\"")
                    .toInt()

            if (Registries.BLOCK.get(Identifier.tryParse(mineType)) == Blocks.AIR) {
                CallToolResult(
                    content = listOf(TextContent("Invalid mine block")),
                )
            }
            mineRequest = Pair(mineType, counter)

            CallToolResult(
                content = listOf(TextContent("Succeed to mine $mineType")),
            )
        }
    }

    @Suppress("unused")
    private val tickHandler =
        handler<GameTickEvent> {

            if (mineRequest != null) {
                BaritoneAPI
                    .getProvider()
                    .primaryBaritone
                    .mineProcess
                    .mineByName(mineRequest!!.second, mineRequest!!.first)

                mineRequest = null
            }
        }
}
