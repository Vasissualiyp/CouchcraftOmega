package dev.couchcraft.render;

import com.mojang.logging.LogUtils;
import dev.couchcraft.CouchConfig;
import dev.couchcraft.input.Gamepad;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;

/**
 * A pixel-art controller with a line from each button to what it does, drawn over a player's view while they hold
 * BACK. The labels differ between the world and menus, the button being held lights up, and the picture shrinks to fit
 * a split-screen view.
 * <p>
 * Other mods can add or replace a label with {@link #describe}, usually from their client initialiser. The text comes
 * from a supplier evaluated every frame for the live player, so it can follow game state; returning null or an empty
 * string hides the label (or falls back to an earlier registration). {@code {name}} placeholders in a label are filled
 * from {@link #variable}. Players can override any label in {@code config/couchcraft-button-map.properties}, for example
 * {@code menu.A=select}; an empty value hides it.
 */
public final class ButtonMap {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final int PX = 2;
	private static final int ROW = 11;
	private static final int GAP = 14;
	private static final int OUTLINE = 0xFFFFD43B;
	private static final int HELD = 0xFFC084FC;
	private static final float MIN_SCALE = 0.45F;

	/** Where a label is shown. */
	public enum Context {
		/** A menu or container screen is open. */
		MENU,
		/** Playing in the world. */
		WORLD
	}

	/** A controller input a label can describe: where it sits on the picture, which input it reads, and its label colour. */
	public enum Input {
		A(Gamepad.A, 44, 18, 5, 5, 0xFF69DB7C),
		B(Gamepad.B, 48, 14, 5, 5, 0xFFFF8787),
		X(Gamepad.X, 40, 14, 5, 5, 0xFF74C0FC),
		Y(Gamepad.Y, 44, 10, 5, 5, 0xFFFFE066),
		LB(Gamepad.LB, 15, 6, 11, 4, 0xFFFFFFFF),
		RB(Gamepad.RB, 45, 6, 11, 4, 0xFFFFFFFF),
		LT(Gamepad.LT, 15, 2, 9, 3, 0xFFFFFFFF),
		RT(Gamepad.RT, 45, 2, 9, 3, 0xFFFFFFFF),
		L3(Gamepad.L3, 17, 14, 11, 11, 0xFFFFFFFF),
		R3(Gamepad.R3, 38, 23, 11, 11, 0xFFFFFFFF),
		BACK(Gamepad.BACK, 27, 14, 5, 3, 0xFFFFFFFF),
		START(Gamepad.START, 33, 14, 5, 3, 0xFFFFFFFF),
		DPAD_UP(Gamepad.DPAD_UP, 22, 21, 3, 3, 0xFFFFFFFF),
		DPAD_DOWN(Gamepad.DPAD_DOWN, 22, 25, 3, 3, 0xFFFFFFFF),
		DPAD_LEFT(Gamepad.DPAD_LEFT, 20, 23, 3, 3, 0xFFFFFFFF),
		DPAD_RIGHT(Gamepad.DPAD_RIGHT, 24, 23, 3, 3, 0xFFFFFFFF),
		LEFT_STICK(-1, 17, 14, 11, 11, 0xFFFFFFFF),
		RIGHT_STICK(-1, 38, 23, 11, 11, 0xFFFFFFFF);

		private final int button;
		private final int x;
		private final int y;
		private final int width;
		private final int height;
		private final int color;

		Input(final int button, final int x, final int y, final int width, final int height, final int color) {
			this.button = button;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.color = color;
		}

		private boolean held(final Gamepad pad) {
			return switch (this) {
				case LEFT_STICK -> Math.abs(pad.leftX()) > 0.3F || Math.abs(pad.leftY()) > 0.3F;
				case RIGHT_STICK -> Math.abs(pad.rightX()) > 0.3F || Math.abs(pad.rightY()) > 0.3F;
				default -> pad.held(this.button);
			};
		}
	}

	private record Entry(Input input, Supplier<String> label) {
	}

	private static final Map<Context, List<Entry>> ENTRIES = new EnumMap<>(Context.class);
	private static final Map<String, Supplier<String>> VARIABLES = new ConcurrentHashMap<>();
	private static volatile Map<String, String> overrides;

