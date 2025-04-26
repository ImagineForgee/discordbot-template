package com.example.discordbot.managers;

import com.example.discordbot.Constants;
import com.example.discordbot.events.EventHandler;
import com.example.discordbot.annotations.EventListen;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.reflections.Reflections;
import reactor.core.publisher.Mono;

import java.util.*;

public class EventManager {
    private final Map<Class<?>, List<EventHandler<?>>> handlers = new HashMap<>();

    public void initialize(GatewayDiscordClient client) {
        Reflections reflections = new Reflections("com.github.clawsoftsolutions.discordbot.events.impl");
        Set<Class<?>> eventClasses = reflections.getTypesAnnotatedWith(EventListen.class);

        for (Class<?> clazz : eventClasses) {
            try {
                EventListen annotation = clazz.getAnnotation(EventListen.class);
                EventHandler<?> handlerInstance = (EventHandler<?>) clazz.getDeclaredConstructor().newInstance();
                handlers.computeIfAbsent(annotation.eventType(), k -> new ArrayList<>()).add(handlerInstance);
            } catch (Exception e) {
                Constants.LOG.error("Failed to register event listener: " + clazz.getName(), e);
            }
        }

        Constants.LOG.info("Registered event listeners: [{}]",
                String.join(", ", eventClasses.stream().map(Class::getSimpleName).toList()));

        handlers.forEach((type, handlerList) -> {
            client.on((Class<? extends Event>) type).flatMap(event -> handleEvent(event, handlerList)).subscribe();
        });
    }

    private <T extends Event> Mono<Void> handleEvent(T event, List<EventHandler<?>> handlerList) {
        List<Mono<Void>> executions = new ArrayList<>();
        for (EventHandler<?> handler : handlerList) {
            @SuppressWarnings("unchecked")
            EventHandler<T> typedHandler = (EventHandler<T>) handler;
            executions.add(typedHandler.handle(event));
        }
        return Mono.when(executions);
    }
}
