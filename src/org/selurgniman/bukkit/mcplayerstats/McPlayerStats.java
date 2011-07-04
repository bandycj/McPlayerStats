package org.selurgniman.bukkit.mcplayerstats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
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
import org.bukkit.util.config.Configuration;
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
public class McPlayerStats extends JavaPlugin {
	private final Logger log = Logger.getLogger("Minecraft." + McPlayerStats.class.getName());
	private final static ConcurrentMap<Player, Calendar> playerLastLogin = new ConcurrentHashMap<Player, Calendar>();
	private final static ConcurrentMap<String, Location> playerLastLocations = new ConcurrentHashMap<String, Location>();
	private static final LinkedHashMap<String, String> CONFIG_DEFAULTS = new LinkedHashMap<String, String>();
	private static Configuration config = null;
	private static List<String> debugPlayers = new ArrayList<String>();
	private final PlayerStatsModel model = new PlayerStatsModel();
	private static McPlayerStats self = null;
	static {
		CONFIG_DEFAULTS.put("connectString", "jdbc:sqlserver://127.0.0.1;databaseName=MCStats;");
		CONFIG_DEFAULTS.put("dbUser", "mcsql");
		CONFIG_DEFAULTS.put("dbPass", "mcsql101");
		CONFIG_DEFAULTS.put("debugUsers", "none,nada");
	}

	public void onDisable() {
		// TODO: Place any custom disable code here

		model.close();

		// NOTE: All registered events are automatically unregistered when a
		// plugin is disabled

		log.info("McPlayerStats shut down");
	}

	public void onEnable() {
		self = this;
		loadConfig();
		model.initialize();

		for (World world : this.getServer().getWorlds()) {
			for (Player player : world.getPlayers()) {
				McPlayerStats.setPlayerLastLogin(player, Calendar.getInstance());
				log.info("Existing player found: " + player.getName() + ". Creating login time as current.");
			}
		}

		// Register our events
		PlayerActionListener playerActionListener = new PlayerActionListener(model);
		BlockActionListener blockActionListener = new BlockActionListener(model);
		VehicleActionListener vehicleActionListener = new VehicleActionListener(model);
		EntityActionListener entityActionListener = new EntityActionListener(model);

		PluginManager pm = getServer().getPluginManager();
		for (Event.Type eventType : Event.Type.values()) {
			String eventName = eventType.toString().toUpperCase();

			// Error said "PLAYER_INVENTORY not supported, so excluding it.
			if (eventName.startsWith("PLAYER") && eventType != Event.Type.PLAYER_INVENTORY) {
				pm.registerEvent(eventType, playerActionListener, Priority.Normal, this);
			} else if (eventName.startsWith("BLOCK")) {
				pm.registerEvent(eventType, blockActionListener, Priority.Normal, this);
			} else if (eventName.startsWith("VEHICLE")) {
				pm.registerEvent(eventType, vehicleActionListener, Priority.Normal, this);
			} else if (eventName.startsWith("ENTITY")) {
				pm.registerEvent(eventType, entityActionListener, Priority.Normal, this);
			}
		}

		// EXAMPLE: Custom code, here we just output some info so we can check
		// all is well
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");

	}

	public static void setPlayerLastLocation(String player, Location location) {
		playerLastLocations.put(player, location);
	}

	public static void removePlayerLastLocation(String player) {
		playerLastLocations.remove(player);
	}

	public static Location getPlayerLastLocation(String player) {
		return playerLastLocations.get(player);
	}

	public static void setPlayerLastLogin(Player player, Calendar calendar) {
		playerLastLogin.put(player, calendar);
	}

	public static Calendar getPlayerLastLogin(Player player) {
		return playerLastLogin.get(player);
	}

	public static Configuration getConfig() {
		return McPlayerStats.config;
	}

	private void loadConfig() {
		config = this.getConfiguration();
		for (Entry<String, String> entry : CONFIG_DEFAULTS.entrySet()) {
			if (config.getProperty(entry.getKey()) == null) {
				config.setProperty(entry.getKey(), entry.getValue());
			}
		}
		config.save();

		try {
			String[] debugPlayers = config.getString("debugUsers").split(",");
			McPlayerStats.debugPlayers = Arrays.asList(debugPlayers);
		} catch (NullPointerException ex) {
			// debugUsers was undefined in the config file.
		}
	}

	public static void debugMsg(String message) {
		if (self != null && debugPlayers.size() > 0) {
			List<World> worlds = self.getServer().getWorlds();
			for (World world : worlds) {
				List<Player> players = world.getPlayers();
				for (Player player : players) {
					if (debugPlayers.contains(player.getName())) {
						player.sendMessage(message);
					}
				}
			}
		}
	}
}