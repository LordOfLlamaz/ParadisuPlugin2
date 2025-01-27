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

public final class TeleportCommand extends AbstractCommand {
    public TeleportCommand(Paradisu paradisu) {
        super(paradisu);
    }

    @Override
    public void register() {
        var builder = this.commandManager.commandBuilder("tp", "vteleport")
            .permission("vparadisu.tp")
            .meta(CommandMeta.DESCRIPTION, paradisu.commands().tp().helpMsg())
            .argument(PlayerArgument.of("target"), ArgumentDescription.of(paradisu.commands().tp().helpArgs(0)))
            .argument(PlayerArgument.optional("player"), ArgumentDescription.of(paradisu.commands().tp().helpArgs(1)))
            .handler(this::teleportHereCommand);
        this.commandManager.command(builder);
    }

    /**
     * Handeler for the /tp command
     * @param context the data specified on registration of the command
     */
    @SuppressWarnings("unchecked")
    private void teleportHereCommand(CommandContext<CommandSource> context) {
        TeleportHistory history = new TeleportHistory();

        Player target = (Player) context.get("target");
        Player player = (Player) context.getOrDefault("player", context.getSender());

        paradisu.getConnector().getBridge().getLocation(player)
        .whenComplete((location, locationException) -> {
            if (locationException == null) {
                history.addTeleport(player, location);
                paradisu.getConnector().getBridge().teleport(player.getUsername(), target.getUsername(), m -> {})
                .whenComplete((success, teleportException) -> {
                    if (success) {
                        context.getSender().sendMessage(
                            Messages.prefixed(MiniMessage.miniMessage().deserialize(
                                paradisu.commands().tp().output(0),
                                Placeholder.component("player", Component.text(player.getUsername())),
                                Placeholder.component("target", Component.text(target.getUsername()))
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
