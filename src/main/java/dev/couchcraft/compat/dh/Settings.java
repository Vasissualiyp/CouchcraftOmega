package dev.couchcraft.compat.dh;

import com.seibel.distanthorizons.api.enums.config.EDhApiVerticalQuality;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Tunables, read once from {@code config/couch-dh-compat.properties} (written with defaults on first run). */
public final class Settings
{
	/** Distance in blocks where DH's fog starts on LODs in the Nether. Negative means the vanilla chunk edge. */
	public static final float NETHER_LOD_FOG_START_BLOCKS;
	/** Distance in blocks where DH's fog on LODs in the Nether is complete. */
	public static final float NETHER_LOD_FOG_END_BLOCKS;
	
	/** Minimum DH fog thickness on LODs in the Nether (0 none, 1 solid fog colour). 1 is a diagnostic. */
	public static final float NETHER_LOD_FOG_MIN;
	/** In the Nether, LODs fade into the background colour up to this distance in blocks. 0 disables. */
	public static final float NETHER_FAR_FADE_END_BLOCKS;
	/** Vertical detail DH uses for LODs in dimensions with a ceiling (the Nether). */
	public static final EDhApiVerticalQuality NETHER_VERTICAL_QUALITY;
	/** Whether DH fogs LODs outside the Nether (overworld, End). */
	public static final boolean OTHER_DIMENSIONS_LOD_FOG;
	/** Logs the fog parameters DH is rendering with, every few seconds in the Nether. */
	public static final boolean DEBUG_LOG;
	
	static
	{
		Properties defaults = new Properties();
		defaults.setProperty("netherLodFogStartBlocks", "-1");
		defaults.setProperty("netherLodFogEndBlocks", "400");
		defaults.setProperty("netherLodFogMin", "0.0");
		defaults.setProperty("netherFarFadeEndBlocks", "400");
		defaults.setProperty("netherVerticalQuality", "EXTREME");
		defaults.setProperty("otherDimensionsLodFog", "false");
		defaults.setProperty("debugLog", "false");
		
		Properties props = new Properties(defaults);
		try
		{
			Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("couch-dh-compat.properties");
			if (Files.exists(file))
			{
				try (InputStream in = Files.newInputStream(file))
				{
					props.load(in);
				}
			}
			else
			{
				Files.createDirectories(file.getParent());
				try (OutputStream out = Files.newOutputStream(file))
				{
					defaults.store(out, "Couchcraft x Distant Horizons. Takes effect on the next launch.");
				}
			}
		}
		catch (IOException | RuntimeException ignored)
		{
			// fall back to the defaults
		}
		
		NETHER_LOD_FOG_START_BLOCKS = read(props, "netherLodFogStartBlocks", -1f);
		NETHER_LOD_FOG_END_BLOCKS = read(props, "netherLodFogEndBlocks", 400f);
		NETHER_LOD_FOG_MIN = read(props, "netherLodFogMin", 0f);
		NETHER_FAR_FADE_END_BLOCKS = read(props, "netherFarFadeEndBlocks", 400f);
		EDhApiVerticalQuality quality;
		try
		{
			quality = EDhApiVerticalQuality.valueOf(props.getProperty("netherVerticalQuality").trim().toUpperCase());
		}
		catch (RuntimeException e)
		{
			quality = EDhApiVerticalQuality.EXTREME;
		}
		NETHER_VERTICAL_QUALITY = quality;
		OTHER_DIMENSIONS_LOD_FOG = Boolean.parseBoolean(props.getProperty("otherDimensionsLodFog"));
		DEBUG_LOG = Boolean.parseBoolean(props.getProperty("debugLog"));
	}
	
	private Settings() { }
	
	private static float read(Properties props, String key, float fallback)
	{
		try
		{
			return Float.parseFloat(props.getProperty(key));
		}
		catch (RuntimeException e)
		{
			return fallback;
		}
	}
	
	public static boolean inNether()
	{
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.level != null && minecraft.level.dimension() == Level.NETHER;
	}
}
