package org.toi.guilds;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.toi.util.tPermissions;
import org.toi.util.tProperties;


public final class GHolder {
	
	private ArrayList<Guild> guilds = new ArrayList<Guild>();
	private ArrayList<String> pendingGuildInvites = new ArrayList<String>();
	private tProperties properties = new tProperties("Guilds" + File.separator + "guilds.properties");
	static int guildAreaExpansion = 3;
	static int guildAreaStartSize = 3;
	static boolean useChatTag = true;
	static String chatFormat = "[%n]";
	static boolean guildCreateCosts = false;
	private ArrayList<Command> guildCommands = new ArrayList<Command>();
	static ArrayList<GuildKind> guildKinds = new ArrayList<GuildKind>();
	private ArrayList<Party> partys = new ArrayList<Party>();
	static Plugin plugin;
	private tPermissions tps = new tPermissions("Guilds" + File.separator + "guilds.perms");
	public static final Logger log = Logger.getLogger("Minecraft");
	
	public GHolder(){}
	
	static String gString ()
	{
		return ChatColor.AQUA + "[Guilds] " + ChatColor.YELLOW;
	}
	
	public boolean playerHasItem (Player player, Material mat, int amount)
	{
		PlayerInventory inventory = player.getInventory();
        ItemStack temp[] = inventory.getContents();
        int id = mat.getId();
        int total = 0;
        ItemStack items[] = temp;
        for(ItemStack item : items)
        {
            if(item != null && item.getTypeId() == id && item.getAmount() > 0)
                total += item.getAmount();
        }

        return total >= amount;
	}
	
	public int playerGetAmountOfItems(Player player, Material mat)
    {
		PlayerInventory inventory = player.getInventory();
        ItemStack temp[] = inventory.getContents();
        int id = mat.getId();
        int amount = 0;
        ItemStack items[] = temp;
        for(ItemStack item : items)
        {
            if(item != null)
            {
            	if (item.getTypeId() == id)
            		amount += item.getAmount();
            }
        }

        return amount;
    }
	
	public void playerRemoveItems(Player player, Material mat, int amount)
	{
		PlayerInventory inv = player.getInventory();
		inv.removeItem(new ItemStack(mat, amount));
	}
	
	public String setGuildHome(Player player) {
		for (Guild guild : this.guilds)
		{
			if (guild.hasMember(player.getName()))
			{
				if (guild.isPlayerAdmin(player.getName()))
				{
					if (guild.isNearArea(player.getLocation().getX(), player.getLocation().getZ(), 2.0))
					{
						guild.setGuildSpawn(player.getLocation());
						return "The guild home is now set!";
					}
					else
						return "The guild home has to be in the guild area!";
				}
				else
					return "You do not have permission to do that!";
			}
		}
		return "You are not in a guild yet!";
	}

	public void printAvaliableCommands (Player player, int page)
	{
		boolean hasCommands = false;
		ArrayList<String> msgList = new ArrayList<String>();
		int counter = 0;
		int nrOfCommandsAvaliable = 0;
		for (Command cmd : this.guildCommands)
		{
			if (tps.canPlayerUseCommand(player.getName(), cmd.getCommand()))
			{
				if (counter < (7 * page))
				{
					if (counter >= (7 * (page - 1)))
					{
						if (cmd.getSyntaxes().equals(""))
							msgList.add(ChatColor.DARK_RED + cmd.getCommand() + ChatColor.WHITE + " - " + cmd.getDescription());
						else
							msgList.add(ChatColor.DARK_RED +  cmd.getCommand() + " " + ChatColor.AQUA + cmd.getSyntaxes() + ChatColor.WHITE + " - " + cmd.getDescription());
					}
					counter++;
				}
				hasCommands = true;
				nrOfCommandsAvaliable++;
			}
		}
		if (!hasCommands)
		{
			player.sendMessage(gString() + "You do not have any avaliable commands");
		}
		else if (page > (nrOfCommandsAvaliable/7) + 1 || page < 1)
		{
			player.sendMessage(gString() + "Invalid pagenumber, " + String.valueOf((nrOfCommandsAvaliable/7)));
		}
		else
		{
			player.sendMessage(gString() + "Command list page " + String.valueOf(page) + "/" + String.valueOf((nrOfCommandsAvaliable/7) + 1) + ":");
			for (String msg : msgList)
			{
				player.sendMessage(msg);
			}
		}
	}
	
