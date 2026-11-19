package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.api.enums.config.EDhApiVerticalQuality;
import com.seibel.distanthorizons.core.dataObjects.fullData.sources.FullDataSourceV2;
import com.seibel.distanthorizons.core.dataObjects.transformers.FullDataToRenderDataTransformer;
import com.seibel.distanthorizons.core.wrapperInterfaces.world.IClientLevelWrapper;
import dev.couchcraft.compat.dh.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FullDataToRenderDataTransformer.class, remap = false)
public abstract class NetherVerticalQualityMixin
{
	/**
	 * DH squeezes each LOD column into a handful of vertical slices. Past that budget it deletes
	 * the thinnest slice and stretches the one above it downward. A Nether column (floor, lava,
	 * ledges, air pockets, ceiling) has far more bands than that, so ceiling material got
	 * stretched down to the floor as tall curtains. Give dimensions with a ceiling a bigger budget.
	 */
	@Redirect(
		method = "transformCompleteFullDataToColumnData",
		at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/api/enums/config/EDhApiVerticalQuality;calculateMaxNumberOfVerticalSlicesAtDetailLevel(B)I"))
	private static int couchdh$ceilingDimensionSlices(EDhApiVerticalQuality configured, byte detailLevel,
		IClientLevelWrapper level, FullDataSourceV2 source)
	{
		int slices = configured.calculateMaxNumberOfVerticalSlicesAtDetailLevel(detailLevel);
		if (level.hasCeiling())
		{
			slices = Math.max(slices, Settings.NETHER_VERTICAL_QUALITY.calculateMaxNumberOfVerticalSlicesAtDetailLevel(detailLevel));
		}
		return slices;
	}
}
