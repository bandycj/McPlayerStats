package org.selurgniman.bukkit.mcplayerstats.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MCStatsDB {

	private String url;
	private String userName;
	private String password;
	
	private Connection connection;
	
	public MCStatsDB(String url, String userName, String password) {
		this.url = url;
		this.userName = userName;
		this.password = password;
	}
	
	public void Open() throws Exception
	{
		if (connection == null)
		{
			try {				
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				connection = DriverManager.getConnection(url, userName, password);

				if (connection == null)
				{
					throw new Exception("Unable to create connection to: " + url);
				}
				
				System.out.println("Database connected: " + url);
			} catch (Exception e) {
				throw e;
			}
		}
	}
	
	public void Close() throws Exception
	{
		if (connection != null)
		{
			try {
				connection.close();		
				System.out.println("Closed connection: " + url);
			} catch (Exception e) {
				throw e;
			} finally {
				connection = null;
			}
		}
	}
	
	public void AddPlayer(String name) throws SQLException
	{
		PreparedStatement statement;
		statement = connection.prepareStatement("{call add_player(?)}");
		
		statement.setString(1, name);
		statement.execute();
		
		statement.close();		
	}
	
	public void AddCategory(String name) throws SQLException
	{
		PreparedStatement statement;
		statement = connection.prepareStatement("{call add_category(?)}");
		
		statement.setString(1, name);
		statement.execute();
		
		statement.close();		
	}
	
	public void AddStatistic(String name) throws SQLException
	{
		PreparedStatement statement;
		statement = connection.prepareStatement("{call add_statistic(?)}");
		
		statement.setString(1, name);
		statement.execute();
		
		statement.close();		
	}
	
	
	public ConcurrentMap<String, Integer> getPlayers() throws Exception
	{
		return ExecuteConcurrentMap("get_players", "Name", "ID");
	}

	public ConcurrentMap<String, Integer> getCategories() throws Exception
	{
		return ExecuteConcurrentMap("get_categories", "Name", "ID");
	}
	
	public ConcurrentMap<String, Integer> getStatistics() throws Exception
	{
		return ExecuteConcurrentMap("get_statistics", "Name", "ID");
	}
	
	public void UpdatePlayerLastLoggedOut(int playerId) throws SQLException
	{
		PreparedStatement statement;
		
		statement = connection.prepareStatement("{call update_playerLastLoggedOut(?)}");
		
		statement.setInt(1, playerId);
		
		statement.execute();
		statement.close();
	}
	
	public void UpdatePlayerLastLoggedIn(int playerId) throws SQLException
	{
		PreparedStatement statement;
		
		statement = connection.prepareStatement("{call update_playerLastLoggedIn(?)}");
		
		statement.setInt(1, playerId);
		
		statement.execute();
		statement.close();
	}	
	
	public void IncrementPlayerStatistic(int playerId, int categoryId, int statisticId, int valueIncrementBy) throws Exception
	{
		PreparedStatement statement;
		
		statement = connection.prepareStatement("{call increment_PlayerStatistic(?,?,?,?)}");
		
		statement.setInt(1, playerId);
		statement.setInt(2, categoryId);
		statement.setInt(3, statisticId);
		statement.setInt(4, valueIncrementBy);
		
		statement.execute();
		statement.close();
	}
		
	private ResultSet ExecuteResultSet(String procedureName) throws Exception
	{
		ResultSet result;
		
		try {
			Statement statement;
			
			statement = connection.createStatement();
			result = statement.executeQuery("{call " + procedureName + "}");			
		} catch (Exception e) {
			throw e;
		}
		
		return result;
	}
		
	private ConcurrentMap<String, Integer> ExecuteConcurrentMap(String procedureName, String keyName, String valueName) throws Exception
	{
		ConcurrentMap<String, Integer> returnValue = null;
		ResultSet rs;
		
		try {
			rs = ExecuteResultSet(procedureName);
			returnValue = new ConcurrentHashMap<String, Integer>();
			
			while (rs.next())
			{
				String key;
				int value;
				
				key = rs.getString(keyName);
				value = rs.getInt(valueName);
				
				returnValue.put(key, value);
			}
			
			rs.close();
			
		} catch (Exception e) {
			throw e;
		}
				
		return returnValue;
	}
	
}
