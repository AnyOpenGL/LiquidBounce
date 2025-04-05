package net.ccbluex.liquidbounce.utils.bot.utils

import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.aiming.data.Rotation
import net.ccbluex.liquidbounce.utils.aiming.data.RotationDelta
import net.ccbluex.liquidbounce.utils.aiming.utils.RotationUtil
import net.ccbluex.liquidbounce.utils.block.getState
import net.ccbluex.liquidbounce.utils.client.player
import net.ccbluex.liquidbounce.utils.math.copy
import net.ccbluex.liquidbounce.utils.math.toVec3d
import net.ccbluex.liquidbounce.utils.math.toVec3i
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import java.util.PriorityQueue
import kotlin.math.abs

data class PathResult(
    val path: List<BlockPos>,
    val turnPoints: List<BlockPos>
)

object CalculatePath {
    private const val MAX_COST = 100

    private var previousRotation : Rotation? = null


    fun calculateRotationDifference(other: Rotation): RotationDelta {
        if (previousRotation == null) {
            previousRotation = other
            return RotationDelta(0f,0f)
        }
        return RotationDelta(
            RotationUtil.angleDifference(previousRotation!!.yaw, other.yaw),
            RotationUtil.angleDifference(previousRotation!!.pitch, other.pitch)
        )
    }
    fun findPath(startPoint: Vec3d, endPoint: Vec3d): PathResult {
        val start = BlockPos(startPoint.toVec3i())
        val end = BlockPos(endPoint.toVec3i())
        if (start == end) return PathResult(listOf(start), emptyList())
        if (!end.isPassable()) return PathResult(emptyList(), emptyList())

        val openQueue: PriorityQueue<Node> = PriorityQueue<Node>(compareBy<Node> { it.f }.thenBy { it.h })

        val openSet = mutableMapOf<BlockPos, Int>()
        val closedSet = mutableSetOf<BlockPos>()

        openQueue.add(Node(start, null, 0, start.heuristicTo(end)))
        openSet[start] = 0


        while (openQueue.isNotEmpty()) {
            val current = openQueue.poll()
            if (current.pos == end) {
                val path = current.backtrackPath()
                return PathResult(path, path.calculateTurnPoints())
            }

            closedSet.add(current.pos)

            for (neighbor in current.pos.getNeighbors()) {
                if (!neighbor.isPassable() || neighbor in closedSet) continue

                val tentativeG = current.g + 1
                if (tentativeG > MAX_COST) continue
                if (tentativeG >= openSet.getOrDefault(neighbor, Int.MAX_VALUE)) continue

                val node = Node(neighbor, current, tentativeG, neighbor.heuristicTo(end))
                openQueue.add(node)
                openSet[neighbor] = tentativeG
            }
        }

        return PathResult(emptyList(), emptyList())
    }

    private fun BlockPos.isPassable() = this.getState()?.isAir == true

    private fun BlockPos.heuristicTo(other: BlockPos) =
        abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

    private fun BlockPos.getNeighbors() = listOf(
        copy(x = x + 1), copy(x = x - 1),
        copy(y = y + 1), copy(y = y - 1),
        copy(z = z + 1), copy(z = z - 1)
    )

    class Node(
        val pos: BlockPos,
        val parent: Node?,
        val g: Int,
        val h: Int
    ) {
        val f get() = g + h

        fun backtrackPath(): List<BlockPos> {
            val path = mutableListOf<BlockPos>()
            var node: Node? = this
            while (node != null) {
                path.add(node.pos)
                node = node.parent
            }
            return path.reversed()
        }
    }

    private fun List<BlockPos>.calculateTurnPoints(): List<BlockPos> {
        if (size < 3) return emptyList()

        val directions = zipWithNext().map { (from, to) ->
            Triple(to.x - from.x, to.y - from.y, to.z - from.z)
        }

        return directions.zipWithNext()
            .mapIndexedNotNull { index, (prev, curr) ->
                if (prev != curr) get(index + 1) else null
            }
    }
}
