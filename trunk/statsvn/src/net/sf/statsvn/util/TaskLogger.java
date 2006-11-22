package net.sf.statsvn.util;

/**
 * An Interface for the Logging mechanism.
 * @author Benoit Xhenseval
 */
public interface TaskLogger {
    /**
     * Generic interface for logging issue & debug info.
     * @param arg the string to log.
     */
    void log(String arg);
}
