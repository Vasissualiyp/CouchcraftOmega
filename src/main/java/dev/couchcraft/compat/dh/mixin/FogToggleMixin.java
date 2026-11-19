package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.core.config.Config;
import com.seibel.distanthorizons.core.config.types.ConfigEntry;
import com.seibel.distanthorizons.core.render.renderer.LodRenderer;
import dev.couchcraft.compat.dh.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LodRenderer.class, remap = false)
public abstract class FogToggleMixin
{
	/** DH fog on LODs is a per-dimension choice here: always on in the Nether, as configured elsewhere. */
	@Redirect(
		method = "renderTerrain",
		at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/config/types/ConfigEntry;get()Ljava/lang/Object;"))
	private Object couchdh$fogPerDimension(ConfigEntry<?> entry)
	{
		if (entry == Config.Client.Advanced.Graphics.Fog.enableDhFog)
		{
			return Settings.inNether() || Settings.OTHER_DIMENSIONS_LOD_FOG;
		}
		return entry.get();
	}
}
