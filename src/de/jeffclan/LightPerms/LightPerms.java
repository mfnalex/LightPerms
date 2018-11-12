package de.jeffclan.LightPerms;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class LightPerms extends JavaPlugin implements Listener {

	HashMap<UUID, PermissionAttachment> perms;

	public void onEnable() {
		saveDefaultConfig();
		
		perms = new HashMap<UUID, PermissionAttachment>();
		getServer().getPluginManager().registerEvents(this, this);
		
		for (Player player : getServer().getOnlinePlayers()) {
			if (player != null)
				addPermissions(player);
		}
		
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this);
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
		for (Player player : getServer().getOnlinePlayers()) {
			if (player != null) {
				PermissionAttachment attachment = perms.get(player.getUniqueId());
				if(attachment != null) {
					player.removeAttachment(attachment);
				}
			}
		}
	}

}
