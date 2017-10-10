package nl.dgrf.bungeejoinmessage;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import nl.dgrf.bungeejoinmessage.Commands.BungeeJoin;
import nl.dgrf.bungeejoinmessage.database.PlayerData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;

/**
 * Created by Albert on 16-Feb-17.
 */
public class BungeeJoinMessage extends Plugin {
    private static BungeeJoinMessage instance;
    private File libDir;
    private File configFile;
    private PlayerData playerData;
    private Configuration config;
    private PlayerListener listen;

    /**
     * @return The BungeeEssentials instance.
     */
    public static BungeeJoinMessage getInstance() {
        return instance;
    }

    /**
     * @return The main config file.
     */
    public Configuration getConfig() {
        return this.config;
    }

    /**
     * @return The playerData database.
     */
    public PlayerData getPlayerData() {
        return this.playerData;
    }

    /**
     * Reload all the config files and re-register all activated commands and listeners.
     *
     * @return Whether reload was successful.
     */
    public boolean reload() {
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        ProxyServer.getInstance().getScheduler().cancel(this);
        ProxyServer.getInstance().getPluginManager().unregisterCommands(this);
        ProxyServer.getInstance().getPluginManager().unregisterListeners(this);

        playerData = new PlayerData();
        playerData.createDataNotExist("CONSOLE");

        listen = new PlayerListener((this));
        ProxyServer.getInstance().getPluginManager().registerListener(this, listen);

        getProxy().getPluginManager().registerCommand(this, new BungeeJoin(this));
        return true;
    }

    @Override
    public void onEnable() {
        instance = this;
        libDir = new File(getDataFolder(), "lib");
        configFile = new File(getDataFolder(), "config.yml");
        try {
            loadConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
        reload();
    }

    private void makeConfig() throws IOException {
        if (!getDataFolder().exists()) {
            if (!getDataFolder().mkdir()) {
                getLogger().log(Level.WARNING, "Unable to create config folder!");
            }
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            Files.copy(getResourceAsStream("config.yml"), file.toPath());
        }
    }

    // TODO Save config option
    public void saveConfig() {
    }

    /**
     * @return The lib directory.
     */
    public File getLibDir() {
        return this.libDir;
    }

    public void updateEnabled() {
        listen.updateEnabled();
    }

    /**
     * Tries to load all the config files.
     *
     * @throws IOException The IOException thrown if the files could not be created
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadConfig() throws IOException {
        if (!libDir.exists()) {
            libDir.mkdir();
        }
        if (!configFile.exists()) {
            makeConfig();
        }
        config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
    }

}

