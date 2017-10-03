package nl.dgrf.bungeejoinmessage.database;

import nl.dgrf.bungeejoinmessage.BungeeJoinMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public abstract class Database {

    String tableName;

    private Connection connection;
    private String primary;
    private String url;
    private String username;
    private String password;
    private int uses = 0;


    public Database(String tableName, String setupSql, String primary) {
        this.tableName = tableName;
        this.primary = primary;
        load(setupSql, primary);
    }

     public String getTableName() {
        return tableName;
    }



    public Object getData(String key, String keyVal, String label) {
        List<Object> datas = getDataMultiple(key, keyVal, label);
        if (datas != null) {
            return datas.get(0);
        }
        return null;
    }

    public List<Object> getDataMultiple(String key, String keyVal, String label) {
        //TODO: Cache queries into memory
        List<Object> datas = new ArrayList<>();

        Connection conn = getSQLConnection();
        try (
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE " + key + " = ?;")
        ) {
            ps.setObject(1, keyVal);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                if (rs.getString(key).equals(keyVal)) {
                    datas.add(rs.getObject(label));
                }
            }

            if (datas.size() > 0) {
                return datas;
            }

        } catch (SQLException e) {
            BungeeJoinMessage.getInstance().getLogger().log(Level.SEVERE, "Couldn't execute SQLite statement: ", e);
        }
        return null;
    }

    public void setData(String key, String keyVal, String label, Object labelVal) {
        if (key.equals(primary) && !createDataNotExist(keyVal)) {
            return;
        }

        Connection conn = getSQLConnection();
        try (
                PreparedStatement ps = conn.prepareStatement("UPDATE " + tableName + " SET " + label + " = ? WHERE " + key + " = ?;")
        ) {
            ps.setObject(1, labelVal);
            ps.setObject(2, keyVal);
            ps.executeUpdate();
        } catch (SQLException e) {
            BungeeJoinMessage.getInstance().getLogger().log(Level.SEVERE, "Couldn't execute SQLite statement: ", e);
        }
    }

    void setValues(PreparedStatement ps, Object... values) throws SQLException {
        setValues(1, ps, values);
    }

    void setValues(int start, PreparedStatement ps, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            ps.setObject(i + start, values[i]);
        }
    }


    Connection getSQLConnection() {
        File dbFile = new File(BungeeJoinMessage.getInstance().getDataFolder(), tableName + ".db");
        if (!dbFile.exists()) {
            try {
                dbFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if(uses > 250) {
                connection.close();
            }

            if (connection != null && !connection.isClosed()) {
                uses++;
                return connection;
            }

            File sqliteLib = new File(BungeeJoinMessage.getInstance().getLibDir(), "sqlite-jdbc-3.8.11.2.jar");

            if (!sqliteLib.exists()) {
                BungeeJoinMessage.getInstance().getLogger().log(Level.INFO, "Downloading SQLite JDBC library...");
                String dlLink = "https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.8.11.2.jar";
                URLConnection con;
                try {
                    URL url = new URL(dlLink);
                    con = url.openConnection();
                } catch (IOException e) {
                    BungeeJoinMessage.getInstance().getLogger().log(Level.SEVERE, "Invalid SQLite download link. Please contact plugin author.");
                    return null;
                }

                try (
                        InputStream in = con.getInputStream();
                        FileOutputStream out = new FileOutputStream(sqliteLib)
                ) {
                    byte[] buffer = new byte[1024];
                    int size;
                    while ((size = in.read(buffer)) != -1) {
                        out.write(buffer, 0, size);
                    }
                } catch (IOException e) {
                    BungeeJoinMessage.getInstance().getLogger().log(Level.WARNING, "Failed to download update, please download it manually from https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.8.11.2.jar and put it in the /plugins/BungeeJoinMessage/lib folder.");
                    BungeeJoinMessage.getInstance().getLogger().log(Level.WARNING, "Error message: ");
                    e.printStackTrace();
                    return null;
                }
            }

            URLClassLoader loader = new URLClassLoader(new URL[]{sqliteLib.toURI().toURL()});
            Method m = DriverManager.class.getDeclaredMethod("getConnection", String.class, Properties.class, Class.class);
            m.setAccessible(true);

            connection = (Connection) m.invoke(null, "jdbc:sqlite:" + dbFile.getPath(), new Properties(), Class.forName("org.sqlite.JDBC", true, loader));

            uses = 0;
            return connection;
        } catch (ClassNotFoundException e) {
            BungeeJoinMessage.getInstance().getLogger().log(Level.SEVERE, "You are missing necessary libraries. If using SQLite, download it from https://bitbucket.org/xerial/sqlite-jdbc/downloads/sqlite-jdbc-3.8.11.2.jar and put it in the /plugins/BungeeJoinMessage/lib folder.");
        } catch (Exception e) {
            BungeeJoinMessage.getInstance().getLogger().log(Level.SEVERE, "Exception on SQL initialize", e);
        }
        return null;
    }

    private void load(String setupSql, String primary) {
        connection = getSQLConnection();

        try (Statement s = connection.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " " + setupSql + ",PRIMARY KEY (`" + primary + "`));");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
