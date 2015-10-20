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

import java.util.Collection;
import java.util.StringTokenizer;

import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.model.CursedWeapon;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.util.StringUtil;

/**
 * This class handles following admin commands: - cw_info = displays cursed weapon status - cw_remove = removes a cursed weapon from the world, item id or name must be provided - cw_add = adds a cursed weapon into the world, item id or name must be provided. Target will be the weilder - cw_goto =
 * teleports GM to the specified cursed weapon - cw_reload = reloads instance manager
 * @version $Revision: 1.1.6.3 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminCursedWeapons implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cw_info",
		"admin_cw_remove",
		"admin_cw_goto",
		"admin_cw_reload",
		"admin_cw_add",
		"admin_cw_info_menu"
	};
	
	private int itemId;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		
		CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
		int id = 0;
		
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		
		if (command.startsWith("admin_cw_info"))
		{
			if (!command.contains("menu"))
			{
				activeChar.sendMessage("====== 受诅咒的武器资讯: ======");
				for (CursedWeapon cw : cwm.getCursedWeapons())
				{
					activeChar.sendMessage("> " + cw.getName() + " (" + cw.getItemId() + ")");
					if (cw.isActivated())
					{
						L2PcInstance pl = cw.getPlayer();
						activeChar.sendMessage("  持有者: " + (pl == null ? "无" : pl.getName()));
						activeChar.sendMessage("    性向指数: " + cw.getPlayerKarma());
						activeChar.sendMessage("    时间剩下: " + (cw.getTimeLeft() / 60000) + " 分.");
						activeChar.sendMessage("    杀人数量 : " + cw.getNbKills());
					}
					else if (cw.isDropped())
					{
						activeChar.sendMessage("  掉落地面.");
						activeChar.sendMessage("    时间剩下: " + (cw.getTimeLeft() / 60000) + " 分.");
						activeChar.sendMessage("    杀人数量 : " + cw.getNbKills());
					}
					else
					{
						activeChar.sendMessage("  尚未出现.");
					}
					activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
				}
			}
			else
			{
				final Collection<CursedWeapon> cws = cwm.getCursedWeapons();
				final StringBuilder replyMSG = new StringBuilder(cws.size() * 300);
				final NpcHtmlMessage adminReply = new NpcHtmlMessage();
				adminReply.setFile(activeChar.getHtmlPrefix(), "data/html/admin/cwinfo.htm");
				for (CursedWeapon cw : cwm.getCursedWeapons())
				{
					itemId = cw.getItemId();
					
					StringUtil.append(replyMSG, "<table width=270><tr><td>名称:</td><td>", cw.getName(), "</td></tr>");
					
					if (cw.isActivated())
					{
						L2PcInstance pl = cw.getPlayer();
						StringUtil.append(replyMSG, "<tr><td>持有者:</td><td>", (pl == null ? "无" : pl.getName()), "</td></tr>" + "<tr><td>性向:</td><td>", String.valueOf(cw.getPlayerKarma()), "</td></tr>" + "<tr><td>杀人数:</td><td>", String.valueOf(cw.getPlayerPkKills()), "/", String.valueOf(cw.getNbKills()), "</td></tr>" + "<tr><td>剩余时间:</td><td>", String.valueOf(cw.getTimeLeft() / 60000), " 分.</td></tr>" + "<tr><td><button value=\"移除\" action=\"bypass -h admin_cw_remove ", String.valueOf(itemId), "\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td><button value=\"传送\" action=\"bypass -h admin_cw_goto ", String.valueOf(itemId), "\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
					}
					else if (cw.isDropped())
					{
						StringUtil.append(replyMSG, "<tr><td>位置:</td><td>掉落地上</td></tr>" + "<tr><td>剩余时间:</td><td>", String.valueOf(cw.getTimeLeft() / 60000), " 分.</td></tr>" + "<tr><td>杀人数:</td><td>", String.valueOf(cw.getNbKills()), "</td></tr>" + "<tr><td><button value=\"移除\" action=\"bypass -h admin_cw_remove ", String.valueOf(itemId), "\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td><button value=\"传送\" action=\"bypass -h admin_cw_goto ", String.valueOf(itemId), "\" width=73 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
					}
					else
					{
						StringUtil.append(replyMSG, "<tr><td>位置:</td><td>不存在.</td></tr>" + "<tr><td><button value=\"给予目标\" action=\"bypass -h admin_cw_add ", String.valueOf(itemId), "\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td></td></tr>");
					}
					
					replyMSG.append("</table><br>");
				}
				adminReply.replace("%cwinfo%", replyMSG.toString());
				activeChar.sendPacket(adminReply);
			}
		}
		else if (command.startsWith("admin_cw_reload"))
		{
			cwm.reload();
		}
		else
		{
			CursedWeapon cw = null;
			try
			{
				String parameter = st.nextToken();
				if (parameter.matches("[0-9]*"))
				{
					id = Integer.parseInt(parameter);
				}
				else
				{
					parameter = parameter.replace('_', ' ');
					for (CursedWeapon cwp : cwm.getCursedWeapons())
					{
						if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
						{
							id = cwp.getItemId();
							break;
						}
					}
				}
				cw = cwm.getCursedWeapon(id);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //cw_remove|//cw_goto|//cw_add <itemid|name>");
			}
			
			if (cw == null)
			{
				activeChar.sendMessage("不是受诅咒的武器 ID");
				return false;
			}
			
			if (command.startsWith("admin_cw_remove "))
			{
				cw.endOfLife();
			}
			else if (command.startsWith("admin_cw_goto "))
			{
				cw.goTo(activeChar);
			}
			else if (command.startsWith("admin_cw_add"))
			{
				if (cw.isActive())
				{
					activeChar.sendMessage("这把受诅咒的武器已经出现。");
				}
				else
				{
					L2Object target = activeChar.getTarget();
					if (target instanceof L2PcInstance)
					{
						((L2PcInstance) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
					}
					else
					{
						activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
					}
					cw.setEndTime(System.currentTimeMillis() + (cw.getDuration() * 60000L));
					cw.reActivate();
				}
			}
			else
			{
				activeChar.sendMessage("指令错误.");
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}