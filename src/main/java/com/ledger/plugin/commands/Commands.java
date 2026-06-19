package com.ledger.plugin.commands;

import com.ledger.Ledger;
import com.ledger.api.database.repositories.PlayerRepository;
import com.ledger.api.services.SessionService;
import com.ledger.config.ConfigManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {
    private final Plugin plugin;
    private final PlayerRepository playerRepository;
    private final ConfigManager configManager;

    public Commands(Plugin plugin, PlayerRepository playerRepository, ConfigManager configManager) {
        this.plugin = plugin;
        this.playerRepository = playerRepository;
        this.configManager = configManager;
    }

    private static final ArrayList<String> PERMISSIONS = new ArrayList<>() {
        {
            add("ledger.balances.view");
            add("ledger.server-chart.view");
            add("ledger.transactions.view-own");
            add("ledger.player-charts.view-own");
            add("ledger.transactions.view-all");
            add("ledger.player-charts.view-all");
        }
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ledger.reload")) {
                sender.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            configManager.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "[Ledger]: Configuration reloaded.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            // Command sent from console
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                String nessId = playerRepository.queryForUuid("NessXXIII");
                if (nessId == null) {
                    OfflinePlayer ness = Bukkit.getOfflinePlayerIfCached("NessXXIII");
                    if (ness == null) {
                        Ledger.getCustomLogger().warning("Could not get Ness UUID. Try logging into the server for awhile.");
                        return;
                    } else {
                        nessId = ness.getUniqueId().toString();
                    }
                }

                String authId = SessionService.createAuthorization(nessId, "NessXXIII", PERMISSIONS);
                String host = configManager.getServerUrl();
                String url = "http://" + host + "/sessions/" + authId;
                Ledger.getCustomLogger().info("[Ledger Login URL]: " + url);
            });
            return false;
        }

        List<String> permissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (player.hasPermission(permission)) {
                permissions.add(permission);
            }
        }

        if (permissions.size() == 0) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        String host = configManager.getServerUrl();
        String authId = SessionService.createAuthorization(player.getUniqueId().toString(), player.getName(), permissions);
        String url = "http://" + host + "/sessions/" + authId;

        TextComponent message = new TextComponent(ChatColor.GREEN + "[Ledger]: ");

        TextComponent clickHere = new TextComponent(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "click here");
        clickHere.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        clickHere.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(url).create()));

        message.addExtra(clickHere);
        message.addExtra(ChatColor.GREEN + " to login.");

        player.sendMessage(message);
        return true;
    }

}
