package de.jeff_media.LightPerms;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands implements CommandExecutor {

    final String header = "§3====[§bLightPerms§3]====";

    final LightPerms main;

    Commands(LightPerms main) {
        this.main = main;
    }

    private boolean addGroup(@NotNull OfflinePlayer p, String arg, CommandSender sender) {
        sender.sendMessage(header);
        List<String> groups;
        groups = main.getConfig().getStringList("users." + p.getName() + ".groups");
        if(groups.contains(arg)) {
            sender.sendMessage("§2Player §a"+p.getName()+" already is in group §a"+arg+"§2.");
            return true;
        }
        groups.add(arg);
        main.getConfig().set("users." + p.getName() + ".groups", groups);
        sender.sendMessage("§2Added player §a" + p.getName() + "§2 to group §a" + arg + "§2.");
        main.reloadPermissions();
        return true;
    }

    private boolean addGroupParent(String group, String arg, CommandSender sender) {
        List<String> parents = main.getConfig().getStringList("groups." + group + ".parents");
        if (!parents.contains(arg)) {
            parents.add(arg);
            main.getConfig().set("groups." + group + ".parents", parents);
            sender.sendMessage("§2Group §a"+group+"§2 now has parent §a"+arg+"§2.");
            main.reloadPermissions();
            return true;
        }
        sender.sendMessage("§2Group §a"+group+"§2 already has parent §a"+arg+"§2.");
        return true;
    }

    private boolean addGroupPermission(String group, String arg, CommandSender sender) {
        sender.sendMessage(header);
        List<String> perms;
        perms = main.getConfig().getStringList("groups." + group + ".permissions");
        if(perms.contains(arg)) {
            sender.sendMessage("§2Group §a"+group+"§2 already has permission §a"+arg+"§2.");
            return true;
        }
        perms.add(arg);
        main.getConfig().set("groups." + group + ".permissions", perms);
        sender.sendMessage("§2Added permission §a" + arg + "§2 to group §a" + group + "§2.");
        main.reloadPermissions();
        return true;
    }

    private boolean addPermission(@Nullable OfflinePlayer p, String arg, CommandSender sender) {
        sender.sendMessage(header);
        List<String> perms;
        if (p == null) {
            perms = main.getConfig().getStringList("default.permissions");
            if(perms.contains(arg)) {
                sender.sendMessage("§2Permission §a"+arg+"§2 already is a default permission.");
                return true;
            }
            perms.add(arg);
            main.getConfig().set("default.permissions", perms);
            sender.sendMessage("§2Added permission §a" + arg + "§2 to §aall players§2.");
        } else {
            perms = main.getConfig().getStringList("users." + p.getName() + ".permissions");
            if(perms.contains(arg)) {
                sender.sendMessage("§2Player §a"+p.getName()+"§2 already has permission §a"+arg+"§2.");
                return true;
            }
            perms.add(arg);
            main.getConfig().set("users." + p.getName() + ".permissions", perms);
            sender.sendMessage("§2Added permission §a" + arg + "§2 to player §a" + p.getName() + "§2.");
        }
        main.reloadPermissions();
        return true;
    }

    private boolean defaultGroup(CommandSender sender, String[] args) {
        args = shift(args);
        if (args.length < 1) {
            usage(sender, "default");
            return true;
        }
        if (args[0].equalsIgnoreCase("info")) {
            return listDefaultPermissions(sender);
        }
        if (args.length < 2) {
            usage(sender, "default");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "add":
                return addPermission(null, args[1], sender);
            case "remove":
                return removePermission(null, args[1], sender);
        }
        usage(sender, "default");
        return false;
    }

    private boolean group(CommandSender sender, String[] args) {
        args = shift(args);
        if (args.length < 1) {
            usage(sender, "group");
            return true;
        }
        if ((args.length > 1 && args[1].equalsIgnoreCase("info")) || args.length == 1) {
            return listGroupPermissions(args, sender);
        }
        if (args[1].equalsIgnoreCase("family")) {
            return listFamily(sender, args[0]);
        }
        if (args.length < 3) {
            usage(sender, "group");
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "add":
                return addGroupPermission(args[0], args[2], sender);
            case "remove":
                return removeGroupPermission(args[0], args[2], sender);
            case "addparent":
                return addGroupParent(args[0], args[2], sender);
            case "removeparent":
                return removeGroupParent(args[0], args[2], sender);
        }

        if (args[1].equalsIgnoreCase("addmember") || args[1].equalsIgnoreCase("removemember")) {
            @SuppressWarnings("deprecation") @NotNull OfflinePlayer p = Bukkit.getOfflinePlayer(args[2]);
            if (p == null) {
                sender.sendMessage("§cCould not find player §4" + args[0] + "§c.");
                return true;
            }
            switch (args[1].toLowerCase()) {
                case "addmember":
                    return addGroup(p, args[0], sender);
                case "removemember":
                    return removeGroup(p, args[0], sender);
            }
        }
        usage(sender, "group");
        return true;
    }

    private boolean listDefaultPermissions(CommandSender sender) {
        sender.sendMessage(header);
        sender.sendMessage("§3Default permissions: ");
        for (String perm : main.getConfig().getStringList("default.permissions")) {
            sender.sendMessage("§3- §a" + perm);
        }
        return true;
    }

    private boolean listGroupPermissions(String[] args, CommandSender sender) {
        sender.sendMessage(header);
        sender.sendMessage("§3Group: §a" + args[0]);
        sender.sendMessage("§3Group permissions: ");
        for (String perm : main.getConfig().getStringList("groups." + args[0] + ".permissions")) {
            sender.sendMessage("§3- §a" + perm);
        }
        sender.sendMessage("§3Members: ");
        List<String> members = new ArrayList<>();
        for (String user : main.getConfig().getConfigurationSection("users").getKeys(false))
            if (main.getConfig().getStringList("users." + user + ".groups").contains(args[0])) {
                members.add(user);
            }
        sender.sendMessage("§a" + members);
        return true;
    }

    private boolean listGroups(CommandSender sender) {
        sender.sendMessage(header);
        sender.sendMessage("§3Groups:");
        for (String group : main.getConfig().getConfigurationSection("groups").getKeys(false)) {
            int users = 0;
            int perms = 0;
            for (String user : main.getConfig().getConfigurationSection("users").getKeys(false)) {
                if (main.getConfig().getStringList("users." + user + ".groups").contains(group)) {
                    users++;
                }
            }
            for(String perm : main.getConfig().getStringList("groups."+group+".permissions")) {
                perms++;
            }
            sender.sendMessage("§3- §a" + group + " §3(" + perms+" permissions, "+ users + " members)");
        }
        return true;
    }

    private boolean listFamily(CommandSender sender, String group)  {
        sender.sendMessage(header);
        sender.sendMessage("§3Group: §a" + group);
        sender.sendMessage("§3Children: ");
        List<String> children = new ArrayList<>();
        for (String g : main.getConfig().getConfigurationSection("groups").getKeys(false)) {
            if (main.getConfig().getStringList("groups."+g+".parents").contains(group))
                children.add(g);
        }
        sender.sendMessage("§a" + children);
        sender.sendMessage("§3Parents: ");
        List<String> parents = main.getConfig().getStringList("groups."+group+".parents");
        sender.sendMessage("§a" + parents);
        return true;
    }

    private boolean listPermissions(@NotNull OfflinePlayer p, CommandSender sender) {
        sender.sendMessage(header);
        sender.sendMessage("§3User: §a" + p.getName());
        sender.sendMessage("§3Default permissions: ");
        for (String perm : main.getConfig().getStringList("default.permissions")) {
            sender.sendMessage("§3- §a" + perm);
        }
        sender.sendMessage("§3User permissions: ");
        for (String perm : main.getConfig().getStringList("users." + p.getName() + ".permissions")) {
            sender.sendMessage("§3- §a" + perm);
        }
        sender.sendMessage("§3Groups: ");
        for (String group : main.getConfig().getStringList("users." + p.getName() + ".groups")) {
            sender.sendMessage("§3- §a" + group);
        }
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        if (args.length == 0) {
            usage(commandSender, "");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                main.reloadPermissionsWithoutSaving();
                commandSender.sendMessage(ChatColor.GREEN + "Permissions have been reloaded.");
                return true;
            case "default":
                return defaultGroup(commandSender, args);
            case "user":
                return user(commandSender, args);
            case "group":
                return group(commandSender, args);
            case "listgroups":
                return listGroups(commandSender);
            default:
                usage(commandSender, "");
        }
        return true;
    }

    private boolean removeGroup(@NotNull OfflinePlayer p, String arg, CommandSender sender) {
        sender.sendMessage(header);
        List<String> groups;
        groups = main.getConfig().getStringList("users." + p.getName() + ".groups");
        groups.remove(arg);
        main.getConfig().set("users." + p.getName() + ".groups", groups);
        sender.sendMessage("§2Removed player §a" + p.getName() + "§2 from group §a" + arg + "§2.");
        main.reloadPermissions();
        return true;
    }

    private boolean removeGroupParent(String group, String arg, CommandSender sender) {
        List<String> parents = main.getConfig().getStringList("groups." + group + ".parents");
        if (parents.contains(arg)) {
            parents.remove(arg);
            main.getConfig().set("groups." + group + ".parents", parents);
            sender.sendMessage("§2Group §a"+group+"§2 no longer has parent §a"+arg+"§2.");
            main.reloadPermissions();
            return true;
        }
        sender.sendMessage("§2Group §a"+group+"§2 doesn't have parent §a"+arg+"§2.");
        return true;
    }

    private boolean removeGroupPermission(String group, String arg, CommandSender sender) {
        sender.sendMessage(header);
        List<String> perms;
        perms = main.getConfig().getStringList("groups." + group + ".permissions");
        perms.remove(arg);
        main.getConfig().set("groups." + group + ".permissions", perms);
        sender.sendMessage("§2Removed permission §a" + arg + "§2 from group §a" + group + "§2.");
        main.reloadPermissions();
        return true;
    }

    private boolean removePermission(@Nullable OfflinePlayer p, String arg, CommandSender sender) {
        sender.sendMessage(header);
        List<String> perms;
        if (p == null) {
            perms = main.getConfig().getStringList("default.permissions");
            perms.remove(arg);
            main.getConfig().set("default.permissions", perms);
            sender.sendMessage("§2Removed permission §a" + arg + "§2 from §aall players§2.");
        } else {
            perms = main.getConfig().getStringList("users." + p.getName() + ".permissions");
            perms.remove(arg);
            main.getConfig().set("users." + p.getName() + ".permissions", perms);
            sender.sendMessage("§2Removed permission §a" + arg + "§2 from player §a" + p.getName() + "§2.");
        }
        main.reloadPermissions();
        return true;
    }

    private String[] shift(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

    void usage(CommandSender s, String p) {
        s.sendMessage(header);
        switch (p) {
            case "":
                s.sendMessage("§3LightPerms commands: §b/lp ...");
                s.sendMessage("§3- §bdefault §3- manage default permissions for everyone");
                s.sendMessage("§3- §buser <user> §3- manage permissions per user");
                s.sendMessage("§3- §bgroup <group> §3- manage permissions per group");
                s.sendMessage("§3- §blistgroups §3- list all groups");
                s.sendMessage("§3- §breload §3- reload permissions from config");
                break;
            case "default":
                s.sendMessage("§3Default subcommands: §b/lp default ...");
                s.sendMessage("§3- §binfo §3- show default permissions");
                s.sendMessage("§3- §badd §3- add default permission to everyone");
                s.sendMessage("§3- §bremove <permission> §3- remove default permission from everyone");
                break;
            case "user":
                s.sendMessage("§3User subcommands: §b/lp user <user> ...");
                s.sendMessage("§3- §binfo §3- show a user's permissions and groups");
                s.sendMessage("§3- §badd <permission> §3- add permission to a user");
                s.sendMessage("§3- §bremove <permission> §3- remove permission from a user");
                s.sendMessage("§3- §baddgroup <group> §3- add user to a group");
                s.sendMessage("§3- §bremovegroup <group> §3- remove user from a group");
                break;
            case "group":
                s.sendMessage("§3Group subcommands: §b/lp group <group> ...");
                s.sendMessage("§3- §binfo §3- show a group's permissions and members");
                s.sendMessage("§3- §badd <permission> §3- add permission to a group");
                s.sendMessage("§3- §bremove <permission> §3- remove permission from a group");
                s.sendMessage("§3- §baddmember <user> §3- add user to a group");
                s.sendMessage("§3- §bremovemember <user> §3- remove user from a group");
                s.sendMessage("§3- §baddparent <group> §3- add parent group to group");
                s.sendMessage("§3- §bremoveparent <group> §3- remove parent group to group");
                break;
        }
    }

    private boolean user(CommandSender sender, String[] args) {
        args = shift(args);
        if (args.length < 1) {
            usage(sender, "user");
            return true;
        }
        @SuppressWarnings("deprecation") @NotNull OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
        if (p == null) {
            sender.sendMessage("§cCould not find player §4" + args[0] + "§c.");
            return true;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("info")) {
            return listPermissions(p, sender);
        }
        if (args.length < 3) {
            usage(sender, "user");
            return true;
        }
        switch (args[1].toLowerCase()) {
            case "add":
                return addPermission(p, args[2], sender);
            case "remove":
                return removePermission(p, args[2], sender);
            case "addgroup":
                return addGroup(p, args[2], sender);
            case "removegroup":
                return removeGroup(p, args[2], sender);
        }
        usage(sender, "user");
        return true;
    }
}
