package nl.dgrf.bungeejoinmessage;


import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import nl.dgrf.bungeejoinmessage.database.PlayerData;
import nl.dgrf.bungeejoinmessage.util.Log;

import java.util.*;
import java.util.logging.Level;

public class PlayerListener implements Listener {
    private Set<UUID> quitAnnc;
    private Set<UUID> joinAnnc;
    private Set<UUID> firstJoinAnnc;
    private BungeeJoinMessage main;

    private PlayerData pD;
    private boolean announceJoin;
    private boolean announceQuit;
    private boolean announceFirstJoin;

    PlayerListener(BungeeJoinMessage main) {
        this.main = main;
        quitAnnc = new HashSet<>();
        joinAnnc = new HashSet<>();
        firstJoinAnnc = new HashSet<>();

        pD = main.getPlayerData();
    }

    /**
     * Event fired when a player completely logs in. A new PlayerData is created and
     * saved, and the player is added to the joinAnnounce list
     *
     * @param event The Post Login Event.
     */
    @EventHandler
    public void postLogin(PostLoginEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        if(this.announceJoin && pD.getData("uuid", uuid, "uuid") != null) {
            joinAnnc.add(event.getPlayer().getUniqueId());
        } else if(this.announceFirstJoin && pD.getData("uuid", uuid, "uuid") == null){
            firstJoinAnnc.add(event.getPlayer().getUniqueId());
        }
        pD.setName(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName());
        pD.setIp(event.getPlayer().getUniqueId().toString(), event.getPlayer().getAddress().getAddress().getHostAddress());
    }

    /**
     * Event fired when a player connects to a server.
     *
     * @param event The Server Connected Event.
     */
    @EventHandler(priority = Byte.MAX_VALUE)
    public void connect(ServerConnectedEvent event) {
        if (main.getConfig().getBoolean("log")) {
            Log.info("[CONNECT] " + event.getPlayer().getName() + " connected to " + event.getServer().getInfo().getName());
        }
        UUID uuid = event.getPlayer().getUniqueId();
        if(joinAnnc.contains(uuid)){
            joinAnnc.remove(uuid);
            String message = main.getConfig().getString("messages.joinMessage").replace("{{PLAYER}}", event.getPlayer().getName());
            BaseComponent[] text = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
            ProxyServer.getInstance().broadcast(text);
        } else if(firstJoinAnnc.contains(uuid)){
            firstJoinAnnc.remove(uuid);
            String message = main.getConfig().getString("messages.firstJoinMessage").replace("{{PLAYER}}", event.getPlayer().getName());
            BaseComponent[] text = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
            ProxyServer.getInstance().broadcast(text);
        }

    }


    /**
     * Catches PlayerDisconnectEvent and stores whether the player has the quit
     * announce permission, before the player's permissions are unloaded by the
     * permission manager.
     *
     * @param event The Disconnect Event.
     */
    @EventHandler(priority = Byte.MIN_VALUE)
    public void logoutPre(final PlayerDisconnectEvent event) {
        joinAnnc.remove(event.getPlayer().getUniqueId());
        UUID uuid = event.getPlayer().getUniqueId();
        if (this.announceQuit && event.getPlayer().getServer() != null) {
            quitAnnc.add(uuid);
        } else {
            quitAnnc.remove(uuid);
        }
        joinAnnc.remove(uuid);
        firstJoinAnnc.remove(uuid);
    }

    /**
     * Event fired when a player disconnects from the server. Player is saved for
     * fast relog, logout is announced and logged, and the PlayerData is saved and
     * removed from the registered list.
     *
     * @param event The Disconnect Event.
     */
    @EventHandler(priority = Byte.MAX_VALUE)
    public void logout(final PlayerDisconnectEvent event) {
        main.getPlayerData().setLastSeen(event.getPlayer().getUniqueId().toString(), System.currentTimeMillis());

        if (announceQuit && quitAnnc.contains(event.getPlayer().getUniqueId())) {
            quitAnnc.remove(event.getPlayer().getUniqueId());

            String message = main.getConfig().getString("messages.quitMessage").replace("{{PLAYER}}", event.getPlayer().getName());
            BaseComponent[] text = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
            ProxyServer.getInstance().broadcast(text);
        }
        if (main.getConfig().getBoolean("log")) {
            Log.info("[QUIT] " + event.getPlayer().getName() + " disconnected from the server");
        }
    }

    void updateEnabled() {
        this.announceJoin = main.getConfig().getBoolean("announcer.announceJoin");
        this.announceQuit = main.getConfig().getBoolean("announcer.announceQuit");
        this.announceFirstJoin = main.getConfig().getBoolean("announcer.announceFirstJoin");
    }
}
