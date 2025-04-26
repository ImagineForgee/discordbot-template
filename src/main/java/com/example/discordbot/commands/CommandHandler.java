package com.example.discordbot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface CommandHandler {
    Mono<Void> execute(ChatInputInteractionEvent event);
}
