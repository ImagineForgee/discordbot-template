package com.example.discordbot.events.impl;

import com.example.discordbot.events.EventHandler;
import com.example.discordbot.annotations.EventListen;
import com.example.discordbot.managers.DatabaseManager;
import com.example.discordbot.models.Application;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import org.mongojack.JacksonMongoCollection;
import reactor.core.publisher.Mono;

@EventListen(eventType = ModalSubmitInteractionEvent.class)
public class DeclineReasonModalEvent implements EventHandler<ModalSubmitInteractionEvent> {

    @Override
    public Mono<Void> handle(ModalSubmitInteractionEvent event) {
        if (!event.getCustomId().startsWith("decline-modal:")) {
            return Mono.empty();
        }

        String userId = event.getCustomId().split(":", 2)[1];
        String reason = event.getComponents(TextInput.class).stream()
                .filter(input -> input.getCustomId().equals("decline-reason"))
                .findFirst()
                .flatMap(TextInput::getValue)
                .orElse("No reason provided.");

        JacksonMongoCollection<Application> col =
                DatabaseManager.getInstance().getCollection("applications", Application.class);

        return Mono.fromCallable(() -> col.findOne(Filters.eq("userId", userId)))
                .flatMap(app -> {
                    if (app == null) return Mono.empty();

                    col.updateOne(Filters.eq("userId", userId), Updates.set("reviewed", true));

                    Mono<Void> dm = event.getClient()
                            .getUserById(Snowflake.of(userId))
                            .flatMap(User::getPrivateChannel)
                            .flatMap(pc -> pc.createMessage("😢 Your application has been **declined** for the following reason:\n\n> " + reason))
                            .then();

                    Mono<Void> deleteChannel = event.getClient()
                            .getChannelById(Snowflake.of(app.getChannelId()))
                            .flatMap(Channel::delete)
                            .onErrorResume(e -> Mono.empty());

                    return event.reply("✅ Decline reason submitted. The user has been notified and the application channel deleted.")
                            .withEphemeral(true)
                            .then(dm)
                            .then(deleteChannel);
                });
    }
}
