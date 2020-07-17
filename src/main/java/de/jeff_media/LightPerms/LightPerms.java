package de.jeff_media.LightPerms;

import java.util.HashMap;
import java.util.UUID;

import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class LightPerms extends JavaPlugin implements Listener, CommandExecutor {

	HashMap<UUID, PermissionAttachment> perms;
	PluginUpdateChecker updateChecker;

	public void onEnable() {
		saveDefaultConfig();
		updateChecker = new PluginUpdateChecker(this,"https://api.jeff-media.de/lightperms/latest-version.txt","https://www.spigotmc.org/resources/1-8-1-16-lightperms.62447/",null,"https://chestsort.de/donate");
		updateChecker.check(4*60*60);
		perms = new HashMap<UUID, PermissionAttachment>();
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("lp").setExecutor(this);

		addPermsToOnlinePlayers();

		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this,3575);
	}

	private void addPermsToOnlinePlayers() {
		for (Player player : getServer().getOnlinePlayers()) {
			if (player != null)
				addPermissions(player);
		}
	}

	public void addPermissions(Player p) {
		PermissionAttachment attachment = p.addAttachment(this);
		
		for(String permission : getConfig().getStringList("permissions")) {
			attachment.setPermission(permission, true);
		}
		
		perms.put(p.getUniqueId(), attachment);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		addPermissions(event.getPlayer());
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		event.getPlayer().removeAttachment(perms.get(event.getPlayer().getUniqueId()));
	}

	public void onDisable() {
		updateChecker.stop();
		for (Player player : getServer().getOnlinePlayers()) {
			if (player != null) {
				PermissionAttachment attachment = perms.get(player.getUniqueId());
				if(attachment != null) {
					player.removeAttachment(attachment);
				}
			}
		}
		HandlerList.unregisterAll((Listener) this);
	}

	public boolean 	onCommand​(CommandSender sender, Command command, String label, String[] args) {

		reloadConfig();
		onDisable();
		onEnable();
		sender.sendMessage(ChatColor.GREEN+"Permissions have been reloaded.");

		return true;
	}

}
