package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiFogRenderParam;
import com.seibel.distanthorizons.common.render.blaze.postProcessing.BlazeDhFogRenderer;
import com.seibel.distanthorizons.core.render.RenderParams;
import dev.couchcraft.compat.dh.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlazeDhFogRenderer.class, remap = false)
public abstract class FogDebugMixin
{
	@Unique
	private static long couchdh$lastLog;
	
	@Inject(method = "render", at = @At("HEAD"))
	private void couchdh$logFog(RenderParams renderParams, DhApiFogRenderParam fog, CallbackInfo ci)
	{
		long now = System.currentTimeMillis();
		if (!Settings.DEBUG_LOG || !Settings.inNether() || now - couchdh$lastLog < 5000)
		{
			return;
		}
		couchdh$lastLog = now;
		System.out.println("[couch-dh-compat] nether DH fog pass: start=" + fog.getFarFogStartPercent()
			+ " end=" + fog.getFarFogEndPercent() + " min=" + fog.getFarFogMinThickness()
			+ " max=" + fog.getFarFogMaxThickness() + " density=" + fog.getFarFogDensity()
			+ " color=" + fog.getFogColor()
			+ " vanillaColor=" + java.util.Arrays.toString(dev.couchcraft.compat.dh.CouchDhState.netherFogColor));
	}
}