	public String modifyGuildValue(String function, String value, String guildName, String playerName)
	{
		String line = "Failed to modify permission!";
		int guildIndex = -1;
		for (int i = 0; i < this.guilds.size(); i++)
		{
			if (this.guilds.get(i).getName().equalsIgnoreCase(guildName))
			{
				guildIndex = i;
				break;
			}
		}
		if (guildIndex > -1)
		{
			if (this.guilds.get(guildIndex).isPlayerAdmin(playerName))
			{
				if (GHolder.isIntNumber(value))
				{
					int rank = Integer.valueOf(value);
					if (rank >= 0)
					{
						if (function.equalsIgnoreCase("invite") || function.equalsIgnoreCase("inv"))
						{
							this.guilds.get(guildIndex).setPerm("invite", rank);
							line = "Invite permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("kick"))
						{
							this.guilds.get(guildIndex).setPerm("kick", rank);
							line = "Kick permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("promote") || function.equalsIgnoreCase("prom"))
						{
							this.guilds.get(guildIndex).setPerm("promote", rank);
							line = "Promote permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("demote") || function.equalsIgnoreCase("dem"))
						{
							this.guilds.get(guildIndex).setPerm("demote", rank);
							line = "Demote permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("build") || function.equalsIgnoreCase("bld"))
						{
							this.guilds.get(guildIndex).setPerm("build", rank);
							line = "Build permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("destroy") || function.equalsIgnoreCase("dstry"))
						{
							this.guilds.get(guildIndex).setPerm("destroy", rank);
							line = "Destroy permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("sethome") || function.equalsIgnoreCase("sh"))
						{
							this.guilds.get(guildIndex).setPerm("set home", rank);
							line = "Set home permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("usechest") || function.equalsIgnoreCase("uc"))
						{
							this.guilds.get(guildIndex).setPerm("use chest", rank);
							line = "Use chest permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("useworkbench") || function.equalsIgnoreCase("uw"))
						{
							this.guilds.get(guildIndex).setPerm("use workbench", rank);
							line = "Use workbench permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("usefurnace") || function.equalsIgnoreCase("uf"))
						{
							this.guilds.get(guildIndex).setPerm("use furnace", rank);
							line = "Use furnace permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("usedispenser") || function.equalsIgnoreCase("ud"))
						{
							this.guilds.get(guildIndex).setPerm("use dispenser", rank);
							line = "Use dispenser permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("eatcake") || function.equalsIgnoreCase("ec"))
						{
							this.guilds.get(guildIndex).setPerm("eat cake", rank);
							line = "Eat cake permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("opendoors") || function.equalsIgnoreCase("od"))
						{
							this.guilds.get(guildIndex).setPerm("open doors", rank);
							line = "Open doors permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("pulllevers") || function.equalsIgnoreCase("pl"))
						{
							this.guilds.get(guildIndex).setPerm("pull levers", rank);
							line = "Pull levers permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("pushbuttons") || function.equalsIgnoreCase("pb"))
						{
							this.guilds.get(guildIndex).setPerm("push buttons", rank);
							line = "Push buttons permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("triggertnt") || function.equalsIgnoreCase("tt"))
						{
							this.guilds.get(guildIndex).setPerm("trigger tnt", rank);
							line = "Trigger TNT permission changed to rank " + rank;
						}
						else if (function.equalsIgnoreCase("color") || function.equalsIgnoreCase("clr"))
						{
							if (this.getColor(value) != null)
							{
								this.guilds.get(guildIndex).setColor(this.getColor(value));
								line = this.guilds.get(guildIndex).getColor() + this.guilds.get(guildIndex).getName() + ChatColor.YELLOW + "'s guild color changed to " + value;
							}
							else
								line = "Invalid color number! use 0-9";
						}
						else
							line = "Unknown function: " + function;
					}
					else
						line = "Rank must be a positive value!";
				}
				else if (GHolder.isBoolean(value))
				{
					boolean bool = Boolean.parseBoolean(value);
					if (function.equalsIgnoreCase("joinable"))
					{
						this.guilds.get(guildIndex).setJoinable(bool);
						if (bool)
							line = this.guilds.get(guildIndex).getColor() + this.guilds.get(guildIndex).getName() + ChatColor.YELLOW + " is now joinable";
						else
							line = this.guilds.get(guildIndex).getColor() + this.guilds.get(guildIndex).getName() + ChatColor.YELLOW + " is now unjoinable";
					}
					else if (function.equalsIgnoreCase("access"))
					{
						this.guilds.get(guildIndex).getGuildArea().setAllowAccess(bool);
						if (bool)
							line = this.guilds.get(guildIndex).getColor() + this.guilds.get(guildIndex).getName() + ChatColor.YELLOW + "'s Guild area is now accessable";
						else
							line = this.guilds.get(guildIndex).getColor() + this.guilds.get(guildIndex).getName() + ChatColor.YELLOW + "'s Guild area is now unaccessable";
					}
					else
						line = "Unknown function: " + function;
				}
				else if (this.getColor(value) != null)
				{
					if (function.equalsIgnoreCase("color") || function.equalsIgnoreCase("clr"))
					{
						this.guilds.get(guildIndex).setColor(this.getColor(value));
						line = this.guilds.get(guildIndex).getColor() + this.guilds.get(guildIndex).getName() + ChatColor.YELLOW + "'s guild color changed to " + value;
					}
					else
						line = "Unknown function: " + function;
				}
				else
					line = "Could not interpret value, type /gfunclist for a list";
				this.guilds.get(guildIndex).saveToFile();
			}
			else
				line = "You don't have permission to do that!";
		}
		else
			line = "Could not find guild";
		return line;
	}
	
	static boolean isIntNumber(String num){
	    try{
	        Integer.parseInt(num);
	    } catch(NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	
	static boolean isBoolean (String bool){
	    if (bool.equalsIgnoreCase("true") || bool.equalsIgnoreCase("false"))
	    	return true;
	    else
	    	return false;
	}
	
	public boolean telePlayerToGuildArea (Player player)
	{
		for (Guild guild : this.guilds)
		{
			if (guild.hasMember(player.getName()))
			{
				player.teleportTo(guild.getGuildSpawn());
				return true;
			}
		}
		return false;
	}
	
	public ChatColor getColor (String clr)
	{
		if (clr.equalsIgnoreCase("black") || clr.equalsIgnoreCase("0"))
			return ChatColor.BLACK;
		else if (clr.equalsIgnoreCase("darkblue") || clr.equalsIgnoreCase("1"))
			return ChatColor.DARK_BLUE;
		else if (clr.equalsIgnoreCase("darkgreen") || clr.equalsIgnoreCase("2"))
			return ChatColor.DARK_GREEN;
		else if (clr.equalsIgnoreCase("darkaqua") || clr.equalsIgnoreCase("3"))
			return ChatColor.DARK_AQUA;
		else if (clr.equalsIgnoreCase("darkred") || clr.equalsIgnoreCase("4"))
			return ChatColor.DARK_RED;
		else if (clr.equalsIgnoreCase("darkpurple") || clr.equalsIgnoreCase("5"))
			return ChatColor.DARK_PURPLE;
		else if (clr.equalsIgnoreCase("gold") || clr.equalsIgnoreCase("6"))
			return ChatColor.GOLD;
		else if (clr.equalsIgnoreCase("gray") || clr.equalsIgnoreCase("7"))
			return ChatColor.GRAY;
		else if (clr.equalsIgnoreCase("darkgray") || clr.equalsIgnoreCase("8"))
			return ChatColor.DARK_GRAY;
		else if (clr.equalsIgnoreCase("blue") || clr.equalsIgnoreCase("9"))
			return ChatColor.BLUE;
		else if (clr.equalsIgnoreCase("green") || clr.equalsIgnoreCase("A"))
			return ChatColor.GREEN;
		else if (clr.equalsIgnoreCase("aqua") || clr.equalsIgnoreCase("B"))
			return ChatColor.AQUA;
		else if (clr.equalsIgnoreCase("red") || clr.equalsIgnoreCase("C"))
			return ChatColor.RED;
		else if (clr.equalsIgnoreCase("lightpurple") || clr.equalsIgnoreCase("D"))
			return ChatColor.LIGHT_PURPLE;
		else if (clr.equalsIgnoreCase("yellow") || clr.equalsIgnoreCase("E"))
			return ChatColor.YELLOW;
		else if (clr.equalsIgnoreCase("white") || clr.equalsIgnoreCase("F"))
			return ChatColor.WHITE;
		else
			return null;
	}
	
	public String preJoinGuild(String guildName, String playerName)
	{
		String line = "Guild not found!";
		for (Guild guild : this.guilds)
		{
			if (guild.getName().equalsIgnoreCase(guildName))
			{
				if (guild.hasMember(playerName))
				{
					line = "You are already in " + guild.getColor() + guild.getName();
					break;
				}
				else
				{
					if (guild.isJoinable())
					{
						this.pendingGuildInvites.add(playerName.toLowerCase() + ";" + guildName.toLowerCase());
						line = "Join " + guild.getColor() + guild.getName() + ChatColor.YELLOW + "? Type /gg accept or /gg decline";
						break;
					}
					else
					{
						line = "Guild " + guild.getColor() + guild.getName() + ChatColor.YELLOW + " is not joinable!";
						break;
					}
				}
			}
		}
		return gString() + line;
	}
	
	public String joinGuild(String guildName, String playerName)
	{
		boolean found = false;
		String line = "Guild not found!";
		
		for (Guild guild : this.guilds)
		{
			if (guild.getName().equalsIgnoreCase(guildName))
			{
				if (guild.isJoinable())
				{
					for (GPlayer plr : guild.getPlayers())
					{
						if (plr.getName().equalsIgnoreCase(playerName))
							found = true;
					}
					if (!found)
					{
						guild.addPlayer(playerName);
						guild.getArea().areaSize(guild.getPlayers().size());
						return "You joined " + guild.getColor() + guildName + ChatColor.YELLOW + "!";
					}
					else 
					{
						line = "You are already in " + guild.getColor() + guild.getName();
						break;
					}
						
				}
				else 
					line = guild.getColor() + guildName + ChatColor.YELLOW + " is not joinable.";
			}
		}
		return line;
	}
	
	public void sendMsgToPlayer(String playerName, String message)
	{
		Player player = GHolder.plugin.getServer().getPlayer(playerName);
		if (player != null)
		{
			if (player.isOnline())
				player.sendMessage(message);
		}
	}
	
	public String inviteToGuild(String guildName, String playerName, String adderName)
	{
		String facColor = "";
		boolean found = false;
		boolean hasRank = false;
		String line = "Guild not found!";
		for (Guild fac : this.guilds)
		{
			if (fac.getName().equalsIgnoreCase(guildName))
			{
				for (GPlayer plr : fac.getPlayers())
				{
					if (plr.getName().equalsIgnoreCase(playerName))
						found = true;
					if (plr.getName().equalsIgnoreCase(adderName))
					{
						if (plr.getRank() >= fac.getPerm("invite") || fac.isPlayerAdmin(plr.getName()))
							hasRank = true;
					}
				}
				if (!found && hasRank)
				{
					this.pendingGuildInvites.add(playerName.toLowerCase() + ";" + guildName.toLowerCase());
					this.sendMsgToPlayer(playerName, gString() + this.playerOnline(adderName) + " invited you to " + fac.getColor() + fac.getName() +
											ChatColor.YELLOW + "! Type /gg accept or /gg decline");
					line = 	gString() + "You invited " + playerOnline(playerName) +
							ChatColor.YELLOW + " to " + facColor + guildName + ChatColor.YELLOW + "!";
				}
				else if (!found && !hasRank)
					line = "You do not have permission to do that!";
				else if (hasRank && found)
					line = 	playerOnline(playerName) + " is already in " + facColor + guildName + ChatColor.YELLOW + "!";
				break;
			}
		}
		return line;
	}
	
	public String playerOnline(String playerName)
	{
		Player plr = plugin.getServer().getPlayer(playerName);
		if (plr != null)
		{
			if (!plr.getName().equals(""))
				return getPlayerWithGuildColor(playerName) + ChatColor.YELLOW;
		}
		return playerName;
	}
	
	public String getPlayerWithGuildColor(String playerName)
	{
		for (Guild guild : this.guilds)
		{
			if (guild.hasMember(playerName))
				return guild.getColor() + playerName + ChatColor.WHITE;
		}
		return playerName;
	}
	
	public String kickPlayerFromGuild(String guildName, String name, String kickerName)
	{
		int index = -1;
		String line = "Guild not found!";
		int kickerRank = - 2;
		int kickeeRank = - 2;
		for (Guild fac : this.guilds)
		{
			if (fac.getName().equalsIgnoreCase(guildName))
			{	
				for (int i = 0; i < fac.getPlayers().size(); i++)
				{
					if (fac.getPlayers().get(i).getName().equalsIgnoreCase(name))
					{
						index = i;
						kickeeRank = fac.getPlayers().get(i).getRank();
					}
					if (fac.getPlayers().get(i).getName().equalsIgnoreCase(kickerName))
					{
						kickerRank = fac.getPlayers().get(i).getRank();
					}
				}
				if ((index != -1 && kickerRank != -2 && kickeeRank != -2 && kickerRank > kickeeRank && kickerRank >= fac.getPerm("kick")) || fac.isPlayerAdmin(kickerName))
				{
					fac.getPlayers().remove(index);
					fac.getArea().areaSize(fac.getPlayers().size());
					line = playerOnline(name) + " was removed from " + fac.getColor() + fac.getName() + ChatColor.YELLOW + "!";
				}
				else if (index != -1 && kickerRank != -2 && kickeeRank != -2 && kickerRank <= kickeeRank)
				{
					line = "You can't kick someone who is higher or equal rank!";
				}
				else if (index == -1)
				{
					line = "Could not find " + playerOnline(name);
				}
				else
					line = "Insufficient permission!";
				break;
			}
		}
		return gString() + line;
	}
	
	public String removePlayerFromGuild(String guildName, String name)
	{
		int index = -1;
		String line = "Guild not found!";
		for (Guild fac : this.guilds)
		{
			if (fac.getName().equalsIgnoreCase(guildName))
			{	
				for (int i = 0; i < fac.getPlayers().size(); i++)
				{
					if (fac.getPlayers().get(i).getName().equalsIgnoreCase(name))
					{
						index = i;
					}
				}
				if (index != -1)
				{
					fac.getPlayers().remove(index);
					fac.getArea().areaSize(fac.getPlayers().size());
					line = 	gString() + this.playerOnline(name) + 
							" was removed from " + fac.getColor() + fac.getName() + ChatColor.YELLOW + "!";
					fac.saveToFile();
				}
				else
					line = gString() + "Could not find " + this.playerOnline(name) + " in " + fac.getColor() + guildName + ChatColor.YELLOW + "!";
				break;
			}
		}
		return line;
	}
	
	public String removeGuild(String guildName)
	{
		int index = -1;
		String line = "Guild not found!";
		for (int i = 0; i < this.guilds.size(); i++)
		{
			if (this.guilds.get(i).getName().equalsIgnoreCase(guildName))
			{
				index = i;
			}
		}
		if (index != -1)
		{
			line = gString() +  this.guilds.get(index).getColor() + guildName + ChatColor.YELLOW + " was removed!";
			this.guilds.remove(index);
		}
		return line;
	}
	
	public String addGuild(String guildName, Player player, String guildKind)
	{
		boolean found = false;
		String line = "Guild not found!";
		ChatColor facColor = ChatColor.YELLOW;
		String playerName = player.getName();
		
		for (Guild fac : this.guilds)
		{
			if (fac.getName().equalsIgnoreCase(guildName))
			{
				found = true;
				facColor = fac.getColor();
				break;
			}
		}
		if (!found)
		{
			Guild guildToAdd = new Guild(guildName);
			guildToAdd = this.initiatePermissons(guildToAdd);
			guildToAdd.addAdmin(playerName);
			guildToAdd.setName(guildName);
			guildToAdd.addPlayer(playerName);
			guildToAdd.promotePlayer(playerName, 100);
			GuildKind gk = this.getGuildKindFromName(guildKind);
			if (gk != null)
			{
				guildToAdd.setKind(gk);
				if (GHolder.guildCreateCosts)
				{
					int c = 0;
					int lc = 0;
					for (ItemNeeded in : gk.getItemsNeeded())
					{
						if (in.getLevel() == 0)
						{
							if (playerHasItem(player, Material.getMaterial(in.getItemIndex()), in.getAmount()))
								c++;
							lc++;
						}
					}
					if (c != lc)
					{
						player.sendMessage(gString() + "Not enough material!");
						this.printItemsNeededToCreateGuild(player, gk);
						return null;
					}
					else
					{
						for (ItemNeeded in : gk.getItemsNeeded())
						{
							if (in.getLevel() == 0)
							{
								playerRemoveItems(player, Material.getMaterial(in.getItemIndex()), in.getAmount());
							}
						}
					}
				}
				this.removePlayerFromGuild(this.getPlayerGuild(playerName), playerName);
				guildToAdd.saveToFile();
				this.guilds.add(guildToAdd);
				line = gString() + guildToAdd.getColor() + guildName + ChatColor.YELLOW + " was created!";
			}
			else
				return gString() + "Invalid guildkind";
		}
		else
			line = gString() + facColor + guildName + ChatColor.YELLOW + " already exists!";
		return line;
	}
	
	private void printItemsNeededToCreateGuild(Player player, GuildKind gk)
	{
		for (ItemNeeded in : gk.getItemsNeeded())
		{
			if (in.getLevel() == 0)
			{
				String materialName = Material.getMaterial(in.getItemIndex()).name();
				materialName = materialName.toLowerCase();
				materialName = materialName.substring(0,1).toUpperCase() + materialName.substring(1, materialName.length());
				player.sendMessage(materialName + ": " +
						String.valueOf(this.playerGetAmountOfItems(player, Material.getMaterial(in.getItemIndex())))+
						"/" + String.valueOf(in.getAmount()));
			}
		}
	}
	
	private GuildKind getGuildKindFromName(String gkName)
	{
		for (GuildKind gk : GHolder.guildKinds)
		{
			if (gk.getName().equalsIgnoreCase(gkName))
				return gk;
		}
		return null;
	}
	
	private String addGuildAuto(String guildName)
	{
		boolean found = false;
		String line = "Guild not found!";
		for (Guild fac : this.guilds)
		{
			if (fac.getName().equalsIgnoreCase(guildName))
			{
				found = true;
				break;
			}
		}
		if (!found)
		{
			Guild guildToAdd = new Guild(guildName);
			guildToAdd = this.initiatePermissons(guildToAdd);
			if (guildToAdd.loadFromFile(guildName) == true)
			{
				this.guilds.add(guildToAdd);
				line = "[Guilds] " + guildName + " was added!";
			}
			else
				line = "[Guilds] " + "Failed to add guild!";
		}
		else
			line = "[Guilds] " + guildName + " already exists!";
		return line;
	}
	
	public Guild initiatePermissons(Guild guild)
	{
		guild.setPerm("invite", 10);
		guild.setPerm("kick", 10);
		guild.setPerm("promote", 10);
		guild.setPerm("demote", 10);
		guild.setPerm("build", 10);
		guild.setPerm("destroy", 10);
		guild.setPerm("set home", 10);
		guild.setPerm("use chest", 10);
		guild.setPerm("use workbench", 10);
		guild.setPerm("use furnace", 10);
		guild.setPerm("use dispenser", 10);
		guild.setPerm("eat cake", 10);
		guild.setPerm("open doors", 10);
		guild.setPerm("pull levers", 10);
		guild.setPerm("push buttons", 10);
		guild.setPerm("trigger tnt", 10);
		return guild;
	}
	
	public String activateGuild(String name)
	{
		String rtrln = "";
		ArrayList<String> guildsActivated = new ArrayList<String>();
		boolean has = false;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("Guilds" + File.separator + "active-guilds.txt"));
			try
			{
				String line = br.readLine();
				if (line != null)
				{
					while (line != null)
					{
						if (!line.startsWith("#") && !line.startsWith("//") && !line.startsWith(";") && !line.equals(""))
						{
							if (line.equalsIgnoreCase(name))
							{
								rtrln = "Guild is already activated";
								has = true;
								break;
							}
							guildsActivated.add(line);
						}
						line = br.readLine();
					}
				}
			}
			finally
			{
				br.close();
			}
			if (!has)
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter("Guilds" + File.separator + "active-guilds.txt"));
				try
				{
					guildsActivated.add(name);
					boolean first = true;
					for (String gld : guildsActivated)
					{
						if (first)
						{
							first = false;
							bw.write(gld);
						}
						else
						{
							bw.newLine();
							bw.write(gld);
						}
					}
					rtrln = "Activated " + name + "!";
				}
				finally
				{
					bw.flush();
					bw.close();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return rtrln;
	}
	
	public boolean addGuild(Guild fac)
	{
		if (!this.guilds.contains(fac))
		{
			this.guilds.add(fac);
			return true;
		}
		else
			return false;
	}
	
	protected boolean loadGuilds()
	{
		this.guilds.clear();
		boolean s = false;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("Guilds" + File.separator + "active-guilds.txt"));
			try
			{
				String line = br.readLine();
				while (line != null)
				{
					if (!line.startsWith("#") && !line.startsWith("//") && !line.startsWith(";") && !line.equals(""))
					{
						System.out.println( this.addGuildAuto(line));
					}
					line = br.readLine();
				}
				s = true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				s = false;
			}
			finally
			{
				br.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			s = false;
		}
		return s;
	}

	public boolean saveGuilds()
	{
		boolean s = false;
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter("Guilds" + File.separator + "active-guilds.txt"));
			try
			{
				for (Guild fac : this.guilds)
				{
					fac.saveToFile();
					bw.write(fac.getName());
					bw.newLine();
				}
				s = true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				s = false;;
			}
			finally
			{
				bw.flush();
				bw.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			s = false;
		}
		return s;
	}
	
	public String setArea(double x, double z, String name)
	{
		String message = gString() + "Failed to set area.";
		for(Guild guild : guilds)
		{
			boolean found = false;
			for(GPlayer player : guild.getPlayers())
			{
				if(player.getName().equals(name))
				{
					if (player.getRank() >= guild.getPerm("set home") || guild.isPlayerAdmin(name))
					{
						guild.getGuildArea().xzSet(x, z);
						guild.getGuildArea().areaSize(guild.getPlayers().size());
						message = gString() + "A new home has been set for the guild: " + guild.getColor() +  guild.getName();
						guild.saveToFile();
						found = true;
						break;
					}
					else
					{
						message = gString() + "You do not have permission to do that!";
						found = true;
					}
				}
			}
			if (found)
				break;
		}
		
		return message;
	}
	
	public String getPlayerGuildWithColor(String name)
	{
		for(Guild guild : guilds)
		{
			if (guild.hasMember(name))
			{
				return guild.getColor() + guild.getName() + ChatColor.YELLOW;
			}
		}
		return null;
	}
	
	public String getPlayerGuild(String name)
	{
		for(Guild guild : guilds)
		{
			if (guild.hasMember(name))
			{
				return guild.getName();
			}
		}
		return null;
	}
	
	public String getGuild(String name)
	{
		String message = gString() + "You are not in any guild yet.";
		for(Guild guild : guilds)
		{
			if (guild.hasMember(name))
			{
				message = gString() + "Your guild: " + guild.getColor() + guild.getName();
				break;
			}
		}
		
		return message;
	}
	
	public void loadConfig()
	{
		try {
            properties.load();
            System.out.println("[Guilds] Config Loaded!");
        } catch (IOException e) {
        	System.out.println("[Guilds] Failed to load configuration: "
                    + e.getMessage());
        }
        guildAreaExpansion = properties.getInt("Guild area expansion rate", guildAreaExpansion);
        guildAreaStartSize = properties.getInt("Guild area start size", guildAreaStartSize);
        useChatTag = properties.getBoolean("Use guild tag in chat", useChatTag);
        chatFormat = properties.getString("Chat format", chatFormat);
        guildCreateCosts = properties.getBoolean("Creating guild do cost", guildCreateCosts);
        properties.save();
	}

	public String addGuildKind(String kindName)
	{
		boolean found = false;
		String line = "GuildKind not found!";
		for (GuildKind gk : GHolder.guildKinds)
		{
			if (gk.getName().equalsIgnoreCase(kindName))
			{
				found = true;
				break;
			}
		}
		if (!found)
		{
			GuildKind guildKindToAdd = new GuildKind(kindName);
			if (guildKindToAdd.readFromFile() == true)
			{
				GHolder.guildKinds.add(guildKindToAdd);
				line = "[Guilds] " + kindName + " guild kind was added!";
			}
			else
				line = "[Guilds] " + "Failed to add guild kind!";
		}
		else
			line = "[Guilds] " + kindName + " guild kind already exists!";
		return line;
	}
	
	public String joinParty(String playerName, String partyName)
	{
		String rtln = "Failed to join " + partyName + " party!";
		boolean partyExists = false;
		boolean playerHasParty = false;
		for (Party party : this.partys)
		{
			if (party.getName().equalsIgnoreCase(partyName))
			{
				partyExists = true;
				for (String player : party.getPlayers())
				{
					if (player.equalsIgnoreCase(playerName))
						playerHasParty = true;
				}
				if (!playerHasParty)
				{
					partyExists = true;
					if (party.addPlayer(playerName))
					{
						rtln = "You joined " + partyName + " party!";
					}
					else
						rtln = "You are already in " + partyName + " party!";
				}
				else
					rtln = "You are already in a party!";
			}
		}
		if (!partyExists)
		{
			rtln = partyName + " party doesn't exist!";
		}
		return rtln;
	}
	
	public String leaveParty(String playerName)
	{
		for (Party prty : this.partys)
		{
			for (String player : prty.getPlayers())
			{
				if (player.equalsIgnoreCase(playerName))
				{
					if (prty.removePlayer(playerName))
						return "You left " + prty.getName() + " party";
					else
						return "Failed to remove " + playerName + " from " + prty.getName() + " party";
				}
			}
		}
		return "You are not in a party!";
	}
	
	public String startParty(String playerName, String partyName)
	{
		for (Party party : this.partys)
		{
			if (party.getName().equalsIgnoreCase(partyName))
			{
				return "Party already exists!";
			}
		}
		this.partys.add(new Party(partyName, playerName));
		return "You started " + partyName + " party!";
	}
	
	public boolean loadGuildKinds()
	{
		this.guilds.clear();
		boolean s = false;
		try
		{
			BufferedReader br = new BufferedReader(new FileReader("Guilds" + File.separator + "GuildKinds" + File.separator + "active-kinds.txt"));
			try
			{
				String line = br.readLine();
				while (line != null)
				{
					if (!line.startsWith("#") && !line.startsWith("//") && !line.startsWith(";") && !line.equals(""))
					{
						System.out.println(addGuildKind(line));
					}
					line = br.readLine();
				}
				s = true;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				s = false;
			}
			finally
			{
				br.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			s = false;
		}
		return s;
	}
	
	public boolean sendPartyMessage(Player player, String message)
	{
		boolean isParty = false;
		for (Party party : this.partys)
		{
			for (String plr : party.getPlayers())
			{
				if (player.getName().equalsIgnoreCase(plr))
				{
					isParty = true;
					break;
				}
			}
			if(isParty)
			{
				for (String plr : party.getPlayers())
				{
					this.sendMsgToPlayer(plr, ChatColor.BLUE + "[Party] <" + getPlayerWithGuildColor(plr) + ChatColor.BLUE + "> " + message);
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean sendGuildMessage(Player player, String message)
	{
		boolean isGuild = false;
		for (Guild guild : this.guilds)
		{
			if (guild.hasMember(player.getName()))
				isGuild = true;
			if(isGuild)
			{
				for (GPlayer plr : guild.getPlayers())
				{
					this.sendMsgToPlayer(plr.getName(), ChatColor.GREEN + "[Guild] <" + guild.getColor() + player.getName() + ChatColor.GREEN + "> " + message);
				}
				return true;
			}
		}
		return false;
	}

	public void addCmd(String cmd, String par, String desc)
	{
		this.guildCommands.add(new Command(cmd, par, desc));
		this.tps.addCmd(cmd);
	}

	public ArrayList<Guild> getGuilds() {
		return guilds;
	}

	public void setGuilds(ArrayList<Guild> guilds) {
		this.guilds = guilds;
	}

	public ArrayList<String> getPendingGuildInvites() {
		return pendingGuildInvites;
	}

	public void setPendingGuildInvites(ArrayList<String> pendingGuildInvites) {
		this.pendingGuildInvites = pendingGuildInvites;
	}

	public tPermissions getTps() {
		return tps;
	}

	public void setTps(tPermissions tps) {
		this.tps = tps;
	}

	public ArrayList<Party> getPartys() {
		return partys;
	}

	public void setPartys(ArrayList<Party> partys) {
		this.partys = partys;
	}
	
}
