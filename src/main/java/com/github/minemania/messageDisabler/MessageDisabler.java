package com.github.minemania.messageDisabler;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Plugin(id = "messagedisabler", name = "MessageDisabler", version = "1.0", authors = {"Computerwhz"})
public class MessageDisabler {

    private static MessageDisabler instance;

    private final  ProxyServer proxyServer;
    private final Path dataDirectory;
    private final Logger logger;

    private YamlDocument config;

    @Inject
    public MessageDisabler(ProxyServer proxyServer, @DataDirectory final Path dataDirectory, Logger logger){
        instance = this;
        this.proxyServer = proxyServer;
        this.dataDirectory = dataDirectory;
        this.logger = logger;

        try {
            this.config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
                    getClass().getResourceAsStream("config.yml"),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version"))
                            .setOptionSorting(UpdaterSettings.DEFAULT_OPTION_SORTING)
                            .build());
        }
        catch (IOException e){

        }

    }

    @Subscribe
    public void OnCommandRun(CommandExecuteEvent event){
        if (event.getCommandSource() instanceof Player) {

            String fullCommand = event.getCommand();
            Player sendingPlayer = (Player) event.getCommandSource();

            String[] parts = fullCommand.split(" ", 3);
            // limit=3 ensures we only split into:
            // [0] = "msg"
            // [1] = "Steve"
            // [2] = "hello there"

            String baseCommand = parts[0];

            Player receivedPlayer = null;
            if (parts.length > 1) {
                Optional<Player> targetOpt = getProxyServer().getPlayer(parts[1]);
                if (targetOpt.isPresent()) {
                    receivedPlayer = targetOpt.get();
                } else {
                    return; // stop if player not online
                }
            }

            String message = parts.length > 2 ? parts[2] : null;

            if (sendingPlayer.hasPermission("messagedisabler.disable")){
                if (messageCommands(baseCommand)) {
                    event.setResult(CommandExecuteEvent.CommandResult.denied());
                    logger.atDebug().log("Message command executed");
                    sendingPlayer.sendMessage(Component.text("You are Not Allowed to use private messaging for safeguarding reasons"));
                    for (Player p : getProxyServer().getAllPlayers()) {
                        if (p.hasPermission("messagedisabler.notify")) {
                            p.sendMessage(Component.text("Player " + sendingPlayer.getUsername() + " Attempted to send " + receivedPlayer.getUsername() + "a private message but is not allowed to\n" + message));
                        }
                    }
                }
            }
        }

    }

    private boolean messageCommands(String command){
        List<String> commands = getConfig().getStringList("message-commands");
        if (commands.contains(command)){
            return true;
        }
        else { return false; }

    }

    public Logger getLogger() {
        return this.logger;
    }

    public ProxyServer getProxyServer(){
        return this.proxyServer;
    }

    public YamlDocument getConfig() {
        return this.config;
    }

    public static MessageDisabler getInstance(){
        return instance;
    }
}
