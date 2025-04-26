package com.example.discordbot.commands.impl.general;

import com.example.discordbot.commands.CommandHandler;
import com.example.discordbot.managers.DatabaseManager;
import com.example.discordbot.models.Application;
import com.github.clawsoftsolutions.purrfectlib.annotations.Command;
import com.mongodb.client.model.Filters;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.TextInput;
import discord4j.core.spec.InteractionPresentModalSpec;
import org.mongojack.JacksonMongoCollection;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Command(name = "apply", description = "Apply")
public class ApplyCommand implements CommandHandler {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        String userId = event.getInteraction().getUser().getId().asString();
        JacksonMongoCollection<Application> col =
                DatabaseManager.getInstance().getCollection("applications", Application.class);

        Application existing = col.findOne(Filters.eq("userId", userId));

        if (existing != null && !existing.isReviewed()) {
            return event.reply("Your application is still being reviewed.").withEphemeral(true);
        }

        return event.presentModal(InteractionPresentModalSpec.builder()
                    .title("Application")
                    .customId("application-form")
                    .addAllComponents(Arrays.asList(
                            ActionRow.of(
                                    TextInput.small("name", "Name")
                                            .placeholder("First and/or last name")
                                            .required(true)
                            ),
                            ActionRow.of(
                                    TextInput.paragraph("skills", "Skills")
                                            .placeholder("Programming, UI/UX, writing/docs, community")
                                            .required(true)
                            ),
                            ActionRow.of(
                                    TextInput.small("langs", "Languages")
                                            .placeholder("E.g. C, C++, Java")
                                            .required(false)
                            ),
                            ActionRow.of(
                                    TextInput.paragraph("showcase", "Showcase")
                                            .placeholder("GitHub URL, portfolio link, art, etc.")
                                            .required(true)
                            ),
                            ActionRow.of(
                                    TextInput.paragraph("info", "Extra Info")
                                            .placeholder("Anything else we should know")
                                            .required(false)
                            )
                    ))
                    .build());
    }
}
