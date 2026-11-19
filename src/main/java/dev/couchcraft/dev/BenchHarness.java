package dev.couchcraft.dev;

import com.mojang.logging.LogUtils;
import dev.couchcraft.context.Couch;
import dev.couchcraft.context.PlayerContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.InactivityFpsLimit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

/**
 * Development-only benchmark, inert unless {@code -Dcouchcraft.bench=<result file>} is set. In a fresh world with a fixed
 * seed it adds players one at a time (1 to 4) and, for each count, measures frame times and the time until every view
 * has all its chunks drawn: after sending everyone to unexplored overworld terrain, while standing still, while flying
 * forward into new chunks, and the same in the Nether. Results are appended to the file, one JSON object per line.
 * Run it with {@code ./gradlew runClient -PcouchBench=<file>} on a deleted {@code run/saves/BenchWorld}.
 */
public final class BenchHarness {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String OUT = System.getProperty("couchcraft.bench");
	private static final String WORLD = "BenchWorld";
	private static final long SEED = 20261118L;
	private static final int PLAYERS = Integer.getInteger("couchcraft.benchPlayers", 4);
	private static final int RENDER_DISTANCE = Integer.getInteger("couchcraft.benchDistance", 16);
	/** Nearly all of the chunks in view must have arrived before a view counts as loaded. */
	private static final int MIN_CHUNKS = (int)(0.98 * (2 * (RENDER_DISTANCE - 2) + 1) * (2 * (RENDER_DISTANCE - 2) + 1));
	private static final long STABLE_MILLIS = 2_000L;
	private static final long IDLE_MILLIS = 8_000L;
	private static final long EXPLORE_MILLIS = 12_000L;
	private static final long READY_TIMEOUT_MILLIS = 300_000L;

	private static final Deque<Task> TASKS = new ArrayDeque<>();
	private static boolean worldRequested;
	private static boolean built;
	private static long lastFrameNanos;
	private static List<Double> frames;
	private static String windowLabel;

	private BenchHarness() {
	}

	private interface Task {
		/** Returns true when the task is finished. */
		boolean tick(Minecraft minecraft);
	}

	private static final class Action implements Task {
		private final Runnable action;

		Action(final Runnable action) {
			this.action = action;
		}

		@Override
		public boolean tick(final Minecraft minecraft) {
			this.action.run();
			return true;
		}
	}

	private static final class Until implements Task {
		private final String label;
		private final java.util.function.BooleanSupplier condition;
		private final long timeoutMillis;
		private final boolean report;
		private final long stableMillis;
		private long start = -1L;
		private long trueSince = -1L;

		Until(final String label, final java.util.function.BooleanSupplier condition, final long timeoutMillis, final boolean report) {
			this(label, condition, timeoutMillis, report, 0L);
		}

		/** The condition must hold continuously for {@code stableMillis}; the reported time is when it first held. */
		Until(final String label, final java.util.function.BooleanSupplier condition, final long timeoutMillis, final boolean report, final long stableMillis) {
			this.label = label;
			this.condition = condition;
			this.timeoutMillis = timeoutMillis;
			this.report = report;
			this.stableMillis = stableMillis;
		}

		@Override
		public boolean tick(final Minecraft minecraft) {
			long now = System.nanoTime();
			if (this.start < 0L) {
				this.start = now;
			}

			long elapsed = (now - this.start) / 1_000_000L;
			if (this.condition.getAsBoolean()) {
				if (this.trueSince < 0L) {
					this.trueSince = now;
				}
			} else {
				this.trueSince = -1L;
			}

			boolean done = this.trueSince >= 0L && (now - this.trueSince) / 1_000_000L >= this.stableMillis;
			if (done || elapsed > this.timeoutMillis) {
				if (this.report) {
					double seconds = done ? (this.trueSince - this.start) / 1_000_000_000.0 : elapsed / 1000.0;
					record("{\"label\":\"%s\",\"seconds\":%.2f,\"timedOut\":%b}", this.label, seconds, !done);
				}

				return true;
			}

			return false;
		}
	}

	private static final class Measure implements Task {
		private final String label;
		private final long millis;
		private final Runnable perSecond;
		private long start = -1L;
		private long lastSecond;

		Measure(final String label, final long millis, final Runnable perSecond) {
			this.label = label;
			this.millis = millis;
			this.perSecond = perSecond;
		}

		@Override
		public boolean tick(final Minecraft minecraft) {
			long now = System.nanoTime();
			if (this.start < 0L) {
				this.start = now;
				frames = new ArrayList<>();
				windowLabel = this.label;
			}

			long elapsed = (now - this.start) / 1_000_000L;
			if (elapsed / 1000L > this.lastSecond) {
				this.lastSecond = elapsed / 1000L;
				if (this.perSecond != null) {
					this.perSecond.run();
				}
			}

			if (elapsed >= this.millis) {
				finishWindow();
				return true;
			}

			return false;
		}
	}

	private static void finishWindow() {
		if (frames == null || frames.isEmpty()) {
			frames = null;
			return;
		}

		double[] sorted = frames.stream().mapToDouble(Double::doubleValue).sorted().toArray();
		double sum = Arrays.stream(sorted).sum();
		double mean = sum / sorted.length;
		record(
			"{\"label\":\"%s\",\"frames\":%d,\"fps\":%.1f,\"meanMs\":%.2f,\"p50Ms\":%.2f,\"p95Ms\":%.2f,\"p99Ms\":%.2f,\"maxMs\":%.2f,\"worstFps1pct\":%.1f}",
			windowLabel, sorted.length, 1000.0 / mean, mean, percentile(sorted, 0.50), percentile(sorted, 0.95), percentile(sorted, 0.99), sorted[sorted.length - 1],
			1000.0 / Math.max(0.001, tailMean(sorted, 0.01))
		);
		frames = null;
	}

	private static double percentile(final double[] sorted, final double p) {
		return sorted[Math.min(sorted.length - 1, (int)Math.floor(p * sorted.length))];
	}

	/** Mean of the slowest fraction of frames (the "1% low" frame time). */
	private static double tailMean(final double[] sorted, final double fraction) {
		int count = Math.max(1, (int)Math.ceil(sorted.length * fraction));
		double sum = 0.0;
		for (int i = sorted.length - count; i < sorted.length; i++) {
			sum += sorted[i];
		}

		return sum / count;
	}

