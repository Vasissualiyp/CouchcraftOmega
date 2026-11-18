package dev.couchcraft.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import dev.couchcraft.input.PadModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Makes {@link PadModifiers} visible to everything that asks whether Shift or Ctrl is down, such as {@code Minecraft.hasShiftDown}. */
@Mixin(InputConstants.class)
public abstract class InputConstantsMixin {
	@Inject(method = "isKeyDown(I)Z", at = @At("HEAD"), cancellable = true)
	private static void couchcraft$padModifiers(final int key, final CallbackInfoReturnable<Boolean> cir) {
		boolean shift = key == InputConstants.KEY_LSHIFT || key == InputConstants.KEY_RSHIFT;
		boolean control = key == InputConstants.KEY_LCONTROL || key == InputConstants.KEY_RCONTROL;
		if (shift && PadModifiers.shift() || control && PadModifiers.control()) {
			cir.setReturnValue(true);
		}
	}
}
