package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftRenderWrapper;
import dev.couchcraft.compat.dh.CouchDhState;
import dev.couchcraft.compat.dh.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.Color;

@Mixin(value = MinecraftRenderWrapper.class, remap = false)
public abstract class DhFogColorMixin
{
	/**
	 * DH computes its own fog colour with a private FogRenderer, which comes out a pale grey-pink in
	 * the Nether. Use the colour vanilla's own fog has for the player instead (see NetherVanillaFogMixin).
	 */
	@Inject(method = "getFogColor", at = @At("HEAD"))
	private void couchdh$markDhColour(float partialTicks, CallbackInfoReturnable<Color> cir)
	{
		CouchDhState.computingDhFogColor = true;
	}
	
	@Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
	private void couchdh$useVanillaColour(float partialTicks, CallbackInfoReturnable<Color> cir)
	{
		CouchDhState.computingDhFogColor = false;
		float[] vanilla = CouchDhState.netherFogColor;
		if (vanilla != null && Settings.inNether())
		{
			cir.setReturnValue(new Color(clamp(vanilla[0]), clamp(vanilla[1]), clamp(vanilla[2]), 1f));
		}
	}
	
	private static float clamp(float value)
	{
		return Math.max(0f, Math.min(value, 1f));
	}
}
