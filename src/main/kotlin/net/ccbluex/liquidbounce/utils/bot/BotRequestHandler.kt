package net.ccbluex.liquidbounce.utils.bot

import net.ccbluex.liquidbounce.features.module.ClientModule
import net.minecraft.client.MinecraftClient
import java.util.concurrent.PriorityBlockingQueue

class BotRequestHandler<T> {

    private val activeRequests = PriorityBlockingQueue<Request<T>>(11, compareBy { -it.priority })

    fun request(request: Request<T>) {
        // we remove all requests provided by module on new request
        activeRequests.removeAll { it.provider == request.provider }
        activeRequests.add(request)
    }

    fun removeRequestValue(provider: ClientModule) {
        activeRequests.removeAll { it.provider == provider }
    }

    fun getActiveRequestValue(): T? {
        var top = activeRequests.peek() ?: return null

        if (MinecraftClient.getInstance()?.isOnThread != false) {
            // we remove all outdated requests here
            while (!top.provider.running) {
                activeRequests.remove()
                top = activeRequests.peek() ?: return null
            }
        }

        return top.value
    }

    /**
     * A requested state of the system.
     *
     * Note: A request is deleted when its corresponding module is disabled.
     *
     * @param expiresIn in how many ticks units should this request expire?
     * @param priority higher = higher priority
     * @param provider module which requested value
     */
    class Request<T>(
        val priority: Int, val provider: ClientModule, val value: T
    )
}
