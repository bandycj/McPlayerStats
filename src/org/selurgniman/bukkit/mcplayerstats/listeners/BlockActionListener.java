/**
 * 
 */
package org.selurgniman.bukkit.mcplayerstats.listeners;

import java.util.logging.Logger;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.selurgniman.bukkit.mcplayerstats.model.PlayerStatsModel;

/**
 * @author <a href="mailto:e83800@wnco.com">Chris Bandy</a>
 * Created on: Jul 3, 2011
 */
public class BlockActionListener extends BlockListener
{
	@SuppressWarnings("unused")
	private final Logger log = Logger.getLogger("Minecraft."
			+ BlockActionListener.class.getName());
	private final PlayerStatsModel model;

	public BlockActionListener(PlayerStatsModel model)
	{
		this.model = model;
	}
	
	/**
	 * Count player block places.
	 */
	@Override
	public void onBlockPlace(BlockPlaceEvent event)
	{
		model.IncrementStatistic(event, event.getPlayer());
	}

	/**
	 * Count player block breaks.
	 */
	@Override
	public void onBlockBreak(BlockBreakEvent event)
	{
		model.IncrementStatistic(event, event.getPlayer());
	}
}