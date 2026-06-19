package com.ledger.config;

import com.ledger.Ledger;

/**
 * Owns the plugin configuration. Values fall into two groups:
 *
 * <ul>
 *   <li><b>Initialization-only</b> values ({@code server-url}, {@code schema},
 *   {@code port}) are read once in the constructor into {@code final} fields.
 *   They are consumed when the HTTP server is created and baked into the served
 *   front-end, so they cannot change without a restart and must NOT be re-read
 *   on reload.</li>
 *   <li><b>Reloadable</b> values ({@code debug}, retention days) are read in
 *   {@link #loadConfig()} and refreshed by {@link #reloadConfig()}, so changes
 *   take effect at runtime.</li>
 * </ul>
 */
public final class ConfigManager {
    private final Ledger plugin;

    // Initialization-only: set once, consumed when the HTTP server starts.
    private final String serverUrl;
    private final String schema;
    private final int port;

    // Reloadable: refreshed on every loadConfig()/reloadConfig().
    private boolean debug;
    private int transactionLogRetentionDays;
    private int playerBalanceHistoryRetentionDays;
    private int serverBalanceHistoryRetentionDays;

    public ConfigManager(Ledger plugin) {
        this.plugin = plugin;
        this.serverUrl = plugin.getConfig().getString("server-url");
        this.schema = plugin.getConfig().getString("schema");
        this.port = plugin.getConfig().getInt("port");
        loadConfig();
    }

    public void loadConfig() {
        this.debug = plugin.getConfig().getBoolean("debug");
        this.transactionLogRetentionDays = plugin.getConfig().getInt("transaction-log-retention-days");
        this.playerBalanceHistoryRetentionDays = plugin.getConfig().getInt("player-balance-history-retention-days");
        this.serverBalanceHistoryRetentionDays = plugin.getConfig().getInt("server-balance-history-retention-days");
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        loadConfig();
    }

    public boolean isDebug() {
        return this.debug;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public String getSchema() {
        return this.schema;
    }

    public int getPort() {
        return this.port;
    }

    public int getTransactionLogRetentionDays() {
        return this.transactionLogRetentionDays;
    }

    public int getPlayerBalanceHistoryRetentionDays() {
        return this.playerBalanceHistoryRetentionDays;
    }

    public int getServerBalanceHistoryRetentionDays() {
        return this.serverBalanceHistoryRetentionDays;
    }
}
