/**
 * 
 */
package org.selurgniman.bukkit.mcplayerstats.model;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.selurgniman.bukkit.mcplayerstats.McPlayerStats;

/**
 * @author <a href="mailto:e83800@wnco.com">Chris Bandy</a> Created on: Jul 3,
 *         2011
 */
public class PlayerStatsModel
{
	private final Logger log = Logger.getLogger("Minecraft."
			+ PlayerStatsModel.class.getName());
	private MCStatsDB database = null;
	public static final String PLAYER_EVENT = "PLAYER_EVENT";

	private ConcurrentMap<String, Integer> categories;
	private ConcurrentMap<String, Integer> statistics;
	private ConcurrentMap<String, Integer> players;

	public void initialize()
	{
		if (database == null)
		{
			String conn = McPlayerStats.getConfig().getString("connectString");
			String user = McPlayerStats.getConfig().getString("dbUser");
			String passwd = McPlayerStats.getConfig().getString("dbPass");
			this.database = new MCStatsDB(conn, user, passwd);
		}

		try
		{
			try
			{
				database.Open();

				categories = database.getCategories();
				statistics = database.getStatistics();
				players = database.getPlayers();
			}
			catch (Exception e)
			{
				log.info(e.toString());
			}
		}
		catch (Exception e)
		{
			log.info(e.getLocalizedMessage());
		}
	}

	public void close()
	{
		try
		{
			database.Close();
		}
		catch (Exception e)
		{
			log.info(e.getLocalizedMessage());
		}
	}

	public int getPlayerId(String name) throws Exception
	{
		Integer value;

		if (!players.containsKey(name.toLowerCase()))
		{
			log.info("Added Player Key: " + name.toLowerCase());
			database.AddPlayer(name);
			players = database.getPlayers();
		}

		value = players.get(name.toLowerCase());
		return value.intValue();
	}

	public int getStatisticId(String name) throws Exception
	{
		Integer value;

		if (!statistics.containsKey(name.toLowerCase()))
		{
			log.info("Added Statistic Key: " + name.toLowerCase());
			database.AddStatistic(name);
			statistics = database.getStatistics();

		}

		value = statistics.get(name.toLowerCase());
		return value.intValue();
	}

	public int getCategoryId(String name) throws Exception
	{
		Integer value;

		if (!categories.containsKey(name.toLowerCase()))
		{
			log.info("Added Category Key: " + name.toLowerCase());
			database.AddCategory(name);
			categories = database.getCategories();
		}

		value = categories.get(name.toLowerCase());
		return value.intValue();
	}

	public void IncrementStatistic(PlayerEvent event)
	{
		// These are all events that are single events with no blocks or items.

		IncrementStatistic(event.getPlayer(), PLAYER_EVENT, event
				.getEventName()
				.toString(), 1);
	}

	public void IncrementStatistic(BlockEvent event, Player player)
	{
		McPlayerStats.debugMsg(String.format("You %1s %2s %3s", event
				.getEventName()
				.toString(), 1, event.getBlock().getType().toString()));

		IncrementStatistic(player, event.getBlock().getType().toString(), event
				.getEventName()
				.toString(), 1);
	}

	public void IncrementStatistic(PlayerEvent event, ItemStack item)
	{
		McPlayerStats.debugMsg(String.format("You %1s %2s %3s", event
				.getEventName()
				.toString(), item.getAmount(), item.getType().toString()));

		IncrementStatistic(event.getPlayer(), item.getType().toString(), event
				.getEventName()
				.toString(), item.getAmount());
	}

	public void IncrementStatistic(
			Player player,
			String statisticName,
			String categoryName,
			int amount)
	{
		int categoryId;
		int playerId;
		int statisticId;

		// statisticName = statisticName.replace("_", "");

		try
		{
			categoryId = getCategoryId(categoryName);
			playerId = getPlayerId(player.getName());
			statisticId = getStatisticId(statisticName);

			database.IncrementPlayerStatistic(
					playerId,
					categoryId,
					statisticId,
					amount);
		}
		catch (Exception e)
		{
			log.info("Couldn't increment statistic: "
					+ statisticName
					+ ", category: "
					+ categoryName
					+ ", player: "
					+ player.getDisplayName());

			log.info(e.toString());
		}
	}

	public void updatePlayerLastLoggedIn(int playerId)
	{
		try
		{
			database.UpdatePlayerLastLoggedIn(playerId);
		}
		catch (SQLException e)
		{
			log.info(e.getLocalizedMessage());
		}
	}

	public void updatePlayerLastLoggedOut(int playerId)
	{
		try
		{
			database.UpdatePlayerLastLoggedOut(playerId);
		}
		catch (SQLException e)
		{
			log.info(e.getLocalizedMessage());
		}
	}

	@Override
	public void finalize()
	{
		try
		{
			database.Close();
		}
		catch (Exception e)
		{
			log.info(e.getLocalizedMessage());
		}
	}
}
