package net.earthcomputer.clientcommands.features;

import com.mojang.brigadier.suggestion.Suggestions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;

import java.util.concurrent.CompletableFuture;

/**
 * Ported from upstream clientcommands. This is used as a "fence": since the server processes
 * packets in order, a completed round trip of a (throwaway) tab-completion request confirms the
 * server has processed every packet sent before it - e.g. every item-throw the enchant cracker
 * sent. Adapted from Fabric's ClientConnectionEvents.DISCONNECT to Forge's
 * ClientPlayerNetworkEvent.LoggingOut. The actual packet round trip still needs a Forge-side hook
 * to deliver {@link ClientboundCommandSuggestionsPacket} responses into {@link #onCompletions};
 * see {@code mixin.rngevents.ClientPacketListenerMixin} in this package's sibling mixin package.
 */
public final class SuggestionsHook {
    static {
        ClientPlayerNetworkEvent.LoggingOut.BUS.addListener(event -> onDisconnect());
    }

    private SuggestionsHook() {
    }

    private static final int MAGIC_SUGGESTION_ID = -314159265;
    private static int currentSuggestionId = MAGIC_SUGGESTION_ID;
    private static final Int2ObjectMap<CompletableFuture<Suggestions>> pendingSuggestions = new Int2ObjectOpenHashMap<>();

    public static CompletableFuture<Void> fence() {
        return request("").thenAccept(suggestions -> {});
    }

    public static CompletableFuture<Suggestions> request(String command) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return CompletableFuture.completedFuture(Suggestions.empty().join());
        }

        currentSuggestionId--;
        CompletableFuture<Suggestions> future = new CompletableFuture<>();
        pendingSuggestions.put(currentSuggestionId, future);
        connection.send(new ServerboundCommandSuggestionPacket(currentSuggestionId, command));
        return future;
    }

    public static boolean onCompletions(ClientboundCommandSuggestionsPacket packet) {
        CompletableFuture<Suggestions> future = pendingSuggestions.remove(packet.id());
        if (future == null) {
            return false;
        }

        if (pendingSuggestions.isEmpty()) {
            currentSuggestionId = MAGIC_SUGGESTION_ID;
        }

        future.complete(packet.toSuggestions());
        return true;
    }

    private static void onDisconnect() {
        for (CompletableFuture<Suggestions> future : pendingSuggestions.values()) {
            future.complete(Suggestions.empty().join());
        }
        pendingSuggestions.clear();
        currentSuggestionId = MAGIC_SUGGESTION_ID;
    }
}
