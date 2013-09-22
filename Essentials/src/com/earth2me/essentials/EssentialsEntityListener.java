package com.earth2me.essentials;

import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.utils.DateUtil;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;


public class EssentialsEntityListener implements Listener
{
	private static final Logger LOGGER = Logger.getLogger("Minecraft");
	private final IEssentials ess;
	private static final transient Pattern powertoolPlayer = Pattern.compile("\\{player\\}");

	public EssentialsEntityListener(IEssentials ess)
	{
		this.ess = ess;
	}

	// This method does something undocumented reguarding certain bucket types #EasterEgg
	@EventHandler(priority = EventPriority.LOW)
	public void onEntityDamage(final EntityDamageByEntityEvent event)
	{
		final Entity eAttack = event.getDamager();
		final Entity eDefend = event.getEntity();
		if (eAttack instanceof Player)
		{
			final User attacker = ess.getUser(eAttack);
			if (eDefend instanceof Player)
			{
				onPlayerVsPlayerDamage(event, (Player)eDefend, attacker);
			}
			else if (eDefend instanceof Ageable)
			{
				final ItemStack hand = attacker.getItemInHand();
				if (hand != null && hand.getType() == Material.MILK_BUCKET)
				{
					((Ageable)eDefend).setBaby();
					hand.setType(Material.BUCKET);
					attacker.setItemInHand(hand);
					attacker.updateInventory();
					event.setCancelled(true);
				}
			}
			attacker.updateActivity(true);
		}
		else if (eAttack instanceof Projectile && eDefend instanceof Player)
		{
			Entity shooter = ((Projectile)event.getDamager()).getShooter();
			if (shooter instanceof Player)
			{
				final User attacker = ess.getUser(shooter);
				onPlayerVsPlayerDamage(event, (Player)eDefend, attacker);
				attacker.updateActivity(true);
			}
		}
	}

	private void onPlayerVsPlayerDamage(final EntityDamageByEntityEvent event, final Player defender, final User attacker)
	{
		if (ess.getSettings().getLoginAttackDelay() > 0
			&& (System.currentTimeMillis() < (attacker.getLastLogin() + ess.getSettings().getLoginAttackDelay()))
			&& !attacker.isAuthorized("essentials.pvpdelay.exempt"))
		{
			event.setCancelled(true);
		}

		if (!defender.equals(attacker.getBase()) && (attacker.hasInvulnerabilityAfterTeleport() || ess.getUser(defender).hasInvulnerabilityAfterTeleport()))
		{
			event.setCancelled(true);
		}

		if (attacker.isGodModeEnabled() && !attacker.isAuthorized("essentials.god.pvp"))
		{
			event.setCancelled(true);
		}
		
		/*if (attacker.isGodModeEnabled() || attacker.isFlyModeEnabled) {
			attacker.sendMessage("Votre god vous a ete retire");
			attacker.setGodModeEnabled(false);
			//On reset le heal/god/fly cooldown
		}*/

		if (attacker.isHidden() && !attacker.isAuthorized("essentials.vanish.pvp"))
		{
			event.setCancelled(true);
		}
		
		if (!ess.getUser(defender).isPvpModeEnabled() && !attacker.isAuthorized("essentials.pvp.bypass"))
		{
			attacker.sendMessage("\u00a7eCe joueur ne d\u00e9sire pas se battre");
			event.setCancelled(true);
		}
		
		final Calendar now = new GregorianCalendar();
		final Calendar cooldownTime = new GregorianCalendar();
		
		if (!attacker.isPvpModeEnabled() && !attacker.isAuthorized("essentials.pvp.bypass"))
		{
			if (now.getTimeInMillis() <= attacker.getLastPvpTimestamp()) {
				cooldownTime.setTimeInMillis(attacker.getLastPvpTimestamp());
				attacker.sendMessage("\u00a7eVous devez attendre "+DateUtil.formatDateDiff(cooldownTime.getTimeInMillis())+" avant de pouvoir PvP");
			} else {
				attacker.sendMessage("\u00a7eVous ne pouvez pas vous battre en mode non-PvP");
			}
			event.setCancelled(true);
		}
		
		if (now.getTimeInMillis() <= ess.getUser(defender).getLastPvpTimestamp() && ess.getUser(defender).isPvpModeEnabled()) {
			cooldownTime.setTimeInMillis(ess.getUser(defender).getLastPvpTimestamp());
			ess.getUser(defender).sendMessage("\u00a7eVous devez attendre "+DateUtil.formatDateDiff(cooldownTime.getTimeInMillis())+" avant que votre PvP ne soit d\u00e9sactiv\u00e9");
		}		

		onPlayerVsPlayerPowertool(event, defender, attacker);
	}

	private void onPlayerVsPlayerPowertool(final EntityDamageByEntityEvent event, final Player defender, final User attacker)
	{
		final List<String> commandList = attacker.getPowertool(attacker.getItemInHand());
		if (commandList != null && !commandList.isEmpty())
		{
			for (final String tempCommand : commandList)
			{
				final String command = powertoolPlayer.matcher(tempCommand).replaceAll(defender.getName());
				if (command != null && !command.isEmpty() && !command.equals(tempCommand))
				{
					ess.scheduleSyncDelayedTask(
							new Runnable()
							{
								@Override
								public void run()
								{
									attacker.getServer().dispatchCommand(attacker.getBase(), command);
									LOGGER.log(Level.INFO, String.format("[PT] %s issued server command: /%s", attacker.getName(), command));
								}
							});

					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityDamage(final EntityDamageEvent event)
	{
		if (event.getEntity() instanceof Player && ess.getUser(event.getEntity()).isGodModeEnabled())
		{
			final Player player = (Player)event.getEntity();
			player.setFireTicks(0);
			player.setRemainingAir(player.getMaximumAir());
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityCombust(final EntityCombustEvent event)
	{
		if (event.getEntity() instanceof Player && ess.getUser(event.getEntity()).isGodModeEnabled())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeathEvent(final PlayerDeathEvent event)
	{
		final User user = ess.getUser(event.getEntity());
		if (user.isAuthorized("essentials.back.ondeath") && !ess.getSettings().isCommandDisabled("back"))
		{
			user.setLastLocation();
			user.sendMessage(_("backAfterDeath"));
		}
		if (!ess.getSettings().areDeathMessagesEnabled())
		{
			event.setDeathMessage("");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeathExpEvent(final PlayerDeathEvent event)
	{
		final User user = ess.getUser(event.getEntity());
		if (user.isAuthorized("essentials.keepxp"))
		{
			event.setKeepLevel(true);
			event.setDroppedExp(0);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onFoodLevelChange(final FoodLevelChangeEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			final User user = ess.getUser(event.getEntity());
			if (user.isGodModeEnabled())
			{
				if (user.isGodModeEnabledRaw())
				{
					user.setFoodLevel(20);
					user.setSaturation(10);
				}
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityRegainHealth(final EntityRegainHealthEvent event)
	{
		if (event.getRegainReason() == RegainReason.SATIATED && event.getEntity() instanceof Player
			&& ess.getUser(event.getEntity()).isAfk() && ess.getSettings().getFreezeAfkPlayers())
		{
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPotionSplashEvent(final PotionSplashEvent event)
	{
		for (LivingEntity entity : event.getAffectedEntities())
		{
			if (entity instanceof Player && ess.getUser(entity).isGodModeEnabled())
			{
				event.setIntensity(entity, 0d);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onEntityShootBow(EntityShootBowEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			final User user = ess.getUser(event.getEntity());
			if (user.isAfk())
			{
				user.updateActivity(true);
			}
		}
	}
}
