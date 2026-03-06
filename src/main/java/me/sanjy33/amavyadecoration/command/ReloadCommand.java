package me.sanjy33.amavyadecoration.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.sanjy33.amavyadecoration.AmavyaDecoration;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ReloadCommand implements BasicCommand {

    private final AmavyaDecoration plugin;

    public ReloadCommand(AmavyaDecoration plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        plugin.reloadManagers();
        commandSourceStack.getSender().sendMessage(Component.text("AmavyaDecoration: Config Reloaded."));
    }

    @Override
    public @Nullable String permission() {
        return "amavyadecoration.reload";
    }
}
