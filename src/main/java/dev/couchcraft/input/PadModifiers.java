package dev.couchcraft.input;

import dev.couchcraft.CouchConfig;
import dev.couchcraft.context.Couch;
import dev.couchcraft.context.PlayerContext;
import net.minecraft.client.Minecraft;

/**
 * Shift and Ctrl for controller players. While a menu is open, holding LB acts as Shift and holding RB as Ctrl for the
 * player whose view is live, so shortcuts that read the keyboard (shift-click variants, "move all matching" buttons,
 * and the same keys in other mods) can be used without one. LB and RB do nothing else in a menu.
 */
public final class PadModifiers {
	/** GLFW modifier bits, as carried by mouse button events. */
	public static final int SHIFT = 1;
	public static final int CONTROL = 2;

	private PadModifiers() {
	}

	private static Gamepad menuPad() {
		if (!Couch.ready() || !CouchConfig.get().shoulderModifiers) {
			return null;
		}

		PlayerContext context = Couch.current();
		if (context == null || context.pad == null || Minecraft.getInstance().gui.screen() == null) {
			return null;
		}

		return context.pad;
	}

	public static boolean shift() {
		Gamepad pad = menuPad();
		return pad != null && pad.held(Gamepad.LB);
	}

	public static boolean control() {
		Gamepad pad = menuPad();
		return pad != null && pad.held(Gamepad.RB);
	}

	/** Modifier bits for a mouse button event from the live player's controller. */
	public static int current() {
		return (shift() ? SHIFT : 0) | (control() ? CONTROL : 0);
	}
}
