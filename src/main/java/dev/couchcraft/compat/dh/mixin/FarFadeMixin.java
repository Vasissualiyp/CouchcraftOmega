package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.common.render.blaze.postProcessing.BlazeDhFarFadeRenderer;
import com.seibel.distanthorizons.core.util.RenderUtil;
import dev.couchcraft.compat.dh.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlazeDhFarFadeRenderer.class, remap = false)
public abstract class FarFadeMixin
{
	/**
	 * DH's far fade blends LODs into the vanilla background colour just before its far clip
	 * plane, which is thousands of blocks away. In the Nether, bring that in so far LODs
	 * disappear into the fog even if the fog pass itself is not visible.
	 */
	@Redirect(
		method = "render",
		at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/util/RenderUtil;getFarClipPlaneDistanceInBlocks()F"))
	private float couchdh$closerFarFade()
	{
		if (Settings.NETHER_FAR_FADE_END_BLOCKS > 0f && Settings.inNether())
		{
			return Settings.NETHER_FAR_FADE_END_BLOCKS;
		}
		return RenderUtil.getFarClipPlaneDistanceInBlocks();
	}
}
