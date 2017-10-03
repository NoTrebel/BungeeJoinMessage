package nl.dgrf.bungeejoinmessage;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import nl.dgrf.bungeejoinmessage.database.PlayerData;
import nl.dgrf.bungeejoinmessage.util.Log;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private Set<UUID> quitAnnc;
    private Set<UUID> joinAnnc;

    private PlayerData pD;

    PlayerListener() {
        quitAnnc = new HashSet<>();
        joinAnnc = new HashSet<>();

        pD = BungeeJoinMessage.getInstance().getPlayerData();
    }

    public void login(final LoginEvent event) {
        if (BungeeEssentials.getInstance().contains("fastRelog")) {
            if (connections.contains(event.getConnection().getAddress().getAddress())) {
                event.setCancelled(true);
                event.setCancelReason(Dictionary.format(Dictionary.FAST_RELOG_KICK).toLegacyText());
                return;
            }
            connections.add(event.getConnection().getAddress().getAddress());
            ProxyServer.getInstance().getScheduler().schedule(BungeeEssentials.getInstance(), () -> connections.remove(event.getConnection().getAddress().getAddress()), 5, TimeUnit.SECONDS);
        }
        if (BungeeEssentials.getInstance().contains("autoredirect")) {
            String[] ip = event.getConnection().getVirtualHost().getHostName().split("\\.");
            for (ServerInfo info : ProxyServer.getInstance().getServers().values()) {
                if (info.getName().equalsIgnoreCase(ip[0])) {
                    redirServer.put(event.getConnection().getAddress().getAddress(), info);
                    break;
                }
            }
        }
    }

    /**
     * Event fired when a player completely logs in. A new PlayerData is created and
     * saved, and the player is added to the playerlist and saved. Login is announced
     * and logged.
     *
     * @param event The Post Login Event.
     */
    @EventHandler
    public void postLogin(PostLoginEvent event) {
        pD.setName(event.getPlayer().getUniqueId().toString(), event.getPlayer().getName());
        pD.setIp(event.getPlayer().getUniqueId().toString(), event.getPlayer().getAddress().getAddress().getHostAddress());
        //TODO: fix permissions
        if (event.getPlayer().getServer() != null && event.getPlayer().hasPermission(Permissions.General.JOINANNC)) {
            joinAnnc.add(event.getPlayer().getUniqueId());

        }

    }

    /**
     * Event fired when a player connects to a server. If player is set to be redirected,
     * player is sent to that server and redirection is removed.
     *
     * @param event The Server Connected Event.
     */
    @EventHandler(priority = Byte.MAX_VALUE)
    public void connect(ServerConnectedEvent event) {
        if (redirServer.containsKey(event.getPlayer().getAddress().getAddress())) {
            ServerInfo info = redirServer.get(event.getPlayer().getAddress().getAddress());
            if (info.canAccess(event.getPlayer())) {
                event.getPlayer().connect(info);
            }
            redirServer.remove(event.getPlayer().getAddress().getAddress());
        }
        if (BungeeEssentials.getInstance().contains("fulllog")) {
            Log.info(Dictionary.format("[CONNECT] {{ PLAYER }} connected to {{ SERVER }}.", "PLAYER", event.getPlayer().getName(), "SERVER", event.getServer().getInfo().getName()).toLegacyText());
        }
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
        if (event.getPlayer().getServer() != null && event.getPlayer().hasPermission(Permissions.General.QUITANNC)) {
            quitAnnc.add(event.getPlayer().getUniqueId());
        } else {
            quitAnnc.remove(event.getPlayer().getUniqueId());
        }
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
        BungeeEssentials.getInstance().getPlayerData().setLastSeen(event.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
        if (BungeeEssentials.getInstance().contains("fastRelog")) {
            if (!connections.contains(event.getPlayer().getAddress().getAddress())) {
                connections.add(event.getPlayer().getAddress().getAddress());
                ProxyServer.getInstance().getScheduler().schedule(BungeeJoinMessage.getInstance(), () -> connections.remove(event.getPlayer().getAddress().getAddress()), 3, TimeUnit.SECONDS);
            }
        }
        if (BungeeEssentials.getInstance().contains("joinAnnounce") && !(Dictionary.FORMAT_QUIT.equals("")) && quitAnnc.contains(event.getPlayer().getUniqueId()) && !pD.isHidden(event.getPlayer().getUniqueId().toString())) {
            ProxyServer.getInstance().broadcast(Dictionary.format(Dictionary.FORMAT_QUIT, "PLAYER", event.getPlayer().getName()));
        }
        if (BungeeEssentials.getInstance().contains("fulllog")) {
            Log.log(Dictionary.format("[QUIT] " + Dictionary.FORMAT_QUIT, "PLAYER", event.getPlayer().getName()).toLegacyText());
        }
    }