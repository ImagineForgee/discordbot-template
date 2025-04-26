package com.example.discordbot.events.impl;

import com.example.discordbot.events.EventHandler;
import com.example.discordbot.annotations.EventListen;
import com.example.discordbot.managers.DatabaseManager;
import com.example.discordbot.models.Application;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.InteractionPresentModalSpec;
import org.mongojack.JacksonMongoCollection;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;

import java.util.List;

@EventListen(eventType = ButtonInteractionEvent.class)
public class ApplicationButtonEvent implements EventHandler<ButtonInteractionEvent> {

    @Override
    public Mono<Void> handle(ButtonInteractionEvent event) {
        String customId = event.getCustomId();
        String[] parts = customId.split(":", 2);
        if (parts.length < 2) return Mono.empty();

        String action = parts[0];
        String userIdStr = parts[1];

        JacksonMongoCollection<Application> col =
                DatabaseManager.getInstance().getCollection("applications", Application.class);

        if (action.equals("accept")) {
            Mono<Void> ack = event.deferEdit();

            return Mono.fromCallable(() -> col.findOne(Filters.eq("userId", userIdStr)))
                    .flatMap(app -> {
                        if (app == null) return Mono.empty();

                        col.updateOne(Filters.eq("userId", userIdStr), Updates.set("reviewed", true));

                        Mono<Void> dm = event.getClient()
                                .getUserById(Snowflake.of(userIdStr))
                                .flatMap(User::getPrivateChannel)
                                .flatMap(pc -> pc.createMessage("🎉 Congratulations! Your application has been **accepted**!"))
                                .then();


                        Mono<Void> deleteChannel = event.getClient()
                                .getChannelById(Snowflake.of(app.getChannelId()))
                                .flatMap(Channel::delete)
                                .onErrorResume(e -> Mono.empty());

                        return ack.then(dm).then(deleteChannel);
                    });
        }

        if (action.equals("decline")) {
            return event.presentModal(InteractionPresentModalSpec.builder()
                    .customId("decline-modal:" + userIdStr)
                    .title("Decline Reason")
                    .addAllComponents(List.of(
                            ActionRow.of(
                                    TextInput.paragraph("decline-reason", "Reason")
                                            .placeholder("Decline reason")
                                            .required(false)
                            )
                    ))
                    .build()
            );
        }

        return Mono.empty();
    }
}