	static {
		for (Context context : Context.values()) {
			ENTRIES.put(context, new CopyOnWriteArrayList<>());
		}

		describe(Context.WORLD, Input.LEFT_STICK, builtIn("move", "move"));
		describe(Context.WORLD, Input.RIGHT_STICK, builtIn("look", "look"));
		describe(Context.WORLD, Input.A, builtIn("jump", "jump"));
		describe(Context.WORLD, Input.B, builtIn("drop", "drop"));
		describe(Context.WORLD, Input.X, builtIn("pick", "pick block"));
		describe(Context.WORLD, Input.Y, builtIn("inventory", "inventory"));
		describe(Context.WORLD, Input.RT, builtIn("attack", "attack"));
		describe(Context.WORLD, Input.LT, builtIn("use", "use"));
		describe(Context.WORLD, Input.LB, builtIn("hotbar_prev", "hotbar left"));
		describe(Context.WORLD, Input.RB, builtIn("hotbar_next", "hotbar right"));
		describe(Context.WORLD, Input.L3, builtIn("sprint", "sprint"));
		describe(Context.WORLD, Input.R3, builtIn("sneak", "sneak"));
		describe(Context.WORLD, Input.DPAD_UP, builtIn("perspective", "perspective"));
		describe(Context.WORLD, Input.DPAD_DOWN, builtIn("swap", "swap hands"));
		describe(Context.WORLD, Input.BACK, builtIn("players", "player list"));
		describe(Context.WORLD, Input.START, builtIn("pause", "pause"));

		describe(Context.MENU, Input.LEFT_STICK, builtIn("cursor", "move cursor"));
		describe(Context.MENU, Input.RIGHT_STICK, builtIn("scroll", "scroll"));
		describe(Context.MENU, Input.A, builtIn("click", "click"));
		describe(Context.MENU, Input.X, builtIn("shift_click", "shift-click"));
		describe(Context.MENU, Input.Y, builtIn("right_click", "right-click"));
		describe(Context.MENU, Input.B, builtIn("close", "close"));
		describe(Context.MENU, Input.LB, () -> CouchConfig.get().shoulderModifiers ? Component.translatableWithFallback("couchcraft.map.shift", "hold: Shift").getString() : null);
		describe(Context.MENU, Input.RB, () -> CouchConfig.get().shoulderModifiers ? Component.translatableWithFallback("couchcraft.map.ctrl", "hold: Ctrl").getString() : null);
		describe(Context.MENU, Input.DPAD_UP, builtIn("nudge", "nudge cursor"));
		describe(Context.MENU, Input.DPAD_DOWN, builtIn("nudge", "nudge cursor"));
		describe(Context.MENU, Input.DPAD_LEFT, builtIn("nudge", "nudge cursor"));
		describe(Context.MENU, Input.DPAD_RIGHT, builtIn("nudge", "nudge cursor"));
		describe(Context.MENU, Input.BACK, builtIn("map", "hold: map"));
	}

	private static Supplier<String> builtIn(final String key, final String fallback) {
		return () -> Component.translatableWithFallback("couchcraft.map." + key, fallback).getString();
	}

	/** Sets the label of an input in a context. A later registration replaces an earlier one unless it returns null. */
	public static void describe(final Context context, final Input input, final Supplier<String> label) {
		ENTRIES.get(context).add(new Entry(input, label));
	}

	/** Defines {@code {name}} for use in labels, for example the current mode of a mod. */
	public static void variable(final String name, final Supplier<String> value) {
		VARIABLES.put(name, value);
	}

	private static Map<String, String> overrides() {
		Map<String, String> loaded = overrides;
		if (loaded == null) {
			loaded = new HashMap<>();
			Path file = FabricLoader.getInstance().getConfigDir().resolve("couchcraft-button-map.properties");
			try {
				if (Files.exists(file)) {
					Properties props = new Properties();
					try (InputStream in = Files.newInputStream(file)) {
						props.load(in);
					}

					for (String key : props.stringPropertyNames()) {
						loaded.put(key, props.getProperty(key));
					}
				} else {
					Properties example = new Properties();
					try (OutputStream out = Files.newOutputStream(file)) {
						example.store(out, "Override button map labels, e.g. menu.A=select or world.RT= (empty hides it). Inputs: " + Arrays.toString(Input.values()) + ". Contexts: menu, world. Takes effect on the next launch.");
					}
				}
			} catch (IOException | RuntimeException e) {
				LOGGER.warn("[Couchcraft] Could not read {}", file, e);
			}

			overrides = loaded;
		}

		return loaded;
	}

