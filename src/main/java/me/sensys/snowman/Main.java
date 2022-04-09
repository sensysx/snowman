// this is just a concept for right now actual work can be done later

package me.sensys.snowman;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import net.dv8tion.jda.api.JDABuilder;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;

public final class Main extends JavaPlugin {

    private JDA jda;
    private TextChannel wakeChannel;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // grabs bot token from config and logs into the bot
        String botToken = getConfig().getString("bot-token");
        try {
            jda = JDABuilder.createDefault(botToken)
                    .build()
                    .awaitReady();
        } catch (InterruptedException | LoginException e) {
            e.printStackTrace();

        }

        // just sends a message when its online
        String statusChannelId = getConfig().getString("wake-channel");
        if (statusChannelId != null) {
            wakeChannel = jda.getTextChannelById(statusChannelId);
        }

        if (wakeChannel != null) {
            wakeChannel.sendMessage("The Snowhaze Whitelist Bot Is Online").queue();
        }

        // recognizes that there is a listener that needs to be on
        jda.addEventListener(new WhitelistListener());
    }

    @Override
    public void onDisable() {

        // offline message
        String statusChannelId = getConfig().getString("wake-channel");
        if (statusChannelId != null) {
            wakeChannel = jda.getTextChannelById(statusChannelId);
        }

        if (wakeChannel != null) {
            wakeChannel.sendMessage("The Snowhaze Whitelist Bot Is Offline").queue();
        }

        // log out of the bot
        if (jda != null) jda.shutdownNow();
    }

    // converts minecraft username to uuid
    public String getUuid(String name) {
        String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
        try {
            @SuppressWarnings("deprecation")
            String UUIDJson = IOUtils.toString(new URL(url));
            if (UUIDJson.isEmpty()) return "invalid name";
            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);
            return UUIDObject.get("id").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return "error";
    }

    //  sets prefix var
    String whitelistcommand = "whitelist/";

    // listener for the whitelist command and whitelists
    public final class WhitelistListener extends ListenerAdapter {
        @Override
        // on message that has whitelist/ infront of iut goes through
        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            if (event.getMessage().getContentDisplay().startsWith("whitelist/")) {

                // transfers uuid string into object
                String username = (event.getMessage().getContentDisplay().replace(whitelistcommand, ""));
                String Uuid = getUuid(username);
                final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(java.util.UUID.fromString(Uuid.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5")).toString()));

                // whitelists
                player.setWhitelisted(true);

                // sends message when you are whitelisted
                event.getMessage().reply("The player" + username + "has been whitelisted more info on how this works check out github.com/sensysx/snowman");

                // makes sure you can't whitelist more than one account
                String msgAuthor = event.getAuthor().toString();
                getConfig().getList("whitelisted").add(msgAuthor);
            }

        }
    }
}


