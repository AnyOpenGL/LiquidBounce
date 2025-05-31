/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */

package net.ccbluex.liquidbounce.injection.mixins.minecraft.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.ccbluex.liquidbounce.event.EventManager;
import net.ccbluex.liquidbounce.event.events.WorldEntityRemoveEvent;
import net.ccbluex.liquidbounce.features.module.modules.render.DoRender;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleAntiBlind;
import net.ccbluex.liquidbounce.features.module.modules.render.ModuleTrueSight;
import net.ccbluex.liquidbounce.utils.cheatdetector.WorldExtra;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    @ModifyReturnValue(method = "getBlockParticle", at = @At("RETURN"))
    private Block injectBlockParticle(Block original) {
        if (ModuleTrueSight.INSTANCE.getRunning() && ModuleTrueSight.INSTANCE.getBarriers()) {
            return Blocks.BARRIER;
        }
        return original;
    }

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void injectNoExplosionParticles(ParticleEffect parameters, double x, double y, double z, double velocityX, double velocityY, double velocityZ, CallbackInfo ci) {
        var type = parameters.getType();
        if (!ModuleAntiBlind.canRender(DoRender.EXPLOSION_PARTICLES) && (type == ParticleTypes.EXPLOSION || type == ParticleTypes.EXPLOSION_EMITTER)) {
            ci.cancel();
        }
    }

    @Inject(method = "removeEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onRemoved()V"))
    private void injectRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local Entity entity) {
        EventManager.INSTANCE.callEvent(new WorldEntityRemoveEvent(entity));
    }

    @Mixin(targets = "net/minecraft/client/world/ClientWorld$ClientEntityHandler")
    static class ClientWorldClientEntityHandlerMixin {
        // final synthetic Lnet/minecraft/client/world/ClientWorld; field_27735
        @SuppressWarnings("ShadowTarget")
        @Shadow
        @Final
        private ClientWorld field_27735;

        // Call our load event after vanilla has loaded the entity
        @Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At("TAIL"))
        private void invokeLoadEntity(Entity entity, CallbackInfo ci) {
            ClientEntityEvents.ENTITY_LOAD.invoker().onLoad(entity, this.field_27735);
        }

        // Call our unload event before vanilla does.
        @Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
        private void invokeUnloadEntity(Entity entity, CallbackInfo ci) {
            ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.field_27735);
        }

        @Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
        private void invokeStartTracking(Entity entity, CallbackInfo ci) {
            WorldExtra.INSTANCE.addEntity(entity);
        }

        @Inject(method = "stopTracking(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
        private void invokeStopTracking(Entity entity, CallbackInfo ci) {
            WorldExtra.INSTANCE.removeEntity(entity);
        }
    }


}
