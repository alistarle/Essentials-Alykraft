package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.User;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public abstract class EssentialsToggleCommand extends EssentialsCommand
{
	String othersPermission;

	public EssentialsToggleCommand(String command, String othersPermission)
	{
		super(command);
		this.othersPermission = othersPermission;
	}

	protected Boolean matchToggleArgument(final String arg)
	{
		if (arg.equalsIgnoreCase("on") || arg.startsWith("ena") || arg.equalsIgnoreCase("1"))
		{
			return true;
		}
		else if (arg.equalsIgnoreCase("off") || arg.startsWith("dis") || arg.equalsIgnoreCase("0"))
		{
			return false;
		}
		return null;
	}

	protected void toggleOtherPlayers(final Server server, final CommandSender sender, final String[] args) throws PlayerNotFoundException, NotEnoughArgumentsException
	{
		if (args.length < 1 || args[0].trim().length() < 2)
		{
			throw new PlayerNotFoundException();
		}

		boolean skipHidden = sender instanceof Player && !ess.getUser(sender).isAuthorized("essentials.vanish.interact");
		boolean foundUser = false;
		final List<Player> matchedPlayers = server.matchPlayer(args[0]);
		for (Player matchPlayer : matchedPlayers)
		{
			final User player = ess.getUser(matchPlayer);
			if (skipHidden && player.isHidden())
			{
				continue;
			}
			foundUser = true;
			if (args.length > 1)
			{
				Boolean toggle = matchToggleArgument(args[1]);
				if (toggle == true)
				{
					togglePlayer(sender, player, true);
				}
				else
				{
					togglePlayer(sender, player, false);
				}
			}
			else
			{
				togglePlayer(sender, player, null);
			}
		}
		if (!foundUser)
		{
			throw new PlayerNotFoundException();
		}
	}

	abstract void togglePlayer(CommandSender sender, User user, Boolean enabled) throws NotEnoughArgumentsException;
}
