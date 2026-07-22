package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.features.CCrackRng;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.features.ServerBrandManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Ported from upstream clientcommands. Only change: {@code FabricClientCommandSource} is
 * replaced by Forge's {@code CommandSourceStack}, and {@code source.sendFeedback} becomes
 * {@link ClientCommandHelper#sendFeedback}.
 */
public class CrackRNGCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ccrackrng")
            .executes(ctx -> crackPlayerRNG()));
    }

    private static int crackPlayerRNG() throws CommandSyntaxException {
        ServerBrandManager.rngWarning();
        CCrackRng.crack(seed -> {
            ClientCommandHelper.sendFeedback(Component.translatable("commands.ccrackrng.success", Long.toHexString(seed)));
            PlayerRandCracker.setSeed(seed);
            Configs.playerCrackState = PlayerRandCracker.CrackState.CRACKED;
        });
        return Command.SINGLE_SUCCESS;
    }

}
