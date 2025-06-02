package net.ccbluex.liquidbounce.features.module.modules.misc.mcp

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import io.ktor.util.collections.ConcurrentMap
import io.modelcontextprotocol.kotlin.sdk.GetPromptResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.PromptArgument
import io.modelcontextprotocol.kotlin.sdk.PromptMessage
import io.modelcontextprotocol.kotlin.sdk.Role
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.SseServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.ClientModule
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools.MCPToolBaritoneGoto
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools.MCPToolChatWithClient
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools.MCPToolGetPlayerStatus
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools.MCPToolGetServerStatus
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools.MCPToolGetWorldStatus
import net.ccbluex.liquidbounce.features.module.modules.misc.mcp.features.tools.MCPToolSendServerMessage
import net.ccbluex.liquidbounce.utils.client.chat

object ModuleMCP : ClientModule("MCP", Category.MISC) {
    private val mcpPort by text("Port", "8080")

    val server by lazy {
        configureServer()
    }

    val mcpToolsList =
        listOf<MCPFactory>(
            MCPToolChatWithClient,
            MCPToolSendServerMessage,
            MCPToolGetPlayerStatus,
            MCPToolGetServerStatus,
            MCPToolGetWorldStatus,
            MCPToolBaritoneGoto,
        )

    override fun enable() {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                runSseMcpServerWithPlainConfiguration(mcpPort.toInt())
            }.onSuccess {
                chat("MCP server started on port $mcpPort")
            }.onFailure {
                chat("MCP server start failed")
            }
        }
    }

    override fun disable() {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                server.close()
            }.onSuccess {
                chat("MCP server closed on port")
            }.onFailure {
                chat("MCP server closed failed")
            }
        }
    }

    fun configureServer(): Server {
        val server =
            Server(
                Implementation(
                    name = "Liquidbounce model context protocol",
                    version = "0.1.0",
                ),
                ServerOptions(
                    capabilities =
                        ServerCapabilities(
                            prompts = ServerCapabilities.Prompts(listChanged = true),
                            resources = ServerCapabilities.Resources(subscribe = true, listChanged = true),
                            tools = ServerCapabilities.Tools(listChanged = true),
                        ),
                ),
            )

        server.addPrompt(
            name = "Liquidbounce model context protocol",
            description =
                "Liquidbounce model context protocol",
            arguments =
                listOf(
                    PromptArgument(
                        name = "Liquidbounce model context protocol",
                        description = "Liquidbounce model context protocol",
                        required = true,
                    ),
                ),
        ) { request ->
            GetPromptResult(
                "Description for ${request.name}",
                messages =
                    listOf(
                        PromptMessage(
                            role = Role.user,
                            content =
                                TextContent(
                                    "Liquidbounce model context protocol <name>${request.arguments?.get("Project Name")}</name>",
                                ),
                        ),
                    ),
            )
        }

        mcpToolsList.forEach {
            it.addTool(server)
        }
        return server
    }

    suspend fun runSseMcpServerWithPlainConfiguration(port: Int) {
        val servers = ConcurrentMap<String, Server>()
        println("Starting sse server on port $port. ")
        println("Use inspector to connect to the http://localhost:$port/sse")

        embeddedServer(CIO, host = "0.0.0.0", port = port) {
            install(SSE)
            routing {
                sse("/sse") {
                    val transport = SseServerTransport("/message", this)
                    val server = server

                    // For SSE, you can also add prompts/tools/resources if needed:
                    // server.addTool(...), server.addPrompt(...), server.addResource(...)

                    servers[transport.sessionId] = server

                    server.onClose {
                        println("Server closed")
                        servers.remove(transport.sessionId)
                    }

                    server.connect(transport)
                }
                post("/message") {
                    println("Received Message")
                    val sessionId: String = call.request.queryParameters["sessionId"]!!
                    val transport = servers[sessionId]?.transport as? SseServerTransport
                    if (transport == null) {
                        call.respond(HttpStatusCode.Companion.NotFound, "Session not found")
                        return@post
                    }

                    transport.handlePostMessage(call)
                }
            }
        }.start(true)
    }
}