	/** The label of every described input in a context, in input order. */
	private static Map<Input, String> labels(final Context context) {
		Map<String, String> overrides = overrides();
		Map<Input, String> out = new EnumMap<>(Input.class);
		for (Input input : Input.values()) {
			String text = overrides.get(context.name().toLowerCase(Locale.ROOT) + "." + input.name());
			if (text == null) {
				List<Entry> entries = ENTRIES.get(context);
				for (int i = entries.size() - 1; i >= 0 && text == null; i--) {
					Entry entry = entries.get(i);
					if (entry.input() == input) {
						text = entry.label().get();
					}
				}
			}

			if (text != null && !text.isEmpty()) {
				out.put(input, expand(text));
			}
		}

		return out;
	}

	private static String expand(final String text) {
		String result = text;
		if (result.indexOf('{') >= 0) {
			for (Map.Entry<String, Supplier<String>> variable : VARIABLES.entrySet()) {
				String value = variable.getValue().get();
				result = result.replace("{" + variable.getKey() + "}", value == null ? "" : value);
			}
		}

		return result;
	}

	/** '.' is transparent; the other letters are looked up in {@link #color}. */
	private static final String[] SPRITE = {
		"............................................................",
		"...........OOOOOOOOO....................OOOOOOOOO...........",
		"...........OkkkkkkkO....................OkkkkkkkO...........",
		"...........OkkkkkkkO....................OkkkkkkkO...........",
		"..........OKKKKKKKKKO..................OKKKKKKKKKO..........",
		"..........OKKKKKKKKKO..................OKKKKKKKKKO..........",
		"..........OKKKKKKKKKO..................OKKKKKKKKKO..........",
		"..........OKKKKKKKKKO..................OKKKKKKKKKO..........",
		"...........OHHHHHHHHHOOOOOOOOOWOOOOOOOOHHHHHyHHHO...........",
		".........OOHHHHHHDHHHHHHHHHHHWWWHHHHHHHHHHHyyyHHHOO.........",
		"........OBBBBBDDDDDDDBBBBBBBWWWWWBBBBBBBBByyyyyBBBBO........",
		".......OBBBBBDDDDSDDDDBBBBBBBWWWBBBBBBBBBBByyyBBBBBBO.......",
		".......OBBBBBDDSSSSSDDBBBBBBBBWBBBBBBBBBxBBByBBBbBBBO.......",
		"......OBBBBBBDDSSSSSDDBBBBBBBBBBBBBBBBBxxxBBBBBbbbBBBO......",
		"......OBBBBBDDSSSSSSSDDBBBDDDBBBDDDBBBxxxxxBBBbbbbbBBO......",
		"......OBBBBBBDDSSSSSDDBBBBBBBBBBBBBBBBBxxxBBBBBbbbBBBO......",
		"......OBBBBBBDDSSSSSDDBBBBBBBBBBBBBBBBBBxBBBaBBBbBBBBO......",
		"......OBBBBBBDDDDSDDDDBBBBBBBBBBBBBBBBBBBBBaaaBBBBBBBO......",
		"......OBBBBBBBDDDDDDDBBBBBBBBBBBBBBBBBDBBBaaaaaBBBBBBO......",
		"......OBBBBBBBBBBDBBBBBBBBBBBBBBBBBDDDDDDDBaaaBBBBBBBO......",
		"......OBBBBBBBBBBBBBBSSSBBBBBBBBBBDDDDSDDDDBaBBBBBBBBO......",
		"......OBBBBBBBBBBBBBBSSSBBBBBBBBBBDDSSSSSDDBBBBBBBBBBO......",
		"......OBBBBBBBBBBBBSSDDDSSBBBBBBBBDDSSSSSDDBBBBBBBBBBO......",
		".....OBBBBBBBBBBBBBSSDDDSSBBBBBBBDDSSSSSSSDDBBBBBBBBBBO.....",
		".....OBBBBBBBBBBBBBSSDDDSSBBBBBBBBDDSSSSSDDBBBBBBBBBBBO.....",
		"....OBBBBBBBBBBBBBBBBSSSGGGGGGGGGGDDSSSSSDDBBBBBBBBBBBBO....",
		"....OBBBBBBBBBBBBGGGGSSSGGGGGGGGGGDDDDSDDDDGBBBBBBBBBBBO....",
		"....OBBBBBBGGGGGGGGGGGOOOOOOOOOOOOODDDDDDDGGGGGGGGBBBBBO....",
		"....OGGGGGGGGGGGGGGGGO................DOGGGGGGGGGGGGGGGO....",
		"....OGGGGGGGGGGGGGGGGO.................OGGGGGGGGGGGGGGGO....",
		"....OGGGGGGGGGGGGGGGGO.................OGGGGGGGGGGGGGGGO....",
		"....OGGGGGGGGGGGGGGGGO.................OGGGGGGGGGGGGGGGO....",
		".....OGGGGGGGGGGGGGGGO.................OGGGGGGGGGGGGGGO.....",
		".....OGGGGGGGGGGGGGGGO.................OGGGGGGGGGGGGGGO.....",
		"......OGGGGGGGGGGGGGGO.................OGGGGGGGGGGGGGO......",
		"......OGGGGGGGGGGGGGGO.................OGGGGGGGGGGGGGO......",
		".......OGGGGGGGGGGGGGO.................OGGGGGGGGGGGGO.......",
		"........OOGGGGGGGGGGGO.................OGGGGGGGGGGOO........",
		"..........OOGGGGGGGOO..................OOGGGGGGGOO..........",
		"............OOOOOOO......................OOOOOOO............"
	};
	private static final int SPRITE_WIDTH = SPRITE[0].length();
	private static final int SPRITE_HEIGHT = SPRITE.length;
	private static final int BODY_W = SPRITE_WIDTH * PX;
	private static final int BODY_H = SPRITE_HEIGHT * PX;

