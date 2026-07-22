package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.clientcommands.task.TaskManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Minimal {@code /ctask} command: just enough to satisfy the "Cancel"/"Crack" chat links
 * printed by /cenchant and /ccrackrng ({@code /ctask stop <name>}), plus a list command.
 * Upstream's full ctask command has more features (stopping by pattern, etc.) not ported here.
 */
public class TaskCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ctask")
            .then(Commands.literal("list").executes(TaskCommand::list))
            .then(Commands.literal("stop")
                .then(Commands.argument("name", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
                    .executes(ctx -> stop(com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "name"))))));
    }

    private static int list(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        int count = TaskManager.getTaskCount();
        if (count == 0) {
            ClientCommandHelper.sendFeedback(Component.translatable("commands.ctask.list.noTasks"));
        } else {
            ClientCommandHelper.sendFeedback(Component.translatable("commands.ctask.list.success", count));
            for (String name : TaskManager.getTaskNames()) {
                ClientCommandHelper.sendFeedback(Component.literal("- " + name));
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int stop(String name) {
        boolean found = false;
        for (String taskName : TaskManager.getTaskNames()) {
            if (taskName.equals(name) || taskName.endsWith("." + name)) {
                TaskManager.removeTask(taskName);
                found = true;
            }
        }
        ClientCommandHelper.sendFeedback(found
            ? Component.translatable("commands.ctask.stop.success", 1)
            : Component.translatable("commands.ctask.stop.noMatch"));
        return Command.SINGLE_SUCCESS;
    }
}
