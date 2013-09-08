package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.User;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;


public class Commandgod extends EssentialsToggleCommand
{
	public Commandgod()
	{
		super("god", "essentials.god.others");
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
				togglePlayer(user, user, toggle);
			}
		}
		else if (args.length == 2 && user.isAuthorized(othersPermission))
		{			
			toggleOtherPlayers(server, user, args);
		}
		else
		{
			togglePlayer(user, user, null);
		}
	}

	@Override
	void togglePlayer(CommandSender sender, User user, Boolean enabled)
	{
		if (enabled == null)
		{
			enabled = !user.isGodModeEnabled();
		}
		
		user.setGodModeEnabled(enabled);

		if (enabled && user.getHealth() != 0)
		{
			user.setHealth(user.getMaxHealth());
			user.setFoodLevel(20);
		}

		user.sendMessage(_("godMode", enabled ? _("enabled") : _("disabled")));
		if (!sender.equals(user))
		{
			sender.sendMessage(_("godMode", _(enabled ? "godEnabledFor" : "godDisabledFor", user.getDisplayName())));
		}
	}
}
