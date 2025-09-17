package com.github.minemania.messageDisabler;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

public class ChatClear {

    public static void clearChat(Player player) {
        // Respect bypass permission
        if (player.hasPermission("MessageDisabler.bypass.clear")) {
            return;
        }

        // Push empty lines
        for (int i = 0; i < MessageDisabler.getInstance().getConfig().getInt("clear-line-amount"); i++) {
            player.sendMessage(Component.empty());
        }

    }
}

}
