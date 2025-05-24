package net.ccbluex.liquidbounce.features.module.modules.misc.cheatdetector

import it.unimi.dsi.fastutil.objects.Object2DoubleMap
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d

class PlayerEntityStatus(
    var name: Text,
    var pos: Vec3d,
    var velocity: Vec3d,
    var boundingBox: Box,
    var yaw: Float,
    var pitch: Float,
    var sprinting: Boolean,
    var sneaking: Boolean,
    var fallDistance: Float,
    var jumpingCooldown: Int,
    var onGround: Boolean,
    var horizontalCollision: Boolean,
    var verticalCollision: Boolean,
    var touchingWater: Boolean,
    var isSwimming: Boolean,
    var submergedInWater: Boolean,
    var fluidHeight: Object2DoubleMap<TagKey<Fluid>>,
    var submergedFluidTag: HashSet<TagKey<Fluid>>,
    var id: Int,
) {
    fun getX(): Double = this.pos.x

    fun getY(): Double = this.pos.y

    fun getZ(): Double = this.pos.z

    companion object {
        internal fun PlayerEntity.getStatus(): PlayerEntityStatus =
            PlayerEntityStatus(
                this.name,
                this.pos,
                this.velocity,
                this.boundingBox,
                this.yaw,
                this.pitch,
                this.isSprinting,
                this.isSneaking,
                this.fallDistance,
                this.jumpingCooldown,
                this.isOnGround,
                this.horizontalCollision,
                this.verticalCollision,
                this.isTouchingWater(),
                this.isSwimming,
                this.isSubmergedInWater(),
                this.fluidHeight,
                this.submergedFluidTag.toHashSet(),
                this.id,
            )
    }
}
