package dev.couchcraft.compat.dh.mixin;

import dev.couchcraft.compat.dh.CouchDhState;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
{
	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void couchdh$publishPlayers(CallbackInfo ci)
	{
		CouchDhState.refresh();
	}
}
