package net.earthcomputer.clientcommands.task;

import com.mojang.logging.LogUtils;
import net.earthcomputer.clientcommands.Configs;
import net.earthcomputer.clientcommands.features.PlayerRandCracker;
import net.earthcomputer.clientcommands.features.SuggestionsHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.Set;

/**
 * Ported from upstream clientcommands. Changes from upstream:
 * <ul>
 *     <li>Upstream hooks {@code MoreClientEntityEvents.POST_ADD}, a Fabric event fed straight
 *     from the raw {@code ClientboundAddEntityPacket}. Forge doesn't have an equivalent packet
 *     -level "entity added" event, so this instead uses Forge's real
 *     {@link EntityJoinLevelEvent} and reads the item's velocity from
 *     {@link Entity#getDeltaMovement()}. By the time that event fires the entity's delta movement
 *     has already been set from the packet's decoded velocity (in the vanilla add-entity packet
 *     handler, before {@code Level.addFreshEntity} runs), so this is numerically equivalent to
 *     reading the packet directly.</li>
 *     <li>{@code PlayerRandCracker.RNG_CALLED_EVENT.register(...)} (Fabric Event) is replaced
 *     with {@code PlayerRandCracker.registerRngCallListener(...)}.</li>
 * </ul>
 */
public abstract class ItemThrowTask extends SimpleTask {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Set<Object> MUTEX_KEYS = Set.of(ItemThrowTask.class);

    public static final int FLAG_URGENT = 1;
    public static final int FLAG_WAIT_FOR_ITEMS = 2;

    private static WeakReference<ItemThrowTask> currentThrowTask = null;

    static {
        EntityJoinLevelEvent.BUS.addListener(ItemThrowTask::handleItemSpawn);
        PlayerRandCracker.registerRngCallListener(ItemThrowTask::handleRNGCallEvent);
    }

    private final int totalItemsToThrow;
    private final int flags;

    private int confirmedItemThrows;
    private int sentItemThrows;
    private float itemThrowsAllowedThisTick;
    private boolean waitingFence = false;
    private boolean failed = false;
    private boolean isThrowingItem = false;
    private boolean hadUnexpectedRNGCall = false;
    private final Set<PlayerRandCracker.ThrowItemsResult.Type> errorTypesHappened = EnumSet.noneOf(PlayerRandCracker.ThrowItemsResult.Type.class);

    public ItemThrowTask(int itemsToThrow) {
        this(itemsToThrow, 0);
    }

    public ItemThrowTask(int itemsToThrow, int flags) {
        this.totalItemsToThrow = itemsToThrow;
        this.flags = flags;
    }

    @Override
    public boolean condition() {
        return waitingFence || sentItemThrows != totalItemsToThrow || sentItemThrows > confirmedItemThrows;
    }

    @Override
    protected void onTick() {
        itemThrowsAllowedThisTick += Configs.itemThrowsPerTick;

        while (((flags & FLAG_URGENT) != 0 || itemThrowsAllowedThisTick >= 1) && sentItemThrows < totalItemsToThrow) {
            itemThrowsAllowedThisTick--;
            isThrowingItem = true;
            PlayerRandCracker.ThrowItemsResult throwItemsResult = PlayerRandCracker.throwItem();
            isThrowingItem = false;
            if (hadUnexpectedRNGCall) {
                return;
            }
            if (!throwItemsResult.isSuccess()) {
                onFailedToThrowItem(throwItemsResult);
                if ((flags & FLAG_WAIT_FOR_ITEMS) != 0) {
                    return;
                }
                failed = true;
                _break();
                return;
            }
            onItemThrown(++sentItemThrows, totalItemsToThrow);
        }

        if (!waitingFence && sentItemThrows == totalItemsToThrow && confirmedItemThrows < sentItemThrows) {
            waitingFence = true;
            SuggestionsHook.fence().thenAccept(v -> {
                if (sentItemThrows > confirmedItemThrows) {
                    LOGGER.info("Server rejected {} item throws. Rethrowing them.", sentItemThrows - confirmedItemThrows);
                    while (sentItemThrows > confirmedItemThrows) {
                        PlayerRandCracker.unthrowItem();
                        sentItemThrows--;
                    }
                }
                waitingFence = false;
            });
        }
    }

    @Override
    public void initialize() {
        currentThrowTask = new WeakReference<>(this);
    }

    @Override
    public void onCompleted() {
        if (!failed) {
            onSuccess();
        }
        currentThrowTask = null;
    }

    @Override
    public Set<Object> getMutexKeys() {
        return MUTEX_KEYS;
    }

    protected void onFailedToThrowItem(PlayerRandCracker.ThrowItemsResult throwItemsResult) {
        if (throwItemsResult.getType() != PlayerRandCracker.ThrowItemsResult.Type.NOT_ENOUGH_ITEMS || (flags & FLAG_WAIT_FOR_ITEMS) == 0) {
            if (errorTypesHappened.add(throwItemsResult.getType())) {
                throwItemsResult.sendErrorMessage();
            }
        }
    }

    protected void onSuccess() {
    }

    protected abstract void onUnexpectedRNGCall(PlayerRandCracker.RNGCallType callType);

    protected void onItemSpawn(Entity entity) {
    }

    protected void onItemThrown(int current, int total) {
    }

    protected boolean requireCrackedRNG() {
        return true;
    }

    private static void handleItemSpawn(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ClientLevel) || event.getEntity().getType() != EntityTypes.ITEM) {
            return;
        }
        if (!(event.getEntity() instanceof ItemEntity)) {
            return;
        }

        ItemThrowTask task = currentThrowTask == null ? null : currentThrowTask.get();
        if (task == null) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Entity entity = event.getEntity();
        if (player.getEyePosition().distanceToSqr(entity.getX(), entity.getY(), entity.getZ()) > 1) {
            return;
        }

        task.confirmedItemThrows++;
        task.onItemSpawn(entity);
    }

    private static void handleRNGCallEvent(PlayerRandCracker.RNGCallEvent event) {
        ItemThrowTask task = currentThrowTask == null ? null : currentThrowTask.get();
        if (task != null) {
            if (task.isThrowingItem && event.getType() == PlayerRandCracker.RNGCallType.DROP_ITEM) {
                if (task.requireCrackedRNG()) {
                    event.setMaintained();
                } else {
                    event.setMaintainedEvenIfSeedUnknown();
                }
                task.isThrowingItem = false;
            } else {
                task.onUnexpectedRNGCall(event.getType());
                task.hadUnexpectedRNGCall = true;
                task.failed = true;
                task._break();
            }
        }
    }

    @Override
    public String toString() {
        return "ItemThrowTask[totalItemsToThrow=" + totalItemsToThrow + ",flags=" + flags + "]";
    }
}
