/**
 * 
 */
package org.selurgniman.bukkit.mcplayerstats.listeners;

import java.util.Calendar;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.selurgniman.bukkit.mcplayerstats.McPlayerStats;
import org.selurgniman.bukkit.mcplayerstats.model.PlayerStatsModel;

/**
 * @author <a href="mailto:e83800@wnco.com">Chris Bandy</a> Created on: Jul 3,
 *         2011
 */
public class PlayerActionListener extends PlayerListener
{
	private final Logger log = Logger.getLogger("Minecraft."
			+ PlayerActionListener.class.getName());
	private final PlayerStatsModel model;

	public PlayerActionListener(PlayerStatsModel model)
	{
		this.model = model;
	}

	/**
	 * Count player item drops.
	 */
	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event)
	{
		model.IncrementStatistic(event, event.getItemDrop().getItemStack());
	}

	/**
	 * Count player item pickups.
	 */
	@Override
	public void onPlayerPickupItem(PlayerPickupItemEvent event)
	{
		model.IncrementStatistic(event, event.getItem().getItemStack());
	}

	/**
	 * Count player arm swings.
	 */
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event)
	{
		Player player = event.getPlayer();

		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING)
		{
			model.IncrementStatistic(player, event
					.getAnimationType()
					.toString(), event.getEventName().toString(), 1);
		}
	}

	/**
	 * Count player chat.
	 */
	@Override
	public void onPlayerChat(PlayerChatEvent event)
	{
		model.IncrementStatistic(event);
		model.IncrementStatistic(event.getPlayer(), "chat_letters", event
				.getEventName()
				.toString(), event.getMessage().length());
	}

	/**
	 * Count player commands issued.
	 */
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
	{
		model.IncrementStatistic(event);
	}

	/**
	 * Count player logins.
	 */
	@Override
	public void onPlayerLogin(PlayerLoginEvent event)
	{
		Player player;

		player = event.getPlayer();

		McPlayerStats.setPlayerLastLogin(player, Calendar.getInstance());

		try
		{
			model.updatePlayerLastLoggedIn(model.getPlayerId(player.getName()));
		}
		catch (Exception e)
		{
			log.info("Error updating Player Last Logged In value.");
			log.info(e.toString());
		}

		model.IncrementStatistic(event);
	}

	/**
	 * Count player logouts.
	 */
	@Override
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		double playerSecondsLoggedIn;
		Calendar playerLogin;
		Player player;

		player = event.getPlayer();

		playerLogin = McPlayerStats.getPlayerLastLogin(player);

		if (playerLogin != null)
		{
			Calendar now;

			now = Calendar.getInstance();

			playerSecondsLoggedIn = ((now.getTimeInMillis() - playerLogin
					.getTimeInMillis()) / 1000);

			model.IncrementStatistic(player, "playedfor", event
					.getEventName()
					.toString(), (int) playerSecondsLoggedIn);
		}

		try
		{
			model.updatePlayerLastLoggedOut(model.getPlayerId(player.getName()));
		}
		catch (Exception e)
		{
			log.info("Error updating Player Last Logged Out value.");
			log.info(e.toString());
		}

		model.IncrementStatistic(event);
	}

	/**
	 * Count player steps.
	 */
	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Location lastLocation;

		String playerName = event.getPlayer().getName();

		lastLocation = McPlayerStats.getPlayerLastLocation(playerName);

		if (lastLocation == null)
		{
			McPlayerStats.setPlayerLastLocation(playerName, event.getTo());
		}
		else
		{
			// Only count if they've moved at least 1 block in some
			// direction except up/down?

			int x1, x2;
			int y1, y2;

			x1 = lastLocation.getBlockX();
			x2 = event.getTo().getBlockX();

			y1 = lastLocation.getBlockZ();
			y2 = event.getTo().getBlockZ();

			if (x1 != x2 || y1 != y2)
			{
				int distance;

				distance = (int) Math.sqrt(Math.pow(x2 - x1, 2)
						+ Math.pow(y2 - y1, 2));

				if (distance >= 1)
				{
					McPlayerStats.setPlayerLastLocation(
							playerName,
							event.getTo());
					model.IncrementStatistic(
							event.getPlayer(),
							PlayerStatsModel.PLAYER_EVENT,
							event.getEventName(),
							distance);
				}
			}

		}
	}

	/**
	 * Count player respawns.
	 */
	@Override
	public void onPlayerRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();

		model.IncrementStatistic(
				player,
				PlayerStatsModel.PLAYER_EVENT,
				event.getEventName(),
				1);
	}

	/**
	 * Count player teleports.
	 */
	@Override
	public void onPlayerTeleport(PlayerTeleportEvent event)
	{
		// Player player = event.getPlayer();
		//
		// Teleports happens when people use vehicles, enter the world and
		// get pushed back by something
		// model.IncrementStatistic(player, PLAYER_EVENT,
		// event.getEventName(),
		// 1);
	}
}