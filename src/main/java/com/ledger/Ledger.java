package com.ledger;

import com.ledger.api.HttpServer;
import com.ledger.api.database.LedgerDB;
import com.ledger.api.database.repositories.*;
import com.ledger.config.ConfigManager;
import com.ledger.plugin.commands.Commands;
import com.ledger.plugin.listeners.EconomyListener;
import com.ledger.plugin.schedulers.DataPurger;
import com.ledger.plugin.schedulers.PlayerBalanceUpdater;
import com.ledger.plugin.schedulers.ServerBalanceUpdater;
import com.ledger.utils.CustomLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Ledger extends JavaPlugin {
    private static CustomLogger customLogger;
    private static ConfigManager configManager;

    private HttpServer server = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        customLogger = new CustomLogger(getLogger(), configManager);

        // Database and repository initialization
        LedgerDB database;
        TransactionRepository transactionRepository;
        PlayerRepository playerRepository;
        PlayerBalanceRepository playerBalanceRepository;
        ServerBalanceRepository serverBalanceRepository;
        SchedulerRepository schedulerRepository;
        try {
            database = new LedgerDB(getDataFolder());
            transactionRepository = new TransactionRepository(database);
            playerRepository = new PlayerRepository(database);
            playerBalanceRepository = new PlayerBalanceRepository(database);
            serverBalanceRepository = new ServerBalanceRepository(database);
            schedulerRepository = new SchedulerRepository(database);
        } catch (Exception e) {
            customLogger.error("Failed to initialize the Ledger database.", e);
            return;
        }

        // Economy initialization
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        Economy economy = rsp.getProvider();

        // Event listener initialization
        getServer().getPluginManager().registerEvents(new EconomyListener(this, playerRepository, transactionRepository), this);

        // Scheduler initialization
        new ServerBalanceUpdater(this, playerRepository, serverBalanceRepository, schedulerRepository).startScheduler();
        new PlayerBalanceUpdater(this, playerBalanceRepository, schedulerRepository, transactionRepository).startScheduler();
        new DataPurger(this, playerBalanceRepository, serverBalanceRepository, transactionRepository, configManager).startScheduler();

        // HTTP Server initialization
        try {
            if (server == null) {
                server = new HttpServer(configManager, playerRepository, playerBalanceRepository, serverBalanceRepository, transactionRepository);
            }
            server.start();
        } catch (Exception e) {
            customLogger.error("Failed to start the Ledger HTTP server.", e);
            return;
        }

        // Command initialization
        getCommand("ledger").setExecutor(new Commands(this, playerRepository, configManager));
    }

    @Override
    public void onDisable() {
        if (server != null) {
            server.stop();
        }

        getServer().getScheduler().cancelTasks(this);
    }

    public static CustomLogger getCustomLogger() {
        return customLogger;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
