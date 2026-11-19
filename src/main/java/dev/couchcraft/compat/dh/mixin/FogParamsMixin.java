package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.core.config.Config;
import com.seibel.distanthorizons.core.config.types.ConfigEntry;
import com.seibel.distanthorizons.core.render.renderer.FogRenderParamFactory;
import dev.couchcraft.compat.dh.Settings;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FogRenderParamFactory.class, remap = false)
public abstract class FogParamsMixin
{
	/**
	 * In the Nether, LODs fade into the fog soon after they begin instead of staying visible.
	 * DH measures fog start and end as a fraction of the LOD radius (not the vanilla render
	 * distance), so convert from blocks.
	 */
	@Redirect(
		method = "getRenderParam",
		at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/config/types/ConfigEntry;get()Ljava/lang/Object;"))
	private static Object couchdh$netherLodFog(ConfigEntry<?> entry)
	{
		if (Settings.inNether())
		{
			float start = Settings.NETHER_LOD_FOG_START_BLOCKS;
			if (start < 0f)
			{
				start = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16f;
			}
			float end = Math.max(Settings.NETHER_LOD_FOG_END_BLOCKS, start + 64f);
			float lodRadiusBlocks = Config.Client.Advanced.Graphics.Quality.lodChunkRenderDistanceRadius.get() * 16f;
			if (entry == Config.Client.Advanced.Graphics.Fog.farFogStart)
			{
				return start / lodRadiusBlocks;
			}
			if (entry == Config.Client.Advanced.Graphics.Fog.farFogMin)
			{
				return Settings.NETHER_LOD_FOG_MIN;
			}
			if (entry == Config.Client.Advanced.Graphics.Fog.farFogEnd)
			{
				return end / lodRadiusBlocks;
			}
		}
		return entry.get();
	}
}
