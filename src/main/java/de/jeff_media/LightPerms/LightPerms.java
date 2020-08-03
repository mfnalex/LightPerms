package de.jeff_media.LightPerms;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.jeff_media.PluginUpdateChecker.PluginUpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class LightPerms extends JavaPlugin implements Listener, CommandExecutor {

	HashMap<UUID, PermissionAttachment> perms;
	PluginUpdateChecker updateChecker;

	public void onEnable() {
		saveDefaultConfig();
		updateChecker = new PluginUpdateChecker(this,"https://api.jeff-media.de/lightperms/latest-version.txt","https://www.spigotmc.org/resources/1-8-1-16-lightperms.62447/",null,"https://chestsort.de/donate");
		updateChecker.check(4*60*60);
		perms = new HashMap<>();
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("lp").setTabCompleter(new TabCompleter());
		getCommand("lp").setExecutor(new Commands(this));

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

	void debug(String text) {
		getLogger().warning(text);
	}


	public void addPermissions(Player p) {
		PermissionAttachment attachment = p.addAttachment(this);

		for(String permission : getConfig().getStringList("default.permissions")) {
			attachment.setPermission(permission, true);
		}
		for(String name : getConfig().getConfigurationSection("users").getKeys(false)) {
			for(String group : getConfig().getStringList("users."+name+".groups")) {
				for(String perm : getConfig().getStringList("groups."+group+".permissions")) {
					attachment.setPermission(perm,true);
				}
			}
			for(String perm : getConfig().getStringList("users."+name+".permissions")) {
				attachment.setPermission(perm, true);
			}
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
		removePermissions();
		saveConfig();
		HandlerList.unregisterAll((Listener) this);
	}

	void removePermissions() {
		for (Player player : getServer().getOnlinePlayers()) {
			PermissionAttachment attachment = perms.get(player.getUniqueId());
			player.removeAttachment(attachment);
		}
	}

	void reloadPermissions() {
		saveConfig();
		removePermissions();
		reloadConfig();
		addPermsToOnlinePlayers();
	}

}
