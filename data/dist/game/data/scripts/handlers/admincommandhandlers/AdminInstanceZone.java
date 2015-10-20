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

import java.util.Map;
import java.util.StringTokenizer;

import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.util.GMAudit;
import com.l2jserver.util.StringUtil;

public class AdminInstanceZone implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_instancezone",
		"admin_instancezone_clear"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		String target = (activeChar.getTarget() != null) ? activeChar.getTarget().getName() : "no-target";
		GMAudit.auditGMAction(activeChar.getName(), command, target, "");
		
		if (command.startsWith("admin_instancezone_clear"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command, " ");
				
				st.nextToken();
				final L2PcInstance player = L2World.getInstance().getPlayer(st.nextToken());
				final int instanceId = Integer.parseInt(st.nextToken());
				final String name = InstanceManager.getInstance().getInstanceIdName(instanceId);
				InstanceManager.getInstance().deleteInstanceTime(player.getObjectId(), instanceId);
				activeChar.sendMessage("副本" + name + "从「 " + player.getName() + "」清除。");
				player.sendMessage("管理员清除即时地区：" + name);
				
				return true;
			}
			catch (Exception e)
			{
				activeChar.sendMessage("清除副本时间失败 " + e.getMessage());
				activeChar.sendMessage("Usage: //instancezone_clear <playername> [instanceId]");
				return false;
			}
		}
		else if (command.startsWith("admin_instancezone"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			command = st.nextToken();
			
			if (st.hasMoreTokens())
			{
				L2PcInstance player = null;
				String playername = st.nextToken();
				
				try
				{
					player = L2World.getInstance().getPlayer(playername);
				}
				catch (Exception e)
				{
				}
				
				if (player != null)
				{
					display(player, activeChar);
				}
				else
				{
					activeChar.sendMessage("玩家「" + playername + "」不在线");
					activeChar.sendMessage("Usage: //instancezone [playername]");
					return false;
				}
			}
			else if (activeChar.getTarget() != null)
			{
				if (activeChar.getTarget() instanceof L2PcInstance)
				{
					display((L2PcInstance) activeChar.getTarget(), activeChar);
				}
			}
			else
			{
				display(activeChar, activeChar);
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void display(L2PcInstance player, L2PcInstance activeChar)
	{
		Map<Integer, Long> instanceTimes = InstanceManager.getInstance().getAllInstanceTimes(player.getObjectId());
		
		final StringBuilder html = StringUtil.startAppend(500 + (instanceTimes.size() * 200), "<html><center><table width=260><tr>" + "<td width=40><button value=\"首页\" action=\"bypass -h admin_admin\" width=40 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td width=180><center>玩家副本</center></td>" + "<td width=40><button value=\"返回\" action=\"bypass -h admin_current_player\" width=40 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "</tr></table><br><font color=\"LEVEL\">玩家「", player.getName(), "」的副本</font><center><br>" + "<table>" + "<tr><td width=150>名称</td><td width=50>时间</td><td width=70>状态</td></tr>");
		
		for (int id : instanceTimes.keySet())
		{
			int hours = 0;
			int minutes = 0;
			long remainingTime = (instanceTimes.get(id) - System.currentTimeMillis()) / 1000;
			if (remainingTime > 0)
			{
				hours = (int) (remainingTime / 3600);
				minutes = (int) ((remainingTime % 3600) / 60);
			}
			
			StringUtil.append(html, "<tr><td>", InstanceManager.getInstance().getInstanceIdName(id), "</td><td>", String.valueOf(hours), ":", String.valueOf(minutes), "</td><td><button value=\"清除\" action=\"bypass -h admin_instancezone_clear ", player.getName(), " ", String.valueOf(id), "\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		}
		
		StringUtil.append(html, "</table></html>");
		
		final NpcHtmlMessage ms = new NpcHtmlMessage();
		ms.setHtml(html.toString());
		
		activeChar.sendPacket(ms);
	}
}