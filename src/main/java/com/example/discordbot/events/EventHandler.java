package com.example.discordbot.events;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface EventHandler<T extends Event> {
    Mono<Void> handle(T event);
}
