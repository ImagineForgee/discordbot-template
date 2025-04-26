package com.example.discordbot;

import com.example.discordbot.managers.CommandManager;
import com.example.discordbot.managers.ConfigManager;
import com.example.discordbot.managers.DatabaseManager;
import com.example.discordbot.managers.EventManager;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.Permission;

import java.util.*;

public class Bot {
    private static Bot instance = null;

    public static Bot getInstance() {
        return instance;
    }

    private final GatewayDiscordClient client;
    private final EventManager eventManager;
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    private final ConfigManager config;

    public ConfigManager getConfig() {
        return config;
    }

    public Bot() {
        instance = this;

        long start = System.currentTimeMillis();
        config = ConfigManager.getInstance();
        String token = config.getBoolean("bot.isDevEnv")
                ? config.getString("bot.developmentToken")
                : config.getString("bot.productionToken");

        DiscordClient discordClient = DiscordClient.create(token);
        this.client = discordClient.login().block();

        eventManager = new EventManager();

        CommandManager.registerGroupPermission("test", List.of(Permission.CONNECT));
        commandManager = new CommandManager(client);

        eventManager.initialize(client);
        client.on(ChatInputInteractionEvent.class, commandManager::executeCommand).subscribe();
        databaseManager = DatabaseManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(databaseManager::shutdown));

        long end = System.currentTimeMillis();
        Constants.LOG.info("Bot loaded in {}ms", (end - start));
    }

    public void start() {
        client.onDisconnect().block();
    }

    public static void main(String[] args) {
        new Bot().start();
    }
}
