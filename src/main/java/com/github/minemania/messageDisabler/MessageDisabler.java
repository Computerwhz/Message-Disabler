package com.github.minemania.messageDisabler;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
                    Objects.requireNonNull(getClass().getResourceAsStream("config.yml")),
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
        String fullCommand = event.getCommand();
        CommandSource source = event.getCommandSource();

        String[] parts = fullCommand.split(" ");
        String baseCommand = parts[0]; // "msg"
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        Player receivedPlayer = getProxyServer().getPlayer(args[0]).get();

        if (messageCommands(baseCommand)){
            if (source.hasPermission("MessageDisabler.disable")){
                ChatClear.clearChat(receivedPlayer);
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
        return logger;
    }

    public ProxyServer getProxyServer(){
        return proxyServer;
    }

    public YamlDocument getConfig() {
        return config;
    }

    public static MessageDisabler getInstance(){
        return instance;
    }
}
