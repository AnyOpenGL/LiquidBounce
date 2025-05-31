package net.ccbluex.liquidbounce.utils.cheatdetector

import net.minecraft.entity.Entity
import net.minecraft.world.World
import java.util.UUID

object WorldExtra {
    internal val uuidAndEntityMap = mutableMapOf<UUID, Entity>()
    internal val uuidAndIdMap = mutableMapOf<UUID, Int>()
    internal val idAndUUIDMap = mutableMapOf<Int, UUID>()

    fun addEntity(entity: Entity) {
        uuidAndEntityMap.put(entity.uuid, entity)
        uuidAndIdMap.put(entity.uuid, entity.id)
        idAndUUIDMap.put(entity.id, entity.uuid)
    }

    fun removeEntity(entity: Entity) {
        uuidAndEntityMap.remove(entity.uuid)
        uuidAndIdMap.remove(entity.uuid)
        idAndUUIDMap.remove(entity.id)
    }

    fun World.getEntityByUUID(uuid: UUID): Entity? = uuidAndEntityMap.get(uuid)

    fun World.getUUIDById(id: Int): UUID? = idAndUUIDMap.get(id)

    fun World.getIdByUUID(uuid: UUID): Int? = uuidAndIdMap.get(uuid)
}