	private ButtonMap() {
	}

	private record Row(Input input, String text, int textWidth) {
	}

	/** Draws the map centred in the view. Returns false, drawing nothing, if even a shrunken map would be unreadable. */
	public static boolean draw(final GuiGraphicsExtractor graphics, final Font font, final boolean menu, final Gamepad pad) {
		Map<Input, String> labels = labels(menu ? Context.MENU : Context.WORLD);
		List<Row> left = new ArrayList<>();
		List<Row> right = new ArrayList<>();
		int leftWidth = 0;
		int rightWidth = 0;
		for (Map.Entry<Input, String> entry : labels.entrySet()) {
			Row row = new Row(entry.getKey(), entry.getValue(), font.width(entry.getValue()));
			if (row.input().x < SPRITE_WIDTH / 2) {
				left.add(row);
				leftWidth = Math.max(leftWidth, row.textWidth());
			} else {
				right.add(row);
				rightWidth = Math.max(rightWidth, row.textWidth());
			}
		}

		// Top to bottom, so the lines to the buttons do not cross.
		Comparator<Row> byHeight = Comparator.comparingInt((Row row) -> row.input().y).thenComparingInt(row -> row.input().x);
		left.sort(byHeight);
		right.sort(byHeight);

		int height = Math.max(BODY_H, Math.max(left.size(), right.size()) * ROW);
		int width = leftWidth + GAP + BODY_W + GAP + rightWidth;
		int viewWidth = graphics.guiWidth();
		int viewHeight = graphics.guiHeight();
		float scale = Math.min(1.0F, Math.min((viewWidth - 8) / (float)(width + 12), (viewHeight - 8) / (float)(height + 12)));
		if (scale < MIN_SCALE) {
			return false;
		}

		graphics.pose().pushMatrix();
		graphics.pose().translate(viewWidth / 2.0F, viewHeight / 2.0F);
		graphics.pose().scale(scale);
		int x0 = -width / 2;
		int y0 = -height / 2;
		graphics.fill(x0 - 6, y0 - 6, x0 + width + 6, y0 + height + 6, ARGB.color(200, 0x000000));

		int bodyX = x0 + leftWidth + GAP;
		int bodyY = y0 + (height - BODY_H) / 2;
		drawSprite(graphics, bodyX, bodyY);
		drawLabels(graphics, font, left, true, x0, leftWidth, bodyX, bodyY, y0, pad);
		drawLabels(graphics, font, right, false, bodyX + BODY_W + GAP, rightWidth, bodyX, bodyY, y0, pad);
		for (Input input : labels.keySet()) {
			drawOutline(graphics, input, bodyX, bodyY, input.held(pad));
		}

		graphics.pose().popMatrix();
		return true;
	}

