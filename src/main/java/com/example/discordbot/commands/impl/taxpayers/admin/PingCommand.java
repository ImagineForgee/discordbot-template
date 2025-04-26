package com.example.discordbot.commands.impl.taxpayers.admin;

import com.example.discordbot.commands.CommandHandler;
import com.github.clawsoftsolutions.purrfectlib.annotations.Command;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

@Command(name = "ping", description = "Replies with Pong!")
public class PingCommand implements CommandHandler {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.reply("Pong!");
    }
}
