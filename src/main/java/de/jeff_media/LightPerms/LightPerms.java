package de.jeff_media.LightPerms;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jeff_media.updatechecker.UpdateChecker;
import org.apache.commons.lang.math.NumberUtils;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
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
    YamlConfiguration updateCheckerYaml = new YamlConfiguration();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        initUpdateChecker();

        perms = new HashMap<>();
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("lp").setTabCompleter(new TabCompleter(this));
        getCommand("lp").setExecutor(new Commands(this));

        addPermsToOnlinePlayers();

        @SuppressWarnings("unused")
        Metrics metrics = new Metrics(this, 3575);
    }

    private void initUpdateChecker() {
        UpdateChecker.init(this,"https://api.jeff-media.de/lightperms/latest-version.txt")
                .setDownloadLink("https://www.spigotmc.org/resources/1-8-1-16-lightperms.62447/")
                .setChangelogLink(62447)
                .setDonationLink("https://paypal.me/mfnalex")
                .suppressUpToDateMessage(true);

        File updateCheckerFile = new File(getDataFolder(),"updatechecker.yml");
        if(!updateCheckerFile.exists()) {
            saveResource("updatechecker.yml",true);
        }
        try {
            updateCheckerYaml.load(updateCheckerFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
        if(updateCheckerYaml.getString("check-for-updates","true").equalsIgnoreCase("true")) {
            UpdateChecker.getInstance().checkNow().checkEveryXHours(updateCheckerYaml.getDouble("check-interval",4));
        } else if(updateCheckerYaml.getString("check-for-updates","true").equalsIgnoreCase("on-startup")) {
            UpdateChecker.getInstance().checkNow();
        }
    }

    // Returns 16 for 1.16, etc.
    static int getMcVersion() {
        String bukkitVersionString = Bukkit.getBukkitVersion();
        Pattern p = Pattern.compile("^1\\.(\\d*)\\.");
        Matcher m = p.matcher((bukkitVersionString));
        int version = -1;
        while (m.find()) {
            if (NumberUtils.isNumber(m.group(1)))
                version = Integer.parseInt(m.group(1));
        }
        return version;
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

        for (String permission : getConfig().getStringList("default.permissions")) {
            addPermission(attachment, permission);
            //System.out.println("Adding " + permission + " to " + p.getName() + " because default");
        }

        for (String group : getConfig().getStringList("users." + p.getName() + ".groups")) {
            System.out.println("User " + p.getName() + " is in group " + group);
            for (String perm : getConfig().getStringList("groups." + group + ".permissions")) {
                addPermission(attachment, perm);
                //System.out.println(" Adding " + perm + " to " + p.getName() + " because group " + group);
            }
            for (String parent : getConfig().getStringList("groups." + group + ".parents"))
                for (String perm : getConfig().getStringList("groups." + parent + ".permissions")) {
                    attachment.setPermission(perm, true);
                    //System.out.println(" Adding " + perm + " to " + p.getName() + " because group " + group);
                }
        }

        for (String perm : getConfig().getStringList("users." + p.getName() + ".permissions")) {
            addPermission(attachment, perm);
            //System.out.println("Adding " + perm + " to " + p.getName() + " because player");
        }

        perms.put(p.getUniqueId(), attachment);
        if (getMcVersion()>=13) {
            p.updateCommands();
        }
    }

    private void addPermission(PermissionAttachment attachment, String permission) {
        boolean positive = !permission.startsWith("-");
        if (!positive) {
            permission = permission.substring(1);
        }
        attachment.setPermission(permission, positive);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addPermissions(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PermissionAttachment attachment = perms.get(event.getPlayer().getUniqueId());
        if(attachment != null) {
            try {
                event.getPlayer().removeAttachment(attachment);
            } catch (Throwable ignored) {

            }
        }
    }

    @Override
    public void onDisable() {
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
        reloadPermissionsWithoutSaving();
    }

    void reloadPermissionsWithoutSaving() {
        removePermissions();
        reloadConfig();
        addPermsToOnlinePlayers();
    }

}
