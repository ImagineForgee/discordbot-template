package com.example.discordbot.commands;

import discord4j.rest.util.Permission;

import java.util.List;

public record CommandGroup(String name, List<Permission> permissions) {
}

