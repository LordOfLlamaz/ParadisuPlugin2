package net.paradisu.paradisuplugin.velocity.commands.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.velocity.arguments.PlayerArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.paradisu.paradisuplugin.velocity.Paradisu;
import net.paradisu.paradisuplugin.velocity.commands.util.AbstractCommand;
import net.paradisu.paradisuplugin.velocity.commands.util.TeleportQueue;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class TeleportRequestCommand extends AbstractCommand {
    public TeleportRequestCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tpr", "tprequest")
            .permission("vparadisu.tpr")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().tpr().helpMsg())
            .argument(PlayerArgument.of("target"), ArgumentDescription.of(paradisu.commands().tpr().helpArgs(0)))
            .handler(this::teleportRequestCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tpr command
     * @param context the data specified on registration of the command
     */
    private void teleportRequestCommand(CommandContext<CommandSource> context) {
        Player target = (Player) context.get("target");
        Player player = (Player) context.getSender();

        TeleportQueue queue = new TeleportQueue();
        queue.queueTeleport(target, (new Player[] {player, target}));

        target.sendMessage(
            Messages.prefixed(MiniMessage.miniMessage().deserialize(
                paradisu.commands().tpr().output(0),
                Placeholder.component("player", Component.text(player.getUsername()))
            )
        ));

        player.sendMessage(
            Messages.prefixed(MiniMessage.miniMessage().deserialize(
                paradisu.commands().tpr().output(1),
                Placeholder.component("player", Component.text(target.getUsername()))
            )
        ));
    }
}