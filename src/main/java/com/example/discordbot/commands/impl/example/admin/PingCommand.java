package com.example.discordbot.commands.impl.example.admin;

import com.example.discordbot.commands.CommandHandler;
import com.github.clawsoftsolutions.purrfectlib.annotations.Command;
import com.github.clawsoftsolutions.purrfectlib.annotations.CommandOption;
import com.github.clawsoftsolutions.purrfectlib.enums.OptionType;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

@Command(name = "ping", description = "Ping a domain or IP address.", options = {
        @CommandOption(name = "id", description = "Domain or IP to ping", type = OptionType.STRING, required = true)
})
public class PingCommand implements CommandHandler {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        Optional<String> idOpt = event.getOption("id")
                .flatMap(opt -> opt.getValue())
                .map(value -> value.asString());

        if (idOpt.isEmpty()) {
            return event.reply("Missing domain or IP address!").withEphemeral(true);
        }

        String id = idOpt.get();

        return Mono.fromCallable(() -> {
            try {
                InetAddress inet = InetAddress.getByName(id);
                boolean reachable = inet.isReachable(3000);
                return reachable ? "✅ " + id + " is reachable!" : "❌ " + id + " is NOT reachable.";
            } catch (IOException e) {
                return "⚠️ Failed to ping " + id + ": " + e.getMessage();
            }
        }).flatMap(result -> event.reply(result));
    }
}
