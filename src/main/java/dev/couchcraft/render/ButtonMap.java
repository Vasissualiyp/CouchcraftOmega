package dev.couchcraft.render;

import dev.couchcraft.input.Gamepad;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

/**
 * A pixel-art controller with a line from each button to what it does, drawn over a player's view while they hold
 * BACK. The labels differ between the world and menus, the button being held lights up, and the picture shrinks to fit
 * a split-screen view.
 */
public final class ButtonMap {
	private static final int PX = 2;
	private static final int ROW = 11;
	private static final int GAP = 14;
	private static final int OUTLINE = 0xFFFFD43B;
	private static final int HELD = 0xFFC084FC;
	private static final float MIN_SCALE = 0.45F;

	private static final int NO_STICK = 0;
	private static final int LEFT_STICK = 1;
	private static final int RIGHT_STICK = 2;

	/**
	 * One button on the picture: its position (centre and size, in sprite pixels), the controller input it reads, and
	 * the colour of its label.
	 */
	private record Spot(int button, int stick, int x, int y, int width, int height, int color) {
	}

	private static final Spot LT = new Spot(Gamepad.LT, NO_STICK, 15, 2, 9, 3, 0xFFFFFFFF);
	private static final Spot LB = new Spot(Gamepad.LB, NO_STICK, 15, 6, 11, 4, 0xFFFFFFFF);
	private static final Spot RT = new Spot(Gamepad.RT, NO_STICK, 45, 2, 9, 3, 0xFFFFFFFF);
	private static final Spot RB = new Spot(Gamepad.RB, NO_STICK, 45, 6, 11, 4, 0xFFFFFFFF);
	private static final Spot LEFT = new Spot(-1, LEFT_STICK, 17, 14, 11, 11, 0xFFFFFFFF);
	private static final Spot L3 = new Spot(Gamepad.L3, NO_STICK, 17, 14, 11, 11, 0xFFFFFFFF);
	private static final Spot RIGHT = new Spot(-1, RIGHT_STICK, 38, 23, 11, 11, 0xFFFFFFFF);
	private static final Spot R3 = new Spot(Gamepad.R3, NO_STICK, 38, 23, 11, 11, 0xFFFFFFFF);
	private static final Spot UP = new Spot(Gamepad.DPAD_UP, NO_STICK, 22, 21, 3, 3, 0xFFFFFFFF);
	private static final Spot DOWN = new Spot(Gamepad.DPAD_DOWN, NO_STICK, 22, 25, 3, 3, 0xFFFFFFFF);
	private static final Spot DLEFT = new Spot(Gamepad.DPAD_LEFT, NO_STICK, 20, 23, 3, 3, 0xFFFFFFFF);
	private static final Spot DRIGHT = new Spot(Gamepad.DPAD_RIGHT, NO_STICK, 24, 23, 3, 3, 0xFFFFFFFF);
	private static final Spot BACK = new Spot(Gamepad.BACK, NO_STICK, 27, 14, 5, 3, 0xFFFFFFFF);
	private static final Spot START = new Spot(Gamepad.START, NO_STICK, 33, 14, 5, 3, 0xFFFFFFFF);
	private static final Spot Y = new Spot(Gamepad.Y, NO_STICK, 44, 10, 5, 5, 0xFFFFE066);
	private static final Spot X = new Spot(Gamepad.X, NO_STICK, 40, 14, 5, 5, 0xFF74C0FC);
	private static final Spot B = new Spot(Gamepad.B, NO_STICK, 48, 14, 5, 5, 0xFFFF8787);
	private static final Spot A = new Spot(Gamepad.A, NO_STICK, 44, 18, 5, 5, 0xFF69DB7C);

	private record Entry(Spot spot, Component label) {
	}

	private static Component text(final String key, final String fallback) {
		return Component.translatableWithFallback("couchcraft.map." + key, fallback);
	}

	private static final List<Entry> WORLD = List.of(
		new Entry(LEFT, text("move", "move")),
		new Entry(RIGHT, text("look", "look")),
		new Entry(A, text("jump", "jump")),
		new Entry(B, text("drop", "drop")),
		new Entry(X, text("pick", "pick block")),
		new Entry(Y, text("inventory", "inventory")),
		new Entry(RT, text("attack", "attack")),
		new Entry(LT, text("use", "use")),
		new Entry(LB, text("hotbar_prev", "hotbar left")),
		new Entry(RB, text("hotbar_next", "hotbar right")),
		new Entry(L3, text("sprint", "sprint")),
		new Entry(R3, text("sneak", "sneak")),
		new Entry(UP, text("perspective", "perspective")),
		new Entry(DOWN, text("swap", "swap hands")),
		new Entry(BACK, text("players", "player list")),
		new Entry(START, text("pause", "pause"))
	);

