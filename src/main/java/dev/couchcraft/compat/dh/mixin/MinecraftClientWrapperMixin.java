package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.common.wrappers.minecraft.MinecraftClientWrapper;
import com.seibel.distanthorizons.core.pos.blockPos.DhBlockPos;
import dev.couchcraft.compat.dh.CouchDhState;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MinecraftClientWrapper.class, remap = false)
public abstract class MinecraftClientWrapperMixin
{
	/**
	 * DH's background threads read the player position while the render thread swaps
	 * split-screen players in and out. Give them the host (first player) instead.
	 */
	@Inject(method = "getPlayerBlockPos", at = @At("HEAD"), cancellable = true)
	private void couchdh$stablePlayerPos(CallbackInfoReturnable<DhBlockPos> cir)
	{
		if (Minecraft.getInstance().isSameThread())
		{
			return;
		}
		
		CouchDhState.Anchor host = CouchDhState.firstAnchor();
		if (host != null)
		{
			cir.setReturnValue(new DhBlockPos(host.x(), 0, host.z()));
		}
	}
}
