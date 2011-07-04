/**
 * 
 */
package org.selurgniman.bukkit.mcplayerstats.listeners;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleListener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.selurgniman.bukkit.mcplayerstats.McPlayerStats;
import org.selurgniman.bukkit.mcplayerstats.model.PlayerStatsModel;

/**
 * @author <a href="mailto:e83800@wnco.com">Chris Bandy</a> Created on: Jul 3,
 *         2011
 */
public class VehicleActionListener extends VehicleListener
{
	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft."
			+ BlockActionListener.class.getName());
	private final PlayerStatsModel model;

	public VehicleActionListener(PlayerStatsModel model)
	{
		this.model = model;
	}

	/**
	 * Count player vehicle distance.
	 */
	@Override
	public void onVehicleMove(VehicleMoveEvent event)
	{
		Entity passenger = event.getVehicle().getPassenger();
		if (passenger instanceof Player)
		{
			Player player = (Player) passenger;

			Location lastLocation;
			String key;

			String categoryName = null;

			if (event.getVehicle() instanceof Boat)
			{
				categoryName = "boat";
			}
			else if (event.getVehicle() instanceof Minecart)
			{
				categoryName = "minecart";
			}

			if (categoryName != null)
			{
				key = player.getName() + "v" + categoryName;

				lastLocation = McPlayerStats.getPlayerLastLocation(key);

				if (lastLocation == null)
				{
					McPlayerStats.setPlayerLastLocation(key, event.getTo());
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
									key,
									event.getTo());

							String vehicleName = event
									.getVehicle()
									.toString()
									.replace("Craft", "");
							String eventName = event.getEventName().toString();
							model.IncrementStatistic(
									player,
									vehicleName,
									eventName,
									distance);
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
	public void onVehicleEnter(VehicleEnterEvent event)
	{
		Player player;
		Vehicle vehicle;

		Entity passenger = event.getVehicle().getPassenger();

		if (passenger instanceof Player)
		{
			vehicle = event.getVehicle();

			if (vehicle != null)
			{
				player = (Player) passenger;

				if (player.isInsideVehicle())
				{
					String vehicleName = event
							.getVehicle()
							.toString()
							.replace("Craft", "");
					model.IncrementStatistic(
							player,
							vehicleName,
							event.getEventName(),
							1);
				}
			}
		}
	}
}