	private static void record(final String format, final Object... args) {
		String line = String.format(Locale.ROOT, format, args);
		LOGGER.info("[Couchcraft/bench] {}", line);
		try {
			Path path = Path.of(OUT);
			if (path.getParent() != null) {
				Files.createDirectories(path.getParent());
			}

			Files.writeString(path, line + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			LOGGER.warn("[Couchcraft/bench] Could not write {}", OUT, e);
		}
	}

	// ---------------------------------------------------------------- scenario

	private static void serverCommand(final String command) {
		var server = Minecraft.getInstance().getSingleplayerServer();
		if (server != null) {
			server.execute(() -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
		}
	}

	private static String name(final PlayerContext context) {
		LocalPlayer player = context.player();
		return player.getGameProfile().name();
	}

	private static boolean everyoneReady(final int count) {
		List<PlayerContext> contexts = Couch.contexts();
		if (contexts.size() < count) {
			return false;
		}

		for (PlayerContext context : contexts) {
			if (context.player() == null || !context.levelRenderer.hasRenderedAllSections() || loadedChunks(context) < MIN_CHUNKS) {
				return false;
			}
		}

		return true;
	}

	/** Chunks present within the view distance around this player's current position (not the old position, after a teleport). */
	private static int loadedChunks(final PlayerContext context) {
		if (!(context.player().level() instanceof net.minecraft.client.multiplayer.ClientLevel level)) {
			return 0;
		}

		int centerX = context.player().blockPosition().getX() >> 4;
		int centerZ = context.player().blockPosition().getZ() >> 4;
		int radius = RENDER_DISTANCE - 2;
		int found = 0;
		for (int x = centerX - radius; x <= centerX + radius; x++) {
			for (int z = centerZ - radius; z <= centerZ + radius; z++) {
				if (level.getChunkSource().getChunk(x, z, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false) != null) {
					found++;
				}
			}
		}

		return found;
	}

	private static boolean everyoneAt(final int count, final String dimension, final int[] expectedX, final int[] expectedZ) {
		List<PlayerContext> contexts = Couch.contexts();
		if (contexts.size() < count) {
			return false;
		}

		for (PlayerContext context : contexts) {
			LocalPlayer player = context.player();
			if (player == null) {
				return false;
			}

			if (!player.level().dimension().identifier().toString().equals(dimension)) {
				return false;
			}

			if (Math.abs(player.getX() - expectedX[context.slot]) > 16.0 || Math.abs(player.getZ() - expectedZ[context.slot]) > 16.0) {
				return false;
			}
		}

		return true;
	}

	/** Sends every player to their own spot for this player count, each in terrain nobody has visited. */
	private static void teleportAll(final int count, final String dimension, final int[] x, final int[] z, final int y) {
		for (PlayerContext context : Couch.contexts()) {
			if (context.slot < count && context.player() != null) {
				serverCommand("gamemode spectator " + name(context));
				serverCommand("execute in " + dimension + " run tp " + name(context) + " " + x[context.slot] + " " + y + " " + z[context.slot] + " " + (context.slot * 90) + " 12");
			}
		}
	}

	private static void flyAll(final int blocksPerSecond) {
		for (PlayerContext context : Couch.contexts()) {
			if (context.player() != null) {
				serverCommand("execute as " + name(context) + " run tp @s ^ ^ ^" + blocksPerSecond);
			}
		}
	}

	private static void build() {
		TASKS.add(new Until("world", () -> Minecraft.getInstance().player != null && Minecraft.getInstance().gui.screen() == null, 120_000L, false));
		TASKS.add(new Action(BenchHarness::configure));
		for (int count = 1; count <= PLAYERS; count++) {
			final int n = count;
			final String prefix = n + "p.";
			if (n > 1) {
				TASKS.add(new Action(() -> Couch.addGuestWithFreeProfile(Minecraft.getInstance(), null)));
				TASKS.add(new Until(prefix + "guest-connect", () -> Couch.contexts().size() >= n && Couch.contexts().stream().allMatch(c -> c.player() != null), 60_000L, true));
			}

			// Overworld: everyone to fresh terrain at once, so this is chunk generation, sending and meshing for n views.
			int[] ox = new int[4];
			int[] oz = new int[4];
			int[] nx = new int[4];
			int[] nz = new int[4];
			for (int slot = 0; slot < 4; slot++) {
				ox[slot] = n * 20_000 + slot * 3_000;
				oz[slot] = 0;
				nx[slot] = n * 2_000 + slot * 700;
				nz[slot] = 0;
			}

			TASKS.add(new Action(() -> teleportAll(n, "minecraft:overworld", ox, oz, 110)));
			TASKS.add(new Until(prefix + "overworld-arrive", () -> everyoneAt(n, "minecraft:overworld", ox, oz), 120_000L, true));
			TASKS.add(new Until(prefix + "overworld-load", () -> everyoneReady(n), READY_TIMEOUT_MILLIS, true, STABLE_MILLIS));
			TASKS.add(new Measure(prefix + "overworld-idle", IDLE_MILLIS, null));
			TASKS.add(new Action(() -> net.minecraft.client.Screenshot.grab(Minecraft.getInstance(), false)));
			TASKS.add(new Measure(prefix + "overworld-explore", EXPLORE_MILLIS, () -> flyAll(24)));
			TASKS.add(new Until(prefix + "overworld-explore-settle", () -> everyoneReady(n), READY_TIMEOUT_MILLIS, true, STABLE_MILLIS));

			// Nether: the first visit to each spot generates it.
			TASKS.add(new Action(() -> teleportAll(n, "minecraft:the_nether", nx, nz, 70)));
			TASKS.add(new Until(prefix + "nether-arrive", () -> everyoneAt(n, "minecraft:the_nether", nx, nz), 120_000L, true));
			TASKS.add(new Until(prefix + "nether-load", () -> everyoneReady(n), READY_TIMEOUT_MILLIS, true, STABLE_MILLIS));
			TASKS.add(new Measure(prefix + "nether-idle", IDLE_MILLIS, null));
			TASKS.add(new Action(() -> net.minecraft.client.Screenshot.grab(Minecraft.getInstance(), false)));
			TASKS.add(new Measure(prefix + "nether-explore", EXPLORE_MILLIS, () -> flyAll(12)));
			TASKS.add(new Until(prefix + "nether-explore-settle", () -> everyoneReady(n), READY_TIMEOUT_MILLIS, true, STABLE_MILLIS));
		}

		TASKS.add(new Action(() -> {
			record("{\"label\":\"done\"}");
			Minecraft.getInstance().stop();
		}));
	}

	private static void configure() {
		Minecraft minecraft = Minecraft.getInstance();
		Options options = minecraft.options;
		options.renderDistance().set(RENDER_DISTANCE);
		options.simulationDistance().set(Math.min(RENDER_DISTANCE, 10));
		options.enableVsync().set(false);
		options.framerateLimit().set(260);
		options.fullscreen().set(false);
		options.exclusiveFullscreen().set(false);
		options.inactivityFpsLimit().set(InactivityFpsLimit.MINIMIZED);
		options.pauseOnLostFocus = false;
		record(
			"{\"label\":\"config\",\"renderDistance\":%d,\"sodium\":%b,\"dh\":%b,\"width\":%d,\"height\":%d,\"fullscreen\":%b}",
			RENDER_DISTANCE, net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("sodium"), net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("distanthorizons"),
			minecraft.getWindow().getScreenWidth(), minecraft.getWindow().getScreenHeight(), options.fullscreen().get()
		);
		serverCommand("gamerule advance_time false");
		serverCommand("time set noon");
	}

	/** Called at the start of every frame, in the resting state. */
	public static void frame(final Minecraft minecraft) {
		if (OUT == null) {
			return;
		}

		long now = System.nanoTime();
		if (frames != null && lastFrameNanos != 0L) {
			frames.add((now - lastFrameNanos) / 1_000_000.0);
		}

		lastFrameNanos = now;

		if (!worldRequested && minecraft.level == null && minecraft.gui.overlay() == null && minecraft.gui.screen() != null) {
			worldRequested = true;
			LevelSettings settings = new LevelSettings(
				WORLD, GameType.CREATIVE, new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false), true, WorldDataConfiguration.DEFAULT
			);
			minecraft.createWorldOpenFlows()
				.createFreshLevel(WORLD, settings, new WorldOptions(SEED, true, false), WorldPresets::createNormalWorldDimensions, new TitleScreen());
			return;
		}

		if (!built) {
			built = true;
			build();
		}

		while (!TASKS.isEmpty() && TASKS.peek().tick(minecraft)) {
			TASKS.poll();
		}
	}
}
