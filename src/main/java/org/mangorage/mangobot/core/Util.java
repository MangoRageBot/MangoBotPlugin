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

package org.mangorage.mangobot.core;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import org.mangorage.basicutils.LogHelper;
import org.mangorage.mangobot.loader.CoreMain;
import org.mangorage.mangobot.modules.logs.LogAnalyser;
import org.mangorage.mangobot.modules.logs.LogAnalyserModule;
import org.mangorage.mangobotapi.core.commands.Arguments;
import org.mangorage.mangobotapi.core.commands.CommandPrefix;
import org.mangorage.mangobotapi.core.events.BasicCommandEvent;
import org.mangorage.mangobotapi.core.plugin.extra.JDAPlugin;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.TimeUtil;

public class Util {

    private static final boolean verbose = false;

    public static TemporalAccessor getTimestamp(ISnowflake iSnowflake) {
        return TimeUtil.getTimeCreated(iSnowflake);
    }


    public record MessageType(boolean cmd, boolean silent) {}

    public static MessageType handleMessage(JDAPlugin plugin, MessageReceivedEvent event) {
        // Handle Message and prefix
        String cmdPrefix = event.isFromGuild() ? CommandPrefix.getPrefix(event.getGuild().getId()) : plugin.getCommandPrefix();
        if (CoreMain.isDevMode()) // Special clause for dev mode!
            cmdPrefix = "dev" + cmdPrefix;

        Message message = event.getMessage();
        String rawMessage = message.getContentRaw();

        boolean silent = rawMessage.startsWith("s");
        if (silent)
            rawMessage = rawMessage.replaceFirst("s", "");

        if (rawMessage.length() > 1 && rawMessage.startsWith(cmdPrefix)) {
            if (event.getAuthor().isBot()) return new MessageType(false, false);
            String[] command_pre = rawMessage.split(" ");
            String command = command_pre[0].substring(cmdPrefix.length());
            Arguments arguments = Arguments.of(Arguments.of(command_pre).getFrom(1).split(" "));

            var commandEvent = new BasicCommandEvent(event.getMessage(), command, arguments);
            plugin.getCommandRegistry().postBasicCommand(commandEvent);

            // Now post to anything using our EventBus...
            if (!commandEvent.isHandled())
                plugin.getPluginBus().post(commandEvent);

            if (commandEvent.getException() != null) {
                plugin.getMessageSettings().apply(message.reply("""
                        An Exception occurred while executing the command.
                        %s
                        """.formatted(commandEvent.getException().getMessage()))).queue();
            } else if (commandEvent.isHandled()) {
                commandEvent.getCommandResult().accept(message);
            } else if (verbose) {
                plugin.getMessageSettings().apply(message.reply("Invalid Command")).queue();
            }

            return new MessageType(true, silent);
        }

        return new MessageType(false, false);
    }

    public static Integer parseStringIntoInteger(String s) {
        Integer res = null;
        try {
            res = Integer.parseInt(s);
        } catch (NumberFormatException ignored) {
        }
        return res;
    }

    public static boolean copy(Path from, BasicFileAttributes a, Path target) {
        LogHelper.info("Copy " + (a.isDirectory() ? "DIR " : "FILE") + " => " + target);
        try {
            if (a.isDirectory())
                Files.createDirectories(target);
            else if (a.isRegularFile())
                Files.copy(from, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }
    
    //Copied from FCIGenUtils.java
    public static InputStream getFileInputStream(String fileUrl) {  
        try {  
            URL url = new URL(fileUrl);  
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
            connection.setRequestMethod("GET");  
  
            int responseCode = connection.getResponseCode();  
            if (responseCode == HttpURLConnection.HTTP_OK) {  
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());  
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();  
                byte[] buffer = new byte[1024];  
                int bytesRead;  
                while ((bytesRead = inputStream.read(buffer)) != -1) {  
                    byteArrayOutputStream.write(buffer, 0, bytesRead);  
                }  
                byte[] byteArray = byteArrayOutputStream.toByteArray();  
                  
                byteArrayOutputStream.close();  
                inputStream.close();  
                  
                return new ByteArrayInputStream(byteArray);  
            } else {  
                throw new IOException("Server response code: " + responseCode);  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
            return null;
        }  
    }
    
  //Copied from FCIGenUtils.java
    public static String getStringFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String nl = System.getProperty("line.separator");
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line+nl);
        }
        return stringBuilder.toString();
    }
    
    
    
    
}
