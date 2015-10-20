/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items with respective id, if num is not specified, assumes 1.
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_itemcreate",
		"admin_create_item",
		"admin_create_coin",
		"admin_give_item_target",
		"admin_give_item_to_all"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.equals("admin_itemcreate"))
		{
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_item"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					long numval = Long.parseLong(num);
					createItem(activeChar, activeChar, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					createItem(activeChar, activeChar, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //create_item <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("Specify a valid number.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_create_coin"))
		{
			try
			{
				String val = command.substring(17);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					String name = st.nextToken();
					int idval = getCoinId(name);
					if (idval > 0)
					{
						String num = st.nextToken();
						long numval = Long.parseLong(num);
						createItem(activeChar, activeChar, idval, numval);
					}
				}
				else if (st.countTokens() == 1)
				{
					String name = st.nextToken();
					int idval = getCoinId(name);
					createItem(activeChar, activeChar, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //create_coin <name> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("请指定一个有效数值.");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_give_item_target"))
		{
			try
			{
				L2PcInstance target;
				if (activeChar.getTarget() instanceof L2PcInstance)
				{
					target = (L2PcInstance) activeChar.getTarget();
				}
				else
				{
					activeChar.sendMessage("目标错误.");
					return false;
				}
				
				String val = command.substring(22);
				StringTokenizer st = new StringTokenizer(val);
				if (st.countTokens() == 2)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					long numval = Long.parseLong(num);
					createItem(activeChar, target, idval, numval);
				}
				else if (st.countTokens() == 1)
				{
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					createItem(activeChar, target, idval, 1);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //give_item_target <itemId> [amount]");
			}
			catch (NumberFormatException nfe)
			{
				activeChar.sendMessage("请指定一个有效数值。");
			}
			AdminHtml.showAdminHtml(activeChar, "itemcreation.htm");
		}
		else if (command.startsWith("admin_give_item_to_all"))
		{
			String val = command.substring(22);
			StringTokenizer st = new StringTokenizer(val);
			int idval = 0;
			long numval = 0;
			if (st.countTokens() == 2)
			{
				String id = st.nextToken();
				idval = Integer.parseInt(id);
				String num = st.nextToken();
				numval = Long.parseLong(num);
			}
			else if (st.countTokens() == 1)
			{
				String id = st.nextToken();
				idval = Integer.parseInt(id);
				numval = 1;
			}
			int counter = 0;
			L2Item template = ItemTable.getInstance().getTemplate(idval);
			if (template == null)
			{
				activeChar.sendMessage("这个物品不存在。");
				return false;
			}
			if ((numval > 10) && !template.isStackable())
			{
				activeChar.sendMessage("这个物品不可堆叠的 - 创造取消。");
				return false;
			}
			for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
			{
				if ((activeChar != onlinePlayer) && onlinePlayer.isOnline() && ((onlinePlayer.getClient() != null) && !onlinePlayer.getClient().isDetached()))
				{
					onlinePlayer.getInventory().addItem("Admin", idval, numval, onlinePlayer, activeChar);
					onlinePlayer.sendMessage("管理员创造「" + numval + "」个「" + template.getName() + "」给你。");
					counter++;
				}
			}
			activeChar.sendMessage(counter + "个玩家得到：" + template.getName());
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void createItem(L2PcInstance activeChar, L2PcInstance target, int id, long num)
	{
		L2Item template = ItemTable.getInstance().getTemplate(id);
		if (template == null)
		{
			activeChar.sendMessage("这个物品不存在。");
			return;
		}
		if ((num > 10) && !template.isStackable())
		{
			activeChar.sendMessage("这个物品不可堆叠的 - 创造取消。");
			return;
		}
		
		target.getInventory().addItem("Admin", id, num, activeChar, null);
		
		if (activeChar != target)
		{
			target.sendMessage("管理员创造「" + num + "」个「" + template.getName() + "」给你。");
		}
		activeChar.sendMessage("創造「" + num + "」个「" + template.getName() + "」(" + id + ") 给「" + target.getName() + "」。");
	}
	
	private int getCoinId(String name)
	{
		int id;
		if (name.equalsIgnoreCase("金币"))
		{
			id = 57;
		}
		else if (name.equalsIgnoreCase("古代金币"))
		{
			id = 5575;
		}
		else if (name.equalsIgnoreCase("祝福金币"))
		{
			id = 6673;
		}
		else if (name.equalsIgnoreCase("蓝色伊娃"))
		{
			id = 4355;
		}
		else if (name.equalsIgnoreCase("金色殷海萨"))
		{
			id = 4356;
		}
		else if (name.equalsIgnoreCase("银色席琳"))
		{
			id = 4357;
		}
		else if (name.equalsIgnoreCase("血色帕格立欧"))
		{
			id = 4358;
		}
		else if (name.equalsIgnoreCase("梦幻岛金币"))
		{
			id = 13067;
		}
		else
		{
			id = 0;
		}
		
		return id;
	}
}
