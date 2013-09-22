package com.earth2me.essentials.commands;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.User;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;


public class Commandheal extends EssentialsCommand
{
	public Commandheal()
	{
		super("heal");
	}

	@Override
	public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception
	{

		if (args.length > 0 && user.isAuthorized("essentials.heal.others")) //Si il a le droit de heal les autre
		{
			if (args[0].trim().length() < 2) //si le nom du joueur est inferieur a 2 caracteres
			{
				throw new PlayerNotFoundException();
			}
			if (!user.isAuthorized("essentials.heal.cooldown.bypass")) //si il n'a pas de le droit de bypass le cd de /heal
			{
				user.healCooldown();
			}
			healOtherPlayers(server, user, args[0]);
			return;
		}

		if (!user.isAuthorized("essentials.heal.cooldown.bypass"))
		{
			user.healCooldown();
		}
		
		if(!user.isPvpModeEnabled() || user.isAuthorized("essentials.pvp.bypass")) {
			healPlayer(user);
		} else {
			user.sendMessage("\u00a77Vous devez quitter le mode PvP");
		}
	}

	@Override
	public void run(final Server server, final CommandSender sender, final String commandLabel, final String[] args) throws Exception
	{
		if (args.length < 1)
		{
			throw new NotEnoughArgumentsException();
		}

		healOtherPlayers(server, sender, args[0]);
	}

	private void healOtherPlayers(final Server server, final CommandSender sender, final String name) throws Exception
	{
		boolean skipHidden = sender instanceof Player && !ess.getUser(sender).isAuthorized("essentials.vanish.interact");
		boolean foundUser = false;
		final List<Player> matchedPlayers = server.matchPlayer(name);
		for (Player matchPlayer : matchedPlayers)
		{
			final User player = ess.getUser(matchPlayer);
			if (skipHidden && player.isHidden())
			{
				continue;
			}
			foundUser = true;
			try
			{
				healPlayer(matchPlayer);
				sender.sendMessage(_("healOther", matchPlayer.getDisplayName()));
			}
			catch (QuietAbortException e)
			{
				//Handle Quietly
			}
		}
		if (!foundUser)
		{
			throw new PlayerNotFoundException();
		}
	}

	private void healPlayer(final Player player) throws Exception
	{
		if (player.getHealth() == 0) //Si le joueur est mort
		{
			throw new Exception(_("healDead"));
		}

		final int amount = player.getMaxHealth() - player.getHealth();
		final EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(player, amount, RegainReason.CUSTOM);
		ess.getServer().getPluginManager().callEvent(erhe);
		if (erhe.isCancelled())
		{
			throw new QuietAbortException();
		}

		int newAmount = player.getHealth() + erhe.getAmount();
		if (newAmount > player.getMaxHealth())
		{
			newAmount = player.getMaxHealth();
		}

		player.setHealth(newAmount);
		player.setFoodLevel(20);
		player.setFireTicks(0);
		player.sendMessage(_("heal"));
		for (PotionEffect effect : player.getActivePotionEffects())
		{
			player.removePotionEffect(effect.getType());
		}
	}
}