	private static final List<Entry> MENU = List.of(
		new Entry(LEFT, text("cursor", "move cursor")),
		new Entry(RIGHT, text("scroll", "scroll")),
		new Entry(A, text("click", "click")),
		new Entry(X, text("shift_click", "shift-click")),
		new Entry(Y, text("right_click", "right-click")),
		new Entry(B, text("close", "close")),
		new Entry(DLEFT, text("nudge", "nudge cursor")),
		new Entry(UP, text("nudge", "nudge cursor")),
		new Entry(DOWN, text("nudge", "nudge cursor")),
		new Entry(DRIGHT, text("nudge", "nudge cursor"))
	);

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

	private record Row(Entry entry, int textWidth) {
	}

	/** Draws the map centred in the view. Returns false, drawing nothing, if even a shrunken map would be unreadable. */
	public static boolean draw(final GuiGraphicsExtractor graphics, final Font font, final boolean menu, final Gamepad pad) {
		List<Row> left = new ArrayList<>();
		List<Row> right = new ArrayList<>();
		int leftWidth = 0;
		int rightWidth = 0;
		for (Entry entry : menu ? MENU : WORLD) {
			Row row = new Row(entry, font.width(entry.label()));
			if (entry.spot().x() < SPRITE_WIDTH / 2) {
				left.add(row);
				leftWidth = Math.max(leftWidth, row.textWidth());
			} else {
				right.add(row);
				rightWidth = Math.max(rightWidth, row.textWidth());
			}
		}

		// Top to bottom, so the lines to the buttons do not cross.
		Comparator<Row> byHeight = Comparator.comparingInt((Row row) -> row.entry().spot().y()).thenComparingInt(row -> row.entry().spot().x());
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
		for (Entry entry : menu ? MENU : WORLD) {
			drawOutline(graphics, entry.spot(), bodyX, bodyY, held(pad, entry.spot()));
		}

		graphics.pose().popMatrix();
		return true;
	}

	private static boolean held(final Gamepad pad, final Spot spot) {
		return switch (spot.stick()) {
			case LEFT_STICK -> Math.abs(pad.leftX()) > 0.3F || Math.abs(pad.leftY()) > 0.3F;
			case RIGHT_STICK -> Math.abs(pad.rightX()) > 0.3F || Math.abs(pad.rightY()) > 0.3F;
			default -> pad.held(spot.button());
		};
	}

	private static void drawLabels(
		final GuiGraphicsExtractor graphics, final Font font, final List<Row> rows, final boolean left, final int columnX, final int columnWidth,
		final int bodyX, final int bodyY, final int top, final Gamepad pad
	) {
		int y = top;
		for (Row row : rows) {
			Spot spot = row.entry().spot();
			boolean held = held(pad, spot);
			int color = held ? HELD : spot.color();
			int textX = left ? columnX + columnWidth - row.textWidth() : columnX;
			graphics.text(font, row.entry().label(), textX, y + 1, color, false);

			// A horizontal run from the label to the button's column, then a vertical one to the button.
			int lineY = y + 5;
			int targetX = bodyX + spot.x() * PX + PX / 2;
			int targetY = bodyY + spot.y() * PX + PX / 2;
			int startX = left ? columnX + columnWidth + 2 : columnX - 2;
			int lineColor = held ? HELD : ARGB.color(190, OUTLINE);
			graphics.fill(Math.min(startX, targetX), lineY, Math.max(startX, targetX) + 1, lineY + 1, lineColor);
			graphics.fill(targetX, Math.min(lineY, targetY), targetX + 1, Math.max(lineY, targetY) + 1, lineColor);
			y += ROW;
		}
	}

	private static void drawOutline(final GuiGraphicsExtractor graphics, final Spot spot, final int bodyX, final int bodyY, final boolean held) {
		int x0 = bodyX + (spot.x() - spot.width() / 2) * PX - 1;
		int y0 = bodyY + (spot.y() - spot.height() / 2) * PX - 1;
		int x1 = bodyX + (spot.x() + spot.width() / 2 + 1) * PX + 1;
		int y1 = bodyY + (spot.y() + spot.height() / 2 + 1) * PX + 1;
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
