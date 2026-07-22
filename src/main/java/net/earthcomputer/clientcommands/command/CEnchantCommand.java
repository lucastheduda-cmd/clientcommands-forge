package net.earthcomputer.clientcommands.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.features.EnchantmentCracker;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.util.CComponentUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;

import static net.earthcomputer.clientcommands.command.arguments.ItemAndEnchantmentsPredicateArgument.*;

/**
 * Ported from upstream clientcommands. Changes from upstream:
 * <ul>
 *     <li>{@code FabricClientCommandSource} is replaced by Forge's {@code CommandSourceStack}
 *     (the type {@link net.minecraftforge.client.event.RegisterClientCommandsEvent} dispatches
 *     against); all {@code source.sendFeedback(...)} calls become
 *     {@link ClientCommandHelper#sendFeedback}, which upstream's sendFeedback also just forwards
 *     to under the hood.</li>
 *     <li>The upstream generic {@code Flag} class isn't ported (see ClientCommandHelper); the
 *     {@code --simulate} flag is instead two sibling command registrations.</li>
 *     <li>A new {@code /cenchant info} subcommand replaces upstream's enchantment-screen overlay
 *     button (see EnchantmentScreenMixin) as the way to feed the cracker the current table's
 *     clues.</li>
 *     <li>A new {@code /cenchant predict <on|off>} subcommand replaces upstream's
 *     {@code /cconfig clientcommands enchantingPrediction set <true|false>}, since the full
 *     cconfig command isn't ported.</li>
 * </ul>
 */
public class CEnchantCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("cenchant")
            .then(Commands.argument("itemAndEnchantmentsPredicate", itemAndEnchantmentsPredicate(context).withEnchantmentPredicate(CEnchantCommand::enchantmentPredicate).constrainMaxLevel())
                .executes(ctx -> cenchant(getItemAndEnchantmentsPredicate(ctx, "itemAndEnchantmentsPredicate"), false))
                .then(Commands.literal("--simulate")
                    .executes(ctx -> cenchant(getItemAndEnchantmentsPredicate(ctx, "itemAndEnchantmentsPredicate"), true))))
            .then(Commands.literal("info")
                .executes(CEnchantCommand::info))
            .then(Commands.literal("predict")
                .then(Commands.literal("on").executes(ctx -> setPredict(true)))
                .then(Commands.literal("off").executes(ctx -> setPredict(false)))));
    }

    private static boolean enchantmentPredicate(Item item, Holder<Enchantment> ench) {
        boolean inEnchantingTable = ench.is(EnchantmentTags.IN_ENCHANTING_TABLE);
        return inEnchantingTable && (item == Items.BOOK || ench.value().isPrimaryItem(new ItemStack(item)));
    }

    private static int setPredict(boolean enabled) {
        Configs.setEnchantingPrediction(enabled);
        ClientCommandHelper.sendFeedback(Component.literal(enabled ? "Enchant prediction enabled." : "Enchant prediction disabled.")
            .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.YELLOW));
        return Command.SINGLE_SUCCESS;
    }

    private static int info(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        var player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof EnchantmentMenu menu)) {
            ClientCommandHelper.sendError(Component.literal("You need to have an enchanting table open to use this."));
            return Command.SINGLE_SUCCESS;
        }
        EnchantmentCracker.printStatusAndFeedCracker(menu);
        return Command.SINGLE_SUCCESS;
    }

    private static int cenchant(ItemAndEnchantmentsPredicate itemAndEnchantmentsPredicate, boolean simulate) throws CommandSyntaxException {
        if (!Configs.enchantingPrediction) {
            Component component = Component.literal("Enchant prediction is disabled.")
                    .withStyle(ChatFormatting.RED)
                    .append(" ")
                    .append(CComponentUtil.getCommandTextComponent("commands.client.enable", "/cenchant predict on"));
            ClientCommandHelper.sendFeedback(component);
            return Command.SINGLE_SUCCESS;
        }
        if (!Configs.playerCrackState.knowsSeed() && Configs.enchCrackState != EnchantmentCracker.CrackState.CRACKED) {
            Component component = Component.translatable("commands.cenchant.uncracked")
                    .withStyle(ChatFormatting.RED)
                    .append(" ")
                    .append(CComponentUtil.getCommandTextComponent("commands.client.crack", "/ccrackrng"));
            ClientCommandHelper.sendFeedback(component);
            return Command.SINGLE_SUCCESS;
        }

        String taskName = EnchantmentCracker.manipulateEnchantments(
            itemAndEnchantmentsPredicate.item(),
            itemAndEnchantmentsPredicate.predicate(),
            simulate,
            result -> {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null) {
                    return;
                }

                if (result == null) {
                    ClientCommandHelper.sendFeedback(Component.translatable("commands.cenchant.failed"));
                    if (Configs.playerCrackState != PlayerRandCracker.CrackState.CRACKED) {
                        MutableComponent help = Component.translatable("commands.cenchant.help.uncrackedPlayerSeed")
                            .append(" ")
                            .append(CComponentUtil.getCommandTextComponent("commands.client.crack", "/ccrackrng"));
                        ClientCommandHelper.sendHelp(help);
                    }
                } else {
                    if (result.itemThrows() < 0) {
                        ClientCommandHelper.sendFeedback(Component.translatable("enchCrack.insn.itemThrows.noDummy"));
                    } else {
                        ClientCommandHelper.sendFeedback(Component.translatable("enchCrack.insn.itemThrows", result.itemThrows(), (float) result.itemThrows() / (Configs.itemThrowsPerTick * 20)));
                    }
                    ClientCommandHelper.sendFeedback(Component.translatable("enchCrack.insn.bookshelves", result.bookshelves()));
                    ClientCommandHelper.sendFeedback(Component.translatable("enchCrack.insn.slot", result.slot() + 1));
                    ClientCommandHelper.sendFeedback(Component.translatable("enchCrack.insn.enchantments"));
                    List<EnchantmentInstance> enchantments = new ArrayList<>(result.enchantments());
                    EnchantmentCracker.sortIntoTooltipOrder(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT), enchantments);
                    for (EnchantmentInstance ench : enchantments) {
                        ClientCommandHelper.sendFeedback(Component.literal("- ").append(Enchantment.getFullname(ench.enchantment(), ench.level())));
                    }
                }
            }
        );

        ClientCommandHelper.sendFeedback(Component.translatable("commands.cenchant.success")
            .append(" ")
            .append(CComponentUtil.getCommandTextComponent("commands.client.cancel", "/ctask stop " + taskName)));

        return Command.SINGLE_SUCCESS;
    }

}
