/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.dgrf.bungeejoinmessage.Commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;
import nl.dgrf.bungeejoinmessage.BungeeJoinMessage;
import nl.dgrf.bungeejoinmessage.util.Log;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

/**
 * Class that interprets the BungeeJoin command
 *
 * @author NoTrebel
 */
public class BungeeJoin extends Command {
    private final BungeeJoinMessage main;
    private final Configuration config;

    public BungeeJoin(BungeeJoinMessage main) {
        super("BungeeJoin", "bungeejoin.admin");
        this.main = main;
        this.config = main.getConfig();

    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        //only allow execution of the command if the executor is a player
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            switch (args[0]) {
                case "status":
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&4Current status:")));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', "§2Join messages enabled: §f" + config.getBoolean("announcer.announceJoin"))));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', " §2Quit messages enabled: §f" + config.getBoolean("announcer.announceQuit"))));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', " §First time join messages enabled: §f" + config.getBoolean("announcer.announceFirstJoin"))));
                    break;
                case "enable":
                case "disable":
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&CNOT IMPLEMENTED YET! MODIFY THE CONFIG FILE AND RELOAD IT")));
                case "reload":
                    main.updateEnabled();
                    break;
                default:
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', "§4/bungeejoin §fhelp - §2Show this help.")));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', "§4/bungeejoin §fenable <§1arg§f> - §2enable a function")));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', "§4/bungeejoin §fdisable <§1arg§f> - §2disable a function")));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', "§4/bungeejoin §fstatus - §2 shows status")));
                    sender.sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('§', "§4/bungeejoin §reload - §2 reload config file")));
            }
        } else {
            Log.warning("This command can only be executed as a player");
        }
    }
}
