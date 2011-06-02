package org.selurgniman.bukkit.mcplayerstats;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
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
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
	private final Logger log = Logger.getLogger("Minecraft."+McPlayerStats.class.getName());
    private Configuration config=null;
    
    // NOTE: There should be no need to define a constructor any more for more info on moving from
    // the old constructor see:
    // http://forums.bukkit.org/threads/too-long-constructor.5032/

    public void onDisable() {
        // TODO: Place any custom disable code here

        // NOTE: All registered events are automatically unregistered when a plugin is disabled

    	config.save();
        log.info("McPlayerStats shut down");
    }

    public void onEnable() {
    	config = this.getConfiguration();
    	config.load();
    	List<World> worlds=this.getServer().getWorlds();
    	
    	for (World world:worlds){
    		String worldName=world.getName();
    		List<String> rules=config.getStringList("GameWarden."+worldName+".extinct", null);
    		if (rules.size()==0){
    			config.setProperty("GameWarden."+world.getName(),"");
    			config.save();
    		}
    		
    		Collections.sort(rules);
    	}
    	
        // Register our events
    	PlayerActionListener playerActionListener = new PlayerActionListener();
    	BlockActionListener blockActionListener = new BlockActionListener();
    	VehicleActionListener vehicleActionListener = new VehicleActionListener();
    	EntityActionListener entityActionListener = new EntityActionListener();
    	
        PluginManager pm = getServer().getPluginManager();
        for (Event.Type eventType:Event.Type.values()){
        	String eventName=eventType.toString().toUpperCase();
        	if (eventName.startsWith("PLAYER")){
        		pm.registerEvent(eventType, playerActionListener, Priority.Normal, this);
        	} else if (eventName.startsWith("BLOCK")){
        		pm.registerEvent(eventType, blockActionListener, Priority.Normal, this);
        	} else if (eventName.startsWith("VEHICLE")){
         		pm.registerEvent(eventType, vehicleActionListener, Priority.Normal, this);
         	} else if (eventName.startsWith("ENTITY")){
         		pm.registerEvent(eventType, entityActionListener, Priority.Normal, this);
         	}
        }

        // EXAMPLE: Custom code, here we just output some info so we can check all is well
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
        
    }
    
    private class PlayerActionListener extends PlayerListener {
    	/**
         * Count player item drops.
         */
        @Override
    	public void onPlayerDropItem(PlayerDropItemEvent event) {
    		Player player = event.getPlayer();
    		Material material = event.getItemDrop().getItemStack().getType();
    	}
    	/**
         * Count player item pickups.
         */
        @Override
    	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    		Player player = event.getPlayer();
    		Material material = event.getItem().getItemStack().getType();
    	}
        /**
         * Count player arm swings.
         */
        @Override
        public void onPlayerAnimation(PlayerAnimationEvent event) {
        	Player player = event.getPlayer();
        }
        /**
         * Count player chat.
         */
        @Override
        public void onPlayerChat(PlayerChatEvent event) {
        	Player player = event.getPlayer();
        }
        /**
         * Count player commands issued.
         */
        @Override
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        	Player player = event.getPlayer();
        }
        /**
         * Count player logins.
         */
        @Override
        public void onPlayerLogin(PlayerLoginEvent event) {
        	Player player = event.getPlayer();
        }
        /**
         * Count player logouts.
         */
        @Override
        public void onPlayerQuit(PlayerQuitEvent event) {
        	Player player = event.getPlayer();
        }
        /**
         * Count player steps.
         */
        @Override
        public void onPlayerMove(PlayerMoveEvent event) {
        	Player player = event.getPlayer();
        }
        /**
         * Count player respawns.
         */
        @Override
        public void onPlayerRespawn(PlayerRespawnEvent event) {
        	Player player = event.getPlayer();
        }
        /**
         * Count player teleports.
         */
        @Override
        public void onPlayerTeleport(PlayerTeleportEvent event) {
        	Player player = event.getPlayer();
        }
    }
    
    private class BlockActionListener extends BlockListener {
    	/**
         * Count player block places.
         */
        @Override 
    	public void onBlockPlace(BlockPlaceEvent event){
    		 Player player = event.getPlayer();
    	 }
        /**
         * Count player block breaks.
         */
        @Override
    	 public void onBlockBreak(BlockBreakEvent event){
    		 Player player = event.getPlayer();
    	 }
    }
    
    private class VehicleActionListener extends VehicleListener {
    	/**
         * Count player vehicle distance.
         */
        @Override 
    	public void onVehicleMove(VehicleMoveEvent event){
    		 Entity passenger = event.getVehicle().getPassenger();
    		 if (passenger instanceof Player){
    			 Player player = (Player)passenger;
    			 
    		 }
    	 }
        /**
         * Count player vehicle uses.
         */
        @Override
	    public void onVehicleEnter(VehicleEnterEvent event) {
	    	Entity passenger = event.getVehicle().getPassenger();
			if (passenger instanceof Player){
				Player player = (Player)passenger;
				
			}
	    }
    }
    
    private class EntityActionListener extends EntityListener {
    	private IdentityHashMap<Player,DamageCause> lastDamagedBy = new IdentityHashMap<Player,DamageCause>();
    	
    	/**
         * Count player damage delt and recieved.
         */
        @Override
    	public void onEntityDamage(EntityDamageEvent event) {
    		Entity damagee = null;
    		// ****************************************************************
        	Player player = null;
        	Entity damager = null;
        	DamageCause cause = null;
        	Integer damage = null;
    		// ****************************************************************
        	
        	/**
             * Handle damage from blocks (cactus, lava)
             */
    		if (event instanceof EntityDamageByBlockEvent){
    			EntityDamageByBlockEvent evt = (EntityDamageByBlockEvent)event;
    			// ****************************************************************
            	damagee = evt.getEntity();
            	cause = evt.getCause();
            	damage = evt.getDamage();
        		// ****************************************************************
            	
            	if (damagee instanceof Player){
            		player = (Player)damagee;
            		lastDamagedBy.put(player, cause);
            		
            	}
    		}
    		/**
             * Count player damage from and to other living and undead sources. 
             */
    		else if (event instanceof EntityDamageByEntityEvent){
    			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
    			// ****************************************************************
            	damagee = evt.getEntity();
            	damager = evt.getDamager();
            	cause = evt.getCause();
            	damage = evt.getDamage();
        		// ****************************************************************
            	
            	if (damagee instanceof Player){
            		player = (Player)damagee;
            		lastDamagedBy.put(player, cause);
            		
            	} else if (damager instanceof Player && damagee instanceof Creature){
            		player = (Player)damager;
            		
            	}
    		}
    		/**
             * Count player damage from and by projectiles (arrows).
             */
    		else if (event instanceof EntityDamageByProjectileEvent){
    			EntityDamageByProjectileEvent evt = (EntityDamageByProjectileEvent)event;
    			// ****************************************************************
            	damagee = evt.getEntity();
            	damager = evt.getDamager();
            	cause = evt.getCause();
            	damage = evt.getDamage();
        		// ****************************************************************
            	
            	if (damagee instanceof Player){
            		player = (Player)damagee;
            		lastDamagedBy.put(player, cause);
            		
            	} else if (damager instanceof Player && damagee instanceof Creature){
            		player = (Player)damager;
            		
            	}
    		}
    	}
    	
        /**
         * Count player deaths, location of death and cause of death.
         */
        @Override
        public void onEntityDeath(EntityDeathEvent event) {
        	Entity entity = event.getEntity();
        	if (entity instanceof Player){
        		// ************************************************************
        		Player player = (Player)entity;
        		Location location = player.getLocation();
        		DamageCause cause = lastDamagedBy.get(player);
        		// ************************************************************
        		
        		if (cause == null){
        			cause = DamageCause.CUSTOM;
        		}
        	}
        }
    }
}