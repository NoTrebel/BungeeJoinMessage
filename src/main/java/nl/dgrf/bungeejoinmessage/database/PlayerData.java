package nl.dgrf.bungeejoinmessage.database;


import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by david on 7/30.
 */

@SuppressWarnings({"unchecked", "MismatchedQueryAndUpdateOfCollection"})
public class PlayerData extends Database {
    private static final String SETUP_SQL = "(" +
            "`uuid` VARCHAR(36) NOT NULL," +
            "`lastname` VARCHAR(32) NOT NULL," +
            "`ip` VARCHAR(32) NOT NULL," +
            "`lastseen` BIGINT(32) NOT NULL";

    private Map<String, String> lastname = new HashMap<>();
    private Map<String, String> ip = new HashMap<>();
    private Map<String, Long> lastseen = new HashMap<>();

    public PlayerData() {
        super("playerdata", SETUP_SQL, "uuid");
    }

    public boolean createDataNotExist(String uuid) {

        if (getData("uuid", uuid, "uuid") != null) {
            return true;
        }

        ProxiedPlayer p = null;
        if (!uuid.equals("CONSOLE")) {
            p = ProxyServer.getInstance().getPlayer(UUID.fromString(uuid));
        }

        Connection conn = getSQLConnection();
        try (
                PreparedStatement ps = conn.prepareStatement("INSERT INTO " + tableName +
                        " (uuid, lastname, ip, lastseen) " +
                        "VALUES (?,?,?,?);")
        ) {
            if (uuid.equals("CONSOLE")) {
                setValues(ps, uuid, "Console", "127.0.0.1");
            } else if (p != null) {
                setValues(ps, uuid, p.getName(), p.getAddress().getAddress().getHostAddress());
            } else {
                setValues(ps, uuid, "", "");
            }
            insertDefaults(ps);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void insertDefaults(PreparedStatement ps) throws SQLException {
        setValues(3, ps, System.currentTimeMillis() / 1000, "");
    }

    private Map<String, Map> getData() {
        Map<String, Map> data = new HashMap<>();
        data.put("lastname", lastname);
        data.put("ip", ip);
        data.put("lastseen", lastseen);
        return data;
    }

    private Object getData(String uuid, String label) {
        if (getData().containsKey(label)) {
            if (!getData().get(label).containsKey(uuid)) {
                getData().get(label).put(uuid, getData("uuid", uuid, label));
            }
            return getData().get(label).get(uuid);
        }
        return getData("uuid", uuid, label);
    }

    private void setData(String uuid, String label, Object labelVal) {
        if (getData().containsKey(label)) {
            getData().get(label).put(uuid, labelVal);
        }
        setData("uuid", uuid, label, labelVal);
    }

    public String getIp(String uuid) {
        return (String) getData(uuid, "ip");
    }

    public void setIp(String uuid, String ip) {
        setData(uuid, "ip", ip);
    }

    public long getLastSeen(String uuid) {
        return (long) getData(uuid, "lastseen");
    }

    public void setLastSeen(String uuid, long lastSeen) {setData(uuid, "lastseen", lastSeen);}

    public String getName(String uuid) {
        return (String) getData(uuid, "lastname");
    }

    public void setName(String uuid, String name) {
        setData(uuid, "lastname", name);
    }
}

