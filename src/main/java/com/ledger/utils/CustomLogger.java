package com.ledger.utils;

import com.ledger.Ledger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wraps the plugin's {@link Logger} so the console stays quiet during normal
 * operation. Routine, high-volume messages ({@link #info} and {@link #debug})
 * are only printed when {@code debug: true} is set in the configuration.
 * Warnings and errors are always printed regardless of the debug flag.
 */
public class CustomLogger {
    private final Logger logger;

    public CustomLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * @return whether verbose logging is enabled via the {@code debug} flag in
     * config.yml. Defaults to {@code false} if the configuration is unavailable.
     */
    private boolean isDebugEnabled() {
        try {
            return Ledger.getConfiguration().getBoolean("debug", false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Logs an informational message. Only printed when debug mode is enabled.
     */
    public void info(String message) {
        if (isDebugEnabled()) {
            logger.info(message);
        }
    }

    /**
     * Logs a verbose debug message. Only printed when debug mode is enabled.
     */
    public void debug(String message) {
        if (isDebugEnabled()) {
            logger.info("[DEBUG] " + message);
        }
    }

    /**
     * Logs a warning. Always printed.
     */
    public void warning(String message) {
        logger.warning(message);
    }

    /**
     * Logs an error. Always printed.
     */
    public void error(String message) {
        logger.severe(message);
    }

    /**
     * Logs an error along with the throwable that caused it. Always printed.
     */
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }
}
