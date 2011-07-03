package org.selurgniman.bukkit.mcplayerstats;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.selurgniman.bukkit.mcplayerstats.listeners.BlockActionListener;
import org.selurgniman.bukkit.mcplayerstats.listeners.EntityActionListener;
import org.selurgniman.bukkit.mcplayerstats.listeners.PlayerActionListener;
import org.selurgniman.bukkit.mcplayerstats.listeners.VehicleActionListener;
import org.selurgniman.bukkit.mcplayerstats.model.PlayerStatsModel;

/**
 * Sample plugin for Bukkit
 * 
 * @author Dinnerbone
 */
public class McPlayerStats extends JavaPlugin
{
	private final Logger log = Logger.getLogger("Minecraft."
			+ McPlayerStats.class.getName());

	private final static ConcurrentMap<Player, Calendar> playerLastLogin= new ConcurrentHashMap<Player, Calendar>();;
	private final static ConcurrentMap<String, Location> playerLastLocations = new ConcurrentHashMap<String, Location>();;

	private final PlayerStatsModel model = new PlayerStatsModel();

	// NOTE: There should be no need to define a constructor any more for more
	// info on moving from
	// the old constructor see:
	// http://forums.bukkit.org/threads/too-long-constructor.5032/

	public void onDisable()
	{
		// TODO: Place any custom disable code here

		model.close();
		
		// NOTE: All registered events are automatically unregistered when a
		// plugin is disabled

		log.info("McPlayerStats shut down");
	}

	public void onEnable()
	{ 
		model.open();
				
		for (World world : this.getServer().getWorlds())
		{
			for (Player player : world.getPlayers())
			{
				McPlayerStats
						.setPlayerLastLogin(player, Calendar.getInstance());
				log.info("Existing player found: "
						+ player.getName()
						+ ". Creating login time as current.");
			}
		}

		// Register our events
		PlayerActionListener playerActionListener = new PlayerActionListener(model);
		BlockActionListener blockActionListener = new BlockActionListener(model);
		VehicleActionListener vehicleActionListener = new VehicleActionListener(model);
		EntityActionListener entityActionListener = new EntityActionListener(model);

		PluginManager pm = getServer().getPluginManager();
		for (Event.Type eventType : Event.Type.values())
		{
			String eventName = eventType.toString().toUpperCase();

			// Error said "PLAYER_INVENTORY not supported, so excluding it.
			if (eventName.startsWith("PLAYER")
					&& eventType != Event.Type.PLAYER_INVENTORY)
			{
				pm.registerEvent(
						eventType,
						playerActionListener,
						Priority.Normal,
						this);
			}
			else if (eventName.startsWith("BLOCK"))
			{
				pm.registerEvent(
						eventType,
						blockActionListener,
						Priority.Normal,
						this);
			}
			else if (eventName.startsWith("VEHICLE"))
			{
				pm.registerEvent(
						eventType,
						vehicleActionListener,
						Priority.Normal,
						this);
			}
			else if (eventName.startsWith("ENTITY"))
			{
				pm.registerEvent(
						eventType,
						entityActionListener,
						Priority.Normal,
						this);
			}
		}

		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName()
				+ " version "
				+ pdfFile.getVersion()
				+ " is enabled!");

	}

	public static void setPlayerLastLocation(String player, Location location)
	{
		playerLastLocations.put(player, location);
	}

	public static Location getPlayerLastLocation(String player)
	{
		return playerLastLocations.get(player);
	}

	public static void setPlayerLastLogin(Player player, Calendar calendar)
	{
		playerLastLogin.put(player, calendar);
	}

	public static Calendar getPlayerLastLogin(Player player)
	{
		return playerLastLogin.get(player);
	}
}