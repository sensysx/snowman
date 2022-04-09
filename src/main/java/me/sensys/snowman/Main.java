// this is just a concept for right now actual work can be done later

package me.sensys.snowman;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.plugin.java.JavaPlugin;
import net.dv8tion.jda.api.JDABuilder;

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

        String statusChannelId = getConfig().getString("wake-channel");
        if (statusChannelId != null) {
            wakeChannel = jda.getTextChannelById(statusChannelId);
        }

        if (wakeChannel != null) {
            wakeChannel.sendMessage("The Server Is Turning On").queue();
        }
    }

    @Override
    public void onDisable() {
        if (jda != null) jda.shutdownNow();
    }
}
