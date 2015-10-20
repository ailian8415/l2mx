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
import java.util.logging.Logger;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.AdminData;
import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.Hero;
import com.l2jserver.gameserver.model.olympiad.Olympiad;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * This class handles following admin commands: - admin|admin1/admin2/admin3/admin4/admin5 = slots for the 5 starting admin menus - gmliston/gmlistoff = includes/excludes active character from /gmlist results - silence = toggles private messages acceptance mode - diet = toggles weight penalty mode -
 * tradeoff = toggles trade acceptance mode - reload = reloads specified component from multisell|skill|npc|htm|item - set/set_menu/set_mod = alters specified server setting - saveolymp = saves olympiad state manually - manualhero = cycles olympiad and calculate new heroes.
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2007/07/28 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler
{
	private static final Logger _log = Logger.getLogger(AdminAdmin.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_admin",
		"admin_admin1",
		"admin_admin2",
		"admin_admin3",
		"admin_admin4",
		"admin_admin5",
		"admin_admin6",
		"admin_admin7",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		"admin_diet",
		"admin_tradeoff",
		"admin_set",
		"admin_set_mod",
		"admin_saveolymp",
		"admin_sethero",
		"admin_givehero",
		"admin_endolympiad",
		"admin_setconfig",
		"admin_config_server",
		"admin_gmon"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_admin"))
		{
			showMainPage(activeChar, command);
		}
		else if (command.equals("admin_config_server"))
		{
			showConfigPage(activeChar);
		}
		else if (command.startsWith("admin_gmliston"))
		{
			AdminData.getInstance().showGm(activeChar);
			activeChar.sendMessage("加入 GM 列表");
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_gmlistoff"))
		{
			AdminData.getInstance().hideGm(activeChar);
			activeChar.sendMessage("离开 GM 列表");
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_silence"))
		{
			if (activeChar.isSilenceMode()) // already in message refusal mode
			{
				activeChar.setSilenceMode(false);
				activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
			}
			else
			{
				activeChar.setSilenceMode(true);
				activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
			}
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_saveolymp"))
		{
			Olympiad.getInstance().saveOlympiadStatus();
			activeChar.sendMessage("奥赛系统已保存.");
		}
		else if (command.startsWith("admin_endolympiad"))
		{
			try
			{
				Olympiad.getInstance().manualSelectHeroes();
			}
			catch (Exception e)
			{
				_log.warning("An error occured while ending olympiad: " + e);
			}
			activeChar.sendMessage("设置英雄.");
		}
		else if (command.startsWith("admin_sethero"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final L2PcInstance target = activeChar.getTarget().isPlayer() ? activeChar.getTarget().getActingPlayer() : activeChar;
			target.setHero(!target.isHero());
			target.broadcastUserInfo();
		}
		else if (command.startsWith("admin_givehero"))
		{
			if (activeChar.getTarget() == null)
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final L2PcInstance target = activeChar.getTarget().isPlayer() ? activeChar.getTarget().getActingPlayer() : activeChar;
			if (Hero.getInstance().isHero(target.getObjectId()))
			{
				activeChar.sendMessage("此玩家已成为英雄.");
				return false;
			}
			
			if (!Hero.getInstance().isUnclaimedHero(target.getObjectId()))
			{
				activeChar.sendMessage("此玩家没有成为英雄的条件.");
				return false;
			}
			Hero.getInstance().claimHero(target);
		}
		else if (command.startsWith("admin_diet"))
		{
			try
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				if (st.nextToken().equalsIgnoreCase("on"))
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("无负重模式启用");
				}
				else if (st.nextToken().equalsIgnoreCase("off"))
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("无负重模式关闭");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getDietMode())
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("无负重模式关闭");
				}
				else
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("无负重模式启用");
				}
			}
			finally
			{
				activeChar.refreshOverloaded();
			}
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_tradeoff"))
		{
			try
			{
				String mode = command.substring(15);
				if (mode.equalsIgnoreCase("on"))
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("拒绝交易模式开启");
				}
				else if (mode.equalsIgnoreCase("off"))
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("拒绝交易模式关闭");
				}
			}
			catch (Exception ex)
			{
				if (activeChar.getTradeRefusal())
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("拒绝交易模式关闭");
				}
				else
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("拒绝交易模式开启");
				}
			}
			AdminHtml.showAdminHtml(activeChar, "gm_menu.htm");
		}
		else if (command.startsWith("admin_setconfig"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String pName = st.nextToken();
				String pValue = st.nextToken();
				if (Config.setParameterValue(pName, pValue))
				{
					activeChar.sendMessage("配置参数" + pName + " 设定为: " + pValue);
				}
				else
				{
					activeChar.sendMessage("参数无效!");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("用法: //setconfig <参数> <值>");
			}
			finally
			{
				showConfigPage(activeChar);
			}
		}
		else if (command.startsWith("admin_set"))
		{
			StringTokenizer st = new StringTokenizer(command);
			String[] cmd = st.nextToken().split("_");
			try
			{
				String[] parameter = st.nextToken().split("=");
				String pName = parameter[0].trim();
				String pValue = parameter[1].trim();
				if (Config.setParameterValue(pName, pValue))
				{
					activeChar.sendMessage("参数: " + pName + " 设定为 " + pValue);
				}
				else
				{
					activeChar.sendMessage("参数无效!");
				}
			}
			catch (Exception e)
			{
				if (cmd.length == 2)
				{
					activeChar.sendMessage("用法: //set parameter=value");
				}
			}
			finally
			{
				if (cmd.length == 3)
				{
					if (cmd[2].equalsIgnoreCase("mod"))
					{
						AdminHtml.showAdminHtml(activeChar, "mods_menu.htm");
					}
				}
			}
		}
		else if (command.startsWith("admin_gmon"))
		{
			// nothing
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(L2PcInstance activeChar, String command)
	{
		int mode = 0;
		String filename = null;
		try
		{
			mode = Integer.parseInt(command.substring(11));
		}
		catch (Exception e)
		{
		}
		switch (mode)
		{
			case 1:
				filename = "main";
				break;
			case 2:
				filename = "game";
				break;
			case 3:
				filename = "effects";
				break;
			case 4:
				filename = "server";
				break;
			case 5:
				filename = "mods";
				break;
			case 6:
				filename = "char";
				break;
			case 7:
				filename = "gm";
				break;
			default:
				filename = "main";
				break;
		}
		AdminHtml.showAdminHtml(activeChar, filename + "_menu.htm");
	}
	
	public void showConfigPage(L2PcInstance activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage();
		StringBuilder replyMSG = new StringBuilder("<html><title>L2JMX天堂II服务端配置设定</title><body>");
		replyMSG.append("<center><table width=270><tr><td width=60><button value=\"首页\" action=\"bypass -h admin_admin\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=150>服务器配置控制</td><td width=60><button value=\"返回\" action=\"bypass -h admin_admin4\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table></center><br>");
		replyMSG.append("<center><table width=260><tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("<tr><td><font color=\"00AA00\">掉落相关:</font></td><td></td><td></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">经验倍率</font> = " + Config.RATE_XP + "</td><td><edit var=\"参数1\" width=40 height=15></td><td><button value=\"设定\" action=\"bypass -h admin_setconfig RateXp $param1\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">S P 倍率</font> = " + Config.RATE_SP + "</td><td><edit var=\"参数2\" width=40 height=15></td><td><button value=\"设定\" action=\"bypass -h admin_setconfig RateSp $param2\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">掉落/回收倍率</font> = " + Config.RATE_CORPSE_DROP_CHANCE_MULTIPLIER + "</td><td><edit var=\"参数4\" width=40 height=15></td><td><button value=\"设定\" action=\"bypass -h admin_setconfig RateDropSpoil $param4\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td width=140></td><td width=40></td><td width=40></td></tr>");
		replyMSG.append("<tr><td><font color=\"00AA00\">强化相关:</font></td><td></td><td></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">元素原石</font> = " + Config.ENCHANT_CHANCE_ELEMENT_STONE + "</td><td><edit var=\"参数8\" width=40 height=15></td><td><button value=\"设定\" action=\"bypass -h admin_setconfig EnchantChanceElementStone $param8\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">元素水晶</font> = " + Config.ENCHANT_CHANCE_ELEMENT_CRYSTAL + "</td><td><edit var=\"参数9\" width=40 height=15></td><td><button value=\"设定\" action=\"bypass -h admin_setconfig EnchantChanceElementCrystal $param9\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">元素宝石</font> = " + Config.ENCHANT_CHANCE_ELEMENT_JEWEL + "</td><td><edit var=\"参数10\" width=40 height=15></td><td><button value=\"设定\" action=\"bypass -h admin_setconfig EnchantChanceElementJewel $param10\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		replyMSG.append("<tr><td><font color=\"LEVEL\">元素能量</font> = " + Config.ENCHANT_CHANCE_ELEMENT_ENERGY + "</td><td><edit var=\"参数11\" width=40 height=15></td><td><button value=\"设定\" action=\"bypass -h admin_setconfig EnchantChanceElementEnergy $param11\" width=40 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		
		replyMSG.append("</table></body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
}
