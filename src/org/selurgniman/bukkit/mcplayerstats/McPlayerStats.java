package org.selurgniman.bukkit.mcplayerstats;

import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

/**
 * Sample plugin for Bukkit
 * 
 * @author Dinnerbone
 */
public class McPlayerStats extends JavaPlugin {
	private final Logger log = Logger.getLogger("Minecraft." + McPlayerStats.class.getName());
	private final String PLAYER_EVENT = "PLAYER_EVENT";
	private Configuration config = null;

	private final MCStatsDB database = new MCStatsDB(
			"jdbc:sqlserver://127.0.0.1;databaseName=MCStats;",
			"mcsql",
			"mcsql101");

	private Hashtable<String, Integer> categories;
	private Hashtable<String, Integer> statistics;
	private Hashtable<String, Integer> players;
	private Hashtable<String, Calendar> playerLastLogin;

	private Hashtable<String, Location> playerLastLocations;
	

	// NOTE: There should be no need to define a constructor any more for more
	// info on moving from
	// the old constructor see:
	// http://forums.bukkit.org/threads/too-long-constructor.5032/

	public void onDisable() {
		// TODO: Place any custom disable code here

		try {
			database.Close();
		} catch (Exception e) {
			log.info(e.toString());
		}
		// NOTE: All registered events are automatically unregistered when a
		// plugin is disabled

		config.save();
		log.info("McPlayerStats shut down");
	}

	public void onEnable() {
		config = this.getConfiguration();
		config.load();
		
		playerLastLocations = new Hashtable<String, Location>();
		playerLastLogin	= new Hashtable<String, Calendar>();

		try {
			database.Open();

			categories = database.getCategories();
			statistics = database.getStatistics();
			players = database.getPlayers();

		} catch (Exception e) {
			log.info(e.toString());
		}
		
		for (World world:this.getServer().getWorlds()){
    		String worldName=world.getName();
    		
    		for (Player player:world.getPlayers())
    		{
    			playerLastLogin.put(player.getName(), Calendar.getInstance());
    			log.info("Existing player found: " + player.getName() + ". Creating login time as current.");
    		}
    	}
		

		// Register our events
		PlayerActionListener playerActionListener = new PlayerActionListener();
		BlockActionListener blockActionListener = new BlockActionListener();
		VehicleActionListener vehicleActionListener = new VehicleActionListener();
		EntityActionListener entityActionListener = new EntityActionListener();

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

	private int getPlayerId(String name) throws Exception {
		Integer value;

		if (!players.containsKey(name.toLowerCase())) {
			log.info("Added Player Key: " + name.toLowerCase());
			database.AddPlayer(name);
			players = database.getPlayers();
		}

		value = players.get(name.toLowerCase());
		return value.intValue();
	}

	private int getStatisticId(String name) throws Exception {
		Integer value;

		if (!statistics.containsKey(name.toLowerCase())) {
			log.info("Added Statistic Key: " + name.toLowerCase());
			database.AddStatistic(name);
			statistics = database.getStatistics();

		}

		value = statistics.get(name.toLowerCase());
		return value.intValue();
	}

	private int getCategoryId(String name) throws Exception {
		Integer value;

		if (!categories.containsKey(name.toLowerCase())) {
			log.info("Added Category Key: " + name.toLowerCase());
			database.AddCategory(name);
			categories = database.getCategories();
		}

		value = categories.get(name.toLowerCase());
		return value.intValue();
	}
	
	private void IncrementStatistic(PlayerEvent event)
	{
		//These are all events that are single events with no blocks or items.
		
		IncrementStatistic(event.getPlayer(), PLAYER_EVENT, event.getEventName().toString(), 1);
	}
	
	private void IncrementStatistic(BlockEvent event, Player player)
	{
		//player.sendMessage(String.format("You %1s %2s %3s", event.getEventName().toString(), 1, event.getBlock().getType().toString() ));
		
		IncrementStatistic(player, event.getBlock().getType().toString(), event.getEventName().toString(), 1);
	}

	private void IncrementStatistic(PlayerEvent event, ItemStack item)
	{
		//event.getPlayer().sendMessage(String.format("You %1s %2s %3s", event.getEventName().toString(), item.getAmount(), item.getType().toString()));
		
		IncrementStatistic(event.getPlayer(), item.getType().toString(), event.getEventName().toString(), item.getAmount());
	}
	
	private void IncrementStatistic(Player player, String statisticName, String categoryName, int amount) {
		int categoryId;
		int playerId;
		int statisticId;

		//statisticName = statisticName.replace("_", "");

		try {
			categoryId = getCategoryId(categoryName);
			playerId = getPlayerId(player.getName());
			statisticId = getStatisticId(statisticName);

			database.IncrementPlayerStatistic(playerId, categoryId, statisticId, amount);
		} catch (Exception e) {
			log.info("Couldn't increment statistic: " + statisticName + ", category: " + categoryName + ", player: " + player.getDisplayName());
			log.info(e.toString());
		}
	}

	private class PlayerActionListener extends PlayerListener {
		/**
		 * Count player item drops.
		 */
		@Override
		public void onPlayerDropItem(PlayerDropItemEvent event) {
			IncrementStatistic(event, event.getItemDrop().getItemStack());
		}

		/**
		 * Count player item pickups.
		 */
		@Override
		public void onPlayerPickupItem(PlayerPickupItemEvent event) {
			IncrementStatistic(event, event.getItem().getItemStack());
		}

		/**
		 * Count player arm swings.
		 */
		@Override
		public void onPlayerAnimation(PlayerAnimationEvent event) {
			Player player = event.getPlayer();
			
			if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
				IncrementStatistic(player, event.getAnimationType().toString(), event.getEventName().toString(), 1);
			}
		}

		/**
		 * Count player chat.
		 */
		@Override
		public void onPlayerChat(PlayerChatEvent event) {
			IncrementStatistic(event);
			IncrementStatistic(event.getPlayer(), "chat_letters", event.getEventName().toString(), event.getMessage().length());
		}

		/**
		 * Count player commands issued.
		 */
		@Override
		public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
			IncrementStatistic(event);
		}

		/**
		 * Count player logins.
		 */
		@Override
		public void onPlayerLogin(PlayerLoginEvent event) {
			Player player;
        	
        	player = event.getPlayer();
        	
        	playerLastLogin.put(player.getName(), Calendar.getInstance());
        	
        	try {
        		database.UpdatePlayerLastLoggedIn(getPlayerId(player.getName()));			
			} catch (Exception e) {
				log.info("Error updating Player Last Logged In value.");
				log.info(e.toString());
			}
			
			IncrementStatistic(event);
		}

		/**
		 * Count player logouts.
		 */
		@Override
		public void onPlayerQuit(PlayerQuitEvent event) {
			double playerSecondsLoggedIn;
        	Calendar playerLogin;        	
        	Player player;
        	
        	player = event.getPlayer();
        	
        	playerLogin = playerLastLogin.get(player.getName());
        	
        	if (playerLogin != null)
        	{
        		Calendar now;
        		
        		now = Calendar.getInstance();
        		
        		playerSecondsLoggedIn = ((now.getTimeInMillis() - playerLogin.getTimeInMillis()) / 1000);
        		
        		IncrementStatistic(player, "playedfor", event.getEventName().toString(), (int)playerSecondsLoggedIn);
        	}
        	
        	try {
        		database.UpdatePlayerLastLoggedOut(getPlayerId(player.getName()));			
			} catch (Exception e) {
				log.info("Error updating Player Last Logged Out value.");
				log.info(e.toString());
			}
			
			IncrementStatistic(event);
		}

		/**
		 * Count player steps.
		 */
		@Override
		public void onPlayerMove(PlayerMoveEvent event) {
			Location lastLocation;

			Player player = event.getPlayer();

			lastLocation = playerLastLocations.get(player.getName().toString());

			if (lastLocation == null) {
				playerLastLocations.put(player.getName().toString(), event.getTo());
			} else {
				// Only count if they've moved at least 1 block in some
				// direction except up/down?

				int x1, x2;
				int y1, y2;

				x1 = lastLocation.getBlockX();
				x2 = event.getTo().getBlockX();

				y1 = lastLocation.getBlockZ();
				y2 = event.getTo().getBlockZ();

				if (x1 != x2 || y1 != y2) {
					int distance;

					distance = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

					if (distance >= 1) {
						playerLastLocations.put(player.getName(), event.getTo());
						IncrementStatistic(player, PLAYER_EVENT, event.getEventName(), distance);
					}
				}

			}
		}

		/**
		 * Count player respawns.
		 */
		@Override
		public void onPlayerRespawn(PlayerRespawnEvent event) {
			Player player = event.getPlayer();

			IncrementStatistic(player, PLAYER_EVENT, event.getEventName(), 1);
		}

