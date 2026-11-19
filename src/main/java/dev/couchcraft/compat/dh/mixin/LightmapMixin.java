package dev.couchcraft.compat.dh.mixin;

import com.seibel.distanthorizons.common.wrappers.world.ClientLevelWrapper;
import com.seibel.distanthorizons.core.api.internal.ClientApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Lightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class LightmapMixin
{
	/**
	 * DH stores each lightmap under the level in {@code RENDER_STATE.clientLevelWrapper}, but it
	 * only updates that wrapper later, in {@code GameRenderer.renderLevel}. When players are in
	 * different dimensions, the lightmap rendered for one player was filed under the previous
	 * player's dimension, so the overworld got the Nether's warm lightmap. Point the wrapper at
	 * the live player's level first.
	 */
	@Inject(method = "render", at = @At("HEAD"))
	private void couchdh$useLiveLevel(CallbackInfo ci)
	{
		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && player.level() instanceof ClientLevel level)
		{
			ClientApi.RENDER_STATE.clientLevelWrapper = ClientLevelWrapper.getWrapperIfDifferent(
				ClientApi.RENDER_STATE.clientLevelWrapper, level);
		}
	}
}
