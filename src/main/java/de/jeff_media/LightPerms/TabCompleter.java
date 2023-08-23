package de.jeff_media.LightPerms;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    final LightPerms main;

    TabCompleter(LightPerms main) {
        this.main = main;
    }

    final String[] commands = new String[] {
            "user","group","listgroups","reload"
    };
    final String[] user = new String[] {
            "add","remove","addgroup","removegroup","info"
    };
    final String[] group = new String[] {
            "add","remove","info","addmember","removemember", "family", "addparent", "removeparent",
    };
    final String[] defaultGroup = new String[] {
            "add","remove","info"
    };

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length<=1) {
            return Arrays.asList(commands);
        }
        switch(args[0].toLowerCase()) {
            case "user":
                List<String> players = new ArrayList<>();
                Bukkit.getServer().getOnlinePlayers().forEach(player -> players.add(player.getName()));
                if(args.length==2) return players;
                if(args.length==3) return Arrays.asList(user);
                return null;
            case "group":
                List<String> groups = new ArrayList<>();
                groups.addAll(main.getConfig().getConfigurationSection("groups").getKeys(false));
                if(args.length==2) return groups;
                if(args.length==3) return Arrays.asList(group);
                return null;
            case "listgroups":
                return null;
            case "default":
                if(args.length==2) return Arrays.asList(defaultGroup);
                return null;
        }
        return null;
    }
}
