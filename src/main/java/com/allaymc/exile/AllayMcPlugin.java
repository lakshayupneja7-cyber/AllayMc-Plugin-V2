package com.allaymc.exile;

import com.allaymc.exile.command.CaseAdminCommand;
import com.allaymc.exile.command.ExileCommand;
import com.allaymc.exile.command.ServerBorderCommand;
import com.allaymc.exile.data.*;
import com.allaymc.exile.discord.DiscordWebhookService;
import com.allaymc.exile.gui.RecoveryGuiListener;
import com.allaymc.exile.listener.*;
import com.allaymc.exile.service.*;
import com.allaymc.exile.util.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AllayMcPlugin extends JavaPlugin {
    private static AllayMcPlugin instance;

    private PlayerDataManager playerDataManager;
    private CaseDataManager caseDataManager;
    private BorderStateManager borderStateManager;
    private MessageUtil messageUtil;
    private DiscordWebhookService discordWebhookService;
    private BorderService borderService;
    private NetherBorderService netherBorderService;
    private ExileService exileService;
    private PunishmentService punishmentService;
    private ValidationService validationService;
    private ExileCaseService exileCaseService;
    private RecoveryService recoveryService;
    private SchedulerService schedulerService;

    public static AllayMcPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResourceIfNotExists("messages.yml");
        saveResourceIfNotExists("gui.yml");
        saveResourceIfNotExists("discord.yml");
        saveResourceIfNotExists("borders.yml");

        messageUtil = new MessageUtil(this);
        playerDataManager = new PlayerDataManager(this);
        caseDataManager = new CaseDataManager(this);
        borderStateManager = new BorderStateManager(this);
        discordWebhookService = new DiscordWebhookService(this);
        borderService = new BorderService(this, borderStateManager);
        netherBorderService = new NetherBorderService(this, borderStateManager);
        exileService = new ExileService(this, playerDataManager);
        punishmentService = new PunishmentService(this);
        validationService = new ValidationService();
        exileCaseService = new ExileCaseService(this, caseDataManager, playerDataManager);
        recoveryService = new RecoveryService(this, exileCaseService, validationService, punishmentService, exileService);
        schedulerService = new SchedulerService(this, caseDataManager, exileCaseService, recoveryService);

        registerCommands();
        registerListeners();

        exileService.disableVanillaWorldBorder();
        exileService.startTimerTask();
        exileService.startDangerZoneTask();
        schedulerService.startRecoveryWatcher();

        getLogger().info("AllayMc V2 enabled.");
    }

    @Override
    public void onDisable() {
        if (exileService != null) exileService.saveAllOnlineExileStates();
        if (playerDataManager != null) playerDataManager.saveAll();
        if (caseDataManager != null) caseDataManager.saveAll();
        getLogger().info("AllayMc V2 disabled.");
    }

    private void registerCommands() {
        ExileCommand exileCommand = new ExileCommand(this);
        ServerBorderCommand serverBorderCommand = new ServerBorderCommand(this);
        CaseAdminCommand caseAdminCommand = new CaseAdminCommand(this);

        bind("exile", exileCommand);
        bind("exileadd", exileCommand);
        bind("exilefree", exileCommand);
        bind("exileextend", exileCommand);
        bind("exileremove", exileCommand);
        bind("exilecount", exileCommand);
        bind("serverborder", serverBorderCommand);
        bind("caseadmin", caseAdminCommand);
    }

    private void bind(String name, Object executor) {
        PluginCommand cmd = getCommand(name);
        if (cmd == null) return;
        if (executor instanceof org.bukkit.command.CommandExecutor ce) cmd.setExecutor(ce);
        if (executor instanceof org.bukkit.command.TabCompleter tc) cmd.setTabCompleter(tc);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(this, exileService, playerDataManager), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this, exileService), this);
        getServer().getPluginManager().registerEvents(new BorderEnforcementListener(borderService, netherBorderService, exileService), this);
        getServer().getPluginManager().registerEvents(new RecoveryProtectionListener(playerDataManager, caseDataManager), this);
        getServer().getPluginManager().registerEvents(new RecoveryGuiListener(this, recoveryService, exileCaseService), this);
        getServer().getPluginManager().registerEvents(new NetherBorderListener(), this);
        getServer().getPluginManager().registerEvents(new ExileCaseListener(), this);
    }

    private void saveResourceIfNotExists(String path) {
        if (!new File(getDataFolder(), path).exists()) {
            saveResource(path, false);
        }
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public CaseDataManager getCaseDataManager() {
        return caseDataManager;
    }

    public BorderStateManager getBorderStateManager() {
        return borderStateManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public DiscordWebhookService getDiscordWebhookService() {
        return discordWebhookService;
    }

    public BorderService getBorderService() {
        return borderService;
    }

    public NetherBorderService getNetherBorderService() {
        return netherBorderService;
    }

    public ExileService getExileService() {
        return exileService;
    }

    public PunishmentService getPunishmentService() {
        return punishmentService;
    }

    public ValidationService getValidationService() {
        return validationService;
    }

    public ExileCaseService getExileCaseService() {
        return exileCaseService;
    }

    public RecoveryService getRecoveryService() {
        return recoveryService;
    }
}
