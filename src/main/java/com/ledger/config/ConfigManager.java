package com.ledger.config;

import com.ledger.Ledger;

/**
 * Owns the plugin configuration. Values are read from config.yml into typed
 * fields once on load and refreshed on {@link #reloadConfig()}, so consumers
 * read cached values through typed getters rather than hitting the raw
 * configuration on every access. Threaded into the components that need it.
 */
public final class ConfigManager {
    private final Ledger plugin;

    private boolean debug;
    private String serverUrl;
    private String schema;
    private int port;
    private int transactionLogRetentionDays;
    private int playerBalanceHistoryRetentionDays;
    private int serverBalanceHistoryRetentionDays;

    public ConfigManager(Ledger plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.debug = plugin.getConfig().getBoolean("debug");
        this.serverUrl = plugin.getConfig().getString("server-url");
        this.schema = plugin.getConfig().getString("schema");
        this.port = plugin.getConfig().getInt("port");
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
