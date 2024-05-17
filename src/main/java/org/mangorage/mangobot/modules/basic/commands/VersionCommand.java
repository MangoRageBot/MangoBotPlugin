/*
 * Copyright (c) 2023. MangoRage
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.mangorage.mangobot.modules.basic.commands;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandResult;
import org.mangorage.mangobotapi.core.commands.IBasicCommand;
import org.mangorage.mangobotapi.core.data.DataHandler;
import org.mangorage.mangobotapi.core.data.IEmptyFileNameResolver;
import org.mangorage.mangobotapi.core.plugin.api.CorePlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public class VersionCommand implements IBasicCommand {
    public record Version(String version) implements IEmptyFileNameResolver {}

    private static final AtomicReference<Version> VERSION = new AtomicReference<>();
    private static final DataHandler<Version> VERSION_DATA_HANDLER = DataHandler.create()
            .path("version")
            .build(Version.class);

    public static void init() {

    }

    public static String findVersion(String key, String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2 && parts[0].trim().equals(key)) {
                    return parts[1].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Undefined Version"; // Key not found or file error occurred
    }

    public static String getVersion() {
        if (VERSION.get() == null)
            VERSION.set(new Version(findVersion("mangobotplugin.jar", "installer/installed.txt")));
        return VERSION.get().version();
    }

    private final CorePlugin corePlugin;

    public VersionCommand(CorePlugin corePlugin) {
        this.corePlugin = corePlugin;
    }

    @NotNull
    @Override
    public CommandResult execute(Message message, Arguments args) {
        var settings = corePlugin.getMessageSettings();
        settings.apply(message.reply("Bot is running on Version: " + VERSION.get().version())).queue();
        return CommandResult.PASS;
    }

    /**
     * @return
     */
    @Override
    public String commandId() {
        return "version";
    }

    /**
     * @return
     */
    @Override
    public String description() {
        return "Tells you what version the bot is running on.";
    }
}