		/**
		 * Count player teleports.
		 */
		@Override
		public void onPlayerTeleport(PlayerTeleportEvent event) {
			Player player = event.getPlayer();

			//Teleports happens when people use vehicles, enter the world and get pushed back by something
			//IncrementStatistic(player, PLAYER_EVENT, event.getEventName(), 1);
		}
	}

	
	
	private class BlockActionListener extends BlockListener {
		/**
		 * Count player block places.
		 */
		@Override
		public void onBlockPlace(BlockPlaceEvent event) {
			IncrementStatistic(event, event.getPlayer());
		}

		/**
		 * Count player block breaks.
		 */
		@Override
		public void onBlockBreak(BlockBreakEvent event) {
			IncrementStatistic(event, event.getPlayer());
		}
	}

	private class VehicleActionListener extends VehicleListener {
		/**
		 * Count player vehicle distance.
		 */
		@Override
		public void onVehicleMove(VehicleMoveEvent event) {
			Entity passenger = event.getVehicle().getPassenger();
			if (passenger instanceof Player) {
				Player player = (Player) passenger;

				Location lastLocation;
				String key;

				String categoryName = null;

				if (event.getVehicle() instanceof Boat) {
					categoryName = "boat";
				} else if (event.getVehicle() instanceof Minecart) {
					categoryName = "minecart";
				}

				if (categoryName != null) {
					key = player.getName() + "v" + categoryName;

					lastLocation = playerLastLocations.get(key);

					if (lastLocation == null) {
						playerLastLocations.put(key, event.getTo());
					} else {
						// Only count if they've moved at least 1 block in some
						// direction except up/down?

						int x1, x2;
						int y1, y2;

						x1 = lastLocation.getBlockX();
						x2 = event.getTo().getBlockX();

						y1 = lastLocation.getBlockZ();
						y2 = event.getTo().getBlockZ();

						if (x1 != x2 || y1 != y2) {
							int distance;

							distance = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

							if (distance >= 1) {
								playerLastLocations.put(key, event.getTo());

								IncrementStatistic(player, event.getVehicle().toString().replace("Craft", ""), event.getEventName().toString(), distance);
							}
						}

					}
				}

			}
		}

		/**
		 * Count player vehicle uses.
		 */
		@Override
		public void onVehicleEnter(VehicleEnterEvent event) {
			Player player; 
			Vehicle vehicle;
			
			Entity passenger = event.getVehicle().getPassenger();
			
			if (passenger instanceof Player) {
				vehicle = event.getVehicle();
				
				if (vehicle != null) {
					player = (Player)passenger;
				
					if (player.isInsideVehicle())
					{
						IncrementStatistic(player, event.getVehicle().toString().replace("Craft", ""), event.getEventName(), 1);
					}
				}
			}
		}
	}

	private class EntityActionListener extends EntityListener {
		private IdentityHashMap<Player, String> lastDamagedBy = new IdentityHashMap<Player, String>();

		/**
		 * Count player damage delt and recieved.
		 */
		@Override
    	public void onEntityDamage(EntityDamageEvent event) {
    		Entity damagee = null;
    		// ****************************************************************
        	
        	Entity damager = null;           	
        	Integer damage = null;
        	String cause = null;
    		// ****************************************************************
        	
        	/**
        	 * FIRE_TICK causes wierd bugs so check for it first
        	 */
        	//if (event.getCause() == DamageCause.FIRE_TICK) {
    			// ****************************************************************
        	
        	damagee = event.getEntity();				
        	damage = event.getDamage(); 
        	
        	if (event instanceof EntityDamageByEntityEvent)
        	{
        		damager = ((EntityDamageByEntityEvent)event).getDamager();
        	}

        	if (damagee instanceof Player || damager instanceof Player)
        	{
        		if (event instanceof EntityDamageByBlockEvent)
        		{
        			if (((EntityDamageByBlockEvent) event).getDamager() != null)
        			{
        				cause = ((EntityDamageByBlockEvent)event).getDamager().getType().toString();					
        			}
        			else
        			{
        				cause = damagee.getLocation().getBlock().getType().toString();
        			}
        		} 
        		else if (damager != null)
        		{        			
        			cause = damager.toString();
        		}
        		else 
        		{
        			cause = event.getCause().toString();
        		} 

        		cause = cause.replace("Craft", "");
        		
        		if (damagee instanceof Player){
        			lastDamagedBy.put((Player)damagee, cause);
        			IncrementStatistic((Player)damagee, cause, event.getEventName() + "_TAKEN", damage.intValue());
        		} 
        		else
        		{
        			IncrementStatistic((Player)damager, damagee.toString().replace("Craft", ""), event.getEventName() + "_DEALT", damage.intValue());
        		}
        	}			
    	}

		/**
		 * Count player deaths, location of death and cause of death.
		 */
		@Override
		public void onEntityDeath(EntityDeathEvent event) {
			Entity entity = event.getEntity();
			if (entity instanceof Player) {
				// ************************************************************
				Player player = (Player) entity;
				@SuppressWarnings("unused")
				Location location = player.getLocation();
				String cause = lastDamagedBy.get(player);
				// ************************************************************

				if (cause == null) {
					cause = "UNKNOWN";
				}

				//Better event to distinguish these automatically?
				IncrementStatistic(player, cause, "ENTITY_KILLED_PLAYER", 1);
			} 
			else 
			{
				EntityDamageEvent causedEvent = entity.getLastDamageCause();
				
				if (causedEvent instanceof EntityDamageByEntityEvent)
				{
					Entity damager = ((EntityDamageByEntityEvent)causedEvent).getDamager();
					
					if (damager != null && damager instanceof Player)
					{
						IncrementStatistic((Player)damager, entity.toString().replace("Craft", ""), "PLAYER_KILLED_ENTITY", 1);
					}
				}
			}
		}
	}
}