package dev.couchcraft.compat.dh.mixin;

import dev.couchcraft.compat.dh.CouchDhState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class NetherFogColorMixin
{
	/** Remembers the colour of the player's vanilla Nether fog, which DH then uses for its own fog. */
	@Inject(method = "setupFog", at = @At("RETURN"))
	private void couchdh$rememberNetherFogColor(Camera camera, int renderDistance, DeltaTracker deltaTracker, float darkenAmount,
		ClientLevel level, CallbackInfoReturnable<FogData> cir)
	{
		FogData data = cir.getReturnValue();
		if (data == null || level.dimension() != Level.NETHER)
		{
			return;
		}
		
		// remember the colour of the player's real vanilla fog, for DH to use on its LODs
		if (!CouchDhState.computingDhFogColor)
		{
			CouchDhState.netherFogColor = new float[] { data.color.x, data.color.y, data.color.z };
		}
	}
}