	private static void drawLabels(
		final GuiGraphicsExtractor graphics, final Font font, final List<Row> rows, final boolean left, final int columnX, final int columnWidth,
		final int bodyX, final int bodyY, final int top, final Gamepad pad
	) {
		int y = top;
		for (Row row : rows) {
			Input input = row.input();
			boolean held = input.held(pad);
			int color = held ? HELD : input.color;
			int textX = left ? columnX + columnWidth - row.textWidth() : columnX;
			graphics.text(font, row.text(), textX, y + 1, color, false);

			// A horizontal run from the label to the button's column, then a vertical one to the button.
			int lineY = y + 5;
			int targetX = bodyX + input.x * PX + PX / 2;
			int targetY = bodyY + input.y * PX + PX / 2;
			int startX = left ? columnX + columnWidth + 2 : columnX - 2;
			int lineColor = held ? HELD : ARGB.color(190, OUTLINE);
			graphics.fill(Math.min(startX, targetX), lineY, Math.max(startX, targetX) + 1, lineY + 1, lineColor);
			graphics.fill(targetX, Math.min(lineY, targetY), targetX + 1, Math.max(lineY, targetY) + 1, lineColor);
			y += ROW;
		}
	}

	private static void drawOutline(final GuiGraphicsExtractor graphics, final Input input, final int bodyX, final int bodyY, final boolean held) {
		int x0 = bodyX + (input.x - input.width / 2) * PX - 1;
		int y0 = bodyY + (input.y - input.height / 2) * PX - 1;
		int x1 = bodyX + (input.x + input.width / 2 + 1) * PX + 1;
		int y1 = bodyY + (input.y + input.height / 2 + 1) * PX + 1;
		int color = held ? HELD : OUTLINE;
		if (held) {
			graphics.fill(x0, y0, x1, y1, ARGB.color(150, HELD));
		}

		graphics.fill(x0, y0, x1, y0 + 1, color);
		graphics.fill(x0, y1 - 1, x1, y1, color);
		graphics.fill(x0, y0, x0 + 1, y1, color);
		graphics.fill(x1 - 1, y0, x1, y1, color);
	}

	private static void drawSprite(final GuiGraphicsExtractor graphics, final int bodyX, final int bodyY) {
		for (int row = 0; row < SPRITE_HEIGHT; row++) {
			String line = SPRITE[row];
			int column = 0;
			while (column < line.length()) {
				char c = line.charAt(column);
				int end = column;
				while (end < line.length() && line.charAt(end) == c) {
					end++;
				}

				if (c != '.') {
					graphics.fill(bodyX + column * PX, bodyY + row * PX, bodyX + end * PX, bodyY + (row + 1) * PX, color(c));
				}

				column = end;
			}
		}
	}

	private static int color(final char c) {
		return switch (c) {
			case 'O' -> 0xFF3B414D;
			case 'B' -> 0xFFD9DCE1;
			case 'H' -> 0xFFF1F3F5;
			case 'G' -> 0xFFB3B9C3;
			case 'K' -> 0xFF8C93A0;
			case 'k' -> 0xFF6B7280;
			case 'D' -> 0xFF22262E;
			case 'S' -> 0xFF4B5160;
			case 'W' -> 0xFFFFFFFF;
			case 'y' -> 0xFFFCC419;
			case 'a' -> 0xFF40C057;
			case 'x' -> 0xFF339AF0;
			case 'b' -> 0xFFFA5252;
			default -> 0;
		};
	}
}
