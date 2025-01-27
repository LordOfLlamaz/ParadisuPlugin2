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
import net.paradisu.paradisuplugin.velocity.commands.util.teleport.TeleportHistory;
import net.paradisu.paradisuplugin.velocity.locale.Messages;

public final class TeleportHereCommand extends AbstractCommand {
    public TeleportHereCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tph", "tphere")
            .permission("vparadisu.tph")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().tph().helpMsg())
            .argument(PlayerArgument.of("target"), ArgumentDescription.of(paradisu.commands().tph().helpArgs(0)))
            .handler(this::teleportCommand);
        this.commandManager.command(builder);
    }
    
    /**
     * Handeler for the /tph command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player target = (Player) context.get("target");
        Player player = (Player) context.getSender();

        paradisu.getConnector().getBridge().getLocation(target)
        .whenComplete((location, locationException) -> {
            if (locationException == null) {
                history.addTeleport(target, location);
                paradisu.getConnector().getBridge().teleport(target.getUsername(), player.getUsername(), m -> {})
                .whenComplete((success, teleportException) -> {
                    if (success) {
                        player.sendMessage(
                            Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                paradisu.commands().tph().output(0),
                                Placeholder.component("player", Component.text(target.getUsername()))
                            )
                        )); 
                    } else {
                        paradisu.logger().error("Error teleporting: " + teleportException.getMessage());
                    }
                });
            } else {
                paradisu.logger().error("Error getting location: " + locationException.getMessage());
            }
        });
    }
}
