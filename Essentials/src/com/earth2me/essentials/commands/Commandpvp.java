package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;

import com.earth2me.essentials.User;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;


public class Commandpvp extends EssentialsToggleCommand
{
	public Commandpvp()
	{
		super("pvp", "essentials.pvp.others");
	}
	@Override
	protected void run(final Server server, final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		toggleOtherPlayers(server, sender, args);
	}

	@Override
	protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{
			if (args.length == 1)
			{
				Boolean toggle = matchToggleArgument(args[0]);
				if (toggle == null && user.isAuthorized(othersPermission))
				{
					toggleOtherPlayers(server, user, args);
				}
				else
				{
					User player = getPlayer(server, user, args, 0);
					user.sendMessage(_("whoisPvp", (player.isPvpModeEnabled() ? _("true") : _("false"))));
				}
			}
			else if (args.length == 2 && user.isAuthorized(othersPermission))
			{			
				toggleOtherPlayers(server, user, args);
			}
			else
			{
				user.pvpCooldown();
				togglePlayer(user, user, null);
			}
	}

	@Override
	void togglePlayer(CommandSender sender, User user, Boolean enabled)
	{
		if (enabled == null)
		{
			enabled = user.isPvpModeEnabled();
		}
		
		user.setPvpModeEnabled(enabled);
		user.sendMessage(_("pvpMode", enabled ? _("enabled")+" \u00a76dans 180 secondes" : _("disabled")+" \u00a76dans 180 secondes"));
		if (!sender.equals(user))
		{
			sender.sendMessage(_("pvpMode", _(enabled ? "pvpEnabledFor" : "pvpDisabledFor", user.getDisplayName())));
		}
	}
}
