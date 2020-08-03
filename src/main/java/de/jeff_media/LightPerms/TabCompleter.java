package de.jeff_media.LightPerms;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    final String[] commands = new String[] {
            "user","group","listgroups","reload"
    };
    final String[] user = new String[] {
            "add","remove","addgroup","removegroup","info"
    };
    final String[] group = new String[] {
            "add","remove","info","listmembers","addmember","removemember"
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
                if(args.length==2) return null;
                if(args.length==3) return Arrays.asList(user);
                return null;
            case "group":
                if(args.length==2) return null;
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
