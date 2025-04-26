package com.example.discordbot.events.impl;

import com.example.discordbot.events.EventHandler;
import com.example.discordbot.annotations.EventListen;
import com.example.discordbot.managers.DatabaseManager;
import com.example.discordbot.models.Application;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.User;
import discord4j.core.spec.*;
import org.mongojack.JacksonMongoCollection;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@EventListen(eventType = ModalSubmitInteractionEvent.class)
public class ApplicationFormEvent implements EventHandler<ModalSubmitInteractionEvent> {

    @Override
    public Mono<Void> handle(ModalSubmitInteractionEvent event) {
        if (!event.getCustomId().equals("application-form")) {
            return Mono.empty();
        }

        String name = "<NO NAME>";
        String skills = "<NO SKILL>";
        String langs = "<NO LANGS>";
        String showcase = "<NO SHOWCASE>";
        String info = "<NO INFO>";

        for (TextInput component : event.getComponents(TextInput.class)) {
            switch (component.getCustomId()) {
                case "name" -> name = component.getValue().orElse("<NO NAME>");
                case "skills" -> skills = component.getValue().orElse("<NO SKILL>");
                case "langs" -> langs = component.getValue().orElse("<NO LANGS>");
                case "showcase" -> showcase = component.getValue().orElse("<NO SHOWCASE>");
                case "info" -> info = component.getValue().orElse("<NO INFO>");
            }
        }

        Boolean reviewed = false;
        String applicantId = event.getInteraction().getUser().getId().asString();

        Application app = new Application();
        app.setUserId(applicantId);
        app.setName(name);
        app.setSkills(skills);
        app.setLanguages(langs);
        app.setShowcase(showcase);
        app.setInfo(info);
        app.setReviewed(reviewed);

        JacksonMongoCollection<Application> col =
                DatabaseManager.getInstance().getCollection("applications", Application.class);
        col.insert(app);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .addAllFields(Arrays.asList(
                        EmbedCreateFields.Field.of("Name", name, false),
                        EmbedCreateFields.Field.of("Skills", skills, false),
                        EmbedCreateFields.Field.of("Languages", langs, false),
                        EmbedCreateFields.Field.of("Showcase", showcase, false),
                        EmbedCreateFields.Field.of("Additional Information", info, false)
                ))
                .build();

        User user = event.getInteraction().getUser();

        return event.getInteraction().getGuild()
                .flatMap(guild -> guild.getChannels()
                        .filter(channel -> channel.getType().name().equals("GUILD_CATEGORY"))
                        .filter(channel -> channel.getName().equalsIgnoreCase("applications"))
                        .singleOrEmpty()
                        .switchIfEmpty(
                                guild.createCategory("applications")
                        )
                        .flatMap(category -> guild.createTextChannel(TextChannelCreateSpec.builder()
                                .name(user.getUsername())
                                .parentId(category.getId())
                                .reason("New applicant")
                                .topic("Channel to debate over " + user.getUsername() + "'s application")
                                .build()))
                )
                .flatMap(c -> {
                    app.setChannelId(c.getId().asString());
                    col.updateOne(Filters.eq("userId", applicantId), Updates.set("channelId", app.getChannelId()));

                    return c.createMessage(MessageCreateSpec.builder()
                            .addEmbed(embed)
                            .content("<@" + user.getId().asString() + ">")
                            .addComponent(ActionRow.of(
                                    Button.success("accept:" + applicantId, "Accept"),
                                    Button.danger("decline:" + applicantId, "Decline")
                            ))
                            .build());
                })
                .then(event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .content("Successfully sent your application!")
                        .ephemeral(true)
                        .build()));
    }
}
