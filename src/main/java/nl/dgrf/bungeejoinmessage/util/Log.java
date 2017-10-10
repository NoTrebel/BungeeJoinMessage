package nl.dgrf.bungeejoinmessage.util;

import nl.dgrf.bungeejoinmessage.BungeeJoinMessage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    private static final Logger LOG = BungeeJoinMessage.getInstance().getLogger();
    private static final String PREFIX = "[BungeeJoinMessage] ";
    
    public static void info(String output) {
        LOG.log(Level.INFO,PREFIX + "{0}", output);
    }
    
    public static void severe(String output) {
        LOG.log(Level.SEVERE,PREFIX + "{0}", output);
    }
    
    public static void warning(String output) {
        LOG.log(Level.WARNING,PREFIX + "{0}", output);
    }
}
