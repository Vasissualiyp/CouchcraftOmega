package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiFogRenderParam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DhApiFogRenderParam.class, remap = false)
public abstract class FogParamCopyMixin
{
	@Shadow
	protected float farFogMaxThickness;
	
	/**
	 * DH's copy constructor passes the fog END distance where the maximum fog THICKNESS belongs.
	 * It never showed because the default end is 1.0, but any fog that ends closer than the far
	 * clip plane could never get thicker than its end fraction (0.098 for a fog ending at 400
	 * blocks), so far LODs stayed visible.
	 */
	@Inject(method = "<init>(Lcom/seibel/distanthorizons/api/methods/events/sharedParameterObjects/DhApiFogRenderParam;)V", at = @At("RETURN"))
	private void couchdh$keepMaxThickness(DhApiFogRenderParam parent, CallbackInfo ci)
	{
		this.farFogMaxThickness = parent.getFarFogMaxThickness();
	}
}
