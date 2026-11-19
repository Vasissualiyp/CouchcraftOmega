package dev.couchcraft.compat.dh;

import dev.couchcraft.context.Couch;
import dev.couchcraft.context.PlayerContext;
import net.minecraft.client.player.LocalPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-player positions for Distant Horizons.
 * <p>
 * Couchcraft swaps each player's level and player into the Minecraft singleton while it renders,
 * and DH reads that singleton from its own timer thread. Whichever player was swapped in at that
 * instant became the centre of DH's LOD tree, which made detail flicker. The render thread
 * publishes an immutable snapshot of every player here so other threads never touch the swap.
 */
public final class CouchDhState
{
	public record Anchor(String dimension, int x, int z) { }
	
	/** Primary player first. Empty when Couchcraft has no players, so DH behaves normally. */
	private static volatile List<Anchor> anchors = List.of();
	
	/** The Nether fog colour vanilla's own fog renderer produced for a player (r, g, b), or null. */
	public static volatile float[] netherFogColor;
	/** True while DH runs its own private fog colour computation, so it is not mistaken for vanilla's. */
	public static volatile boolean computingDhFogColor;
	
	private CouchDhState() { }
	
	/** Render thread only. */
	public static void refresh()
	{
		List<PlayerContext> contexts = Couch.contexts();
		if (contexts.size() < 2)
		{
			anchors = List.of();
			return;
		}
		
		List<Anchor> next = new ArrayList<>(contexts.size());
		for (PlayerContext context : contexts)
		{
			LocalPlayer player = context.isRemoved() ? null : context.player();
			if (player == null)
			{
				continue;
			}
			next.add(new Anchor(
				player.level().dimension().identifier().toString(),
				player.blockPosition().getX(),
				player.blockPosition().getZ()));
		}
		anchors = List.copyOf(next);
	}
	
	/** The host, or null when there is no split screen. */
	public static Anchor firstAnchor()
	{
		List<Anchor> current = anchors;
		return current.isEmpty() ? null : current.getFirst();
	}
	
	/** The first player (host first) in the given dimension, or null if there is none. */
	public static Anchor anchorIn(String dimension)
	{
		for (Anchor anchor : anchors)
		{
			if (anchor.dimension().equals(dimension))
			{
				return anchor;
			}
		}
		return null;
	}
	
	/** Distance from (x, z) to the nearest player in the dimension, or NaN if there is none. */
	public static double nearestDistance(String dimension, int x, int z)
	{
		double best = Double.NaN;
		for (Anchor anchor : anchors)
		{
			if (!anchor.dimension().equals(dimension))
			{
				continue;
			}
			double dx = anchor.x() - x;
			double dz = anchor.z() - z;
			double distance = Math.sqrt(dx * dx + dz * dz);
			if (Double.isNaN(best) || distance < best)
			{
				best = distance;
			}
		}
		return best;
	}
}
