package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.core.level.IDhClientLevel;
import com.seibel.distanthorizons.core.pos.blockPos.DhBlockPos2D;
import com.seibel.distanthorizons.core.render.QuadTree.LodQuadTree;
import dev.couchcraft.compat.dh.CouchDhState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LodQuadTree.class, remap = false)
public abstract class LodQuadTreeMixin
{
	@Shadow
	@Final
	private IDhClientLevel level;
	
	/**
	 * The detail wanted at a block is decided by its distance to "the player". With split screen
	 * that is the nearest player in this level's dimension, so each view keeps full detail around
	 * itself and the result no longer depends on whose view is currently swapped in.
	 */
	@Redirect(
		method = "calcExpectedDetailLevel(Lcom/seibel/distanthorizons/core/pos/blockPos/DhBlockPos2D;JIID)B",
		at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/pos/blockPos/DhBlockPos2D;dist(II)D"))
	private double couchdh$nearestPlayerDistance(DhBlockPos2D treeCentre, int x, int z)
	{
		double nearest = CouchDhState.nearestDistance(this.level.getLevelWrapper().getDimensionName(), x, z);
		return Double.isNaN(nearest) ? treeCentre.dist(x, z) : nearest;
	}
}
