/**
 * 
 */
package org.selurgniman.bukkit.mcplayerstats.listeners;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.selurgniman.bukkit.mcplayerstats.model.PlayerStatsModel;

/**
 * @author <a href="mailto:e83800@wnco.com">Chris Bandy</a> Created on: Jul 3,
 *         2011
 */
public class EntityActionListener extends EntityListener
{
	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft."
			+ BlockActionListener.class.getName());

	private ConcurrentMap<Player, String> lastDamagedBy = new ConcurrentHashMap<Player, String>();
	private final PlayerStatsModel model;

	public EntityActionListener(PlayerStatsModel model)
	{
		this.model = model;
	}

	/**
	 * Count player damage delt and recieved.
	 */
	@Override
	public void onEntityDamage(EntityDamageEvent event)
	{
		Entity damagee = null;
		// ****************************************************************

		Entity damager = null;
		Integer damage = null;
		String cause = null;
		// ****************************************************************

		/**
		 * FIRE_TICK causes wierd bugs so check for it first
		 */
		// if (event.getCause() == DamageCause.FIRE_TICK) {
		// ****************************************************************

		damagee = event.getEntity();
		damage = event.getDamage();

		if (event instanceof EntityDamageByEntityEvent)
		{
			damager = ((EntityDamageByEntityEvent) event).getDamager();
		}

		if (damagee instanceof Player || damager instanceof Player)
		{
			if (event instanceof EntityDamageByBlockEvent)
			{
				if (((EntityDamageByBlockEvent) event).getDamager() != null)
				{
					cause = ((EntityDamageByBlockEvent) event)
							.getDamager()
							.getType()
							.toString();
				}
				else
				{
					cause = damagee
							.getLocation()
							.getBlock()
							.getType()
							.toString();
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

			if (damagee instanceof Player)
			{
				lastDamagedBy.put((Player) damagee, cause);
				model.IncrementStatistic(
						(Player) damagee,
						cause,
						event.getEventName() + "_TAKEN",
						damage.intValue());
			}
			else
			{
				model.IncrementStatistic(
						(Player) damager,
						damagee.toString().replace("Craft", ""),
						event.getEventName() + "_DEALT",
						damage.intValue());
			}
		}
	}

	/**
	 * Count player deaths, location of death and cause of death.
	 */
	@Override
	public void onEntityDeath(EntityDeathEvent event)
	{
		Entity entity = event.getEntity();
		if (entity instanceof Player)
		{
			// ************************************************************
			Player player = (Player) entity;
			@SuppressWarnings("unused")
			Location location = player.getLocation();
			String cause = lastDamagedBy.get(player);
			// ************************************************************

			if (cause == null)
			{
				cause = "UNKNOWN";
			}

			// Better event to distinguish these automatically?
			model.IncrementStatistic(player, cause, "ENTITY_KILLED_PLAYER", 1);
		}
		else
		{
			EntityDamageEvent causedEvent = entity.getLastDamageCause();

			if (causedEvent instanceof EntityDamageByEntityEvent)
			{
				Entity damager = ((EntityDamageByEntityEvent) causedEvent)
						.getDamager();

				if (damager != null && damager instanceof Player)
				{
					String entityName = entity.toString().replace("Craft", "");
					
					model.IncrementStatistic(
							(Player) damager,
							entityName,
							"PLAYER_KILLED_ENTITY",
							1);
				}
			}
		}
	}
}