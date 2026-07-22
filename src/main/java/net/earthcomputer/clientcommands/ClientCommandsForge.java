package net.earthcomputer.clientcommands;

import net.earthcomputer.clientcommands.command.CEnchantCommand;
import net.earthcomputer.clientcommands.command.CrackRNGCommand;
import net.earthcomputer.clientcommands.command.TaskCommand;
import net.earthcomputer.clientcommands.features.EnchantmentCracker;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Mod entry point, replacing upstream's Fabric ClientModInitializer (ClientCommands.java).
 * This build only registers the commands/features that have been ported so far: /cenchant,
 * /ccrackrng, and the minimal /ctask needed to back their "Cancel"/"Crack" chat links. See the
 * package-level porting notes on EnchantmentCracker and PlayerRandCracker for what changed.
 */
@Mod(ClientCommandsForge.MODID)
public final class ClientCommandsForge {
    public static final String MODID = "clientcommandsforge";

    public ClientCommandsForge(FMLJavaModLoadingContext context) {
        RegisterClientCommandsEvent.BUS.addListener(this::registerCommands);

        PlayerRandCracker.registerEvents();
        EnchantmentCracker.registerEvents();
    }

    private void registerCommands(RegisterClientCommandsEvent event) {
        CEnchantCommand.register(event.getDispatcher(), event.getBuildContext());
        CrackRNGCommand.register(event.getDispatcher());
        TaskCommand.register(event.getDispatcher());
    }
}
