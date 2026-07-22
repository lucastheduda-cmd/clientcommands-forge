package net.earthcomputer.clientcommands.task;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.logging.LogUtils;
import net.earthcomputer.clientcommands.util.CComponentUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Adapted from upstream clientcommands: swaps Fabric's ClientTickEvents/level-unload events for
 * Forge's TickEvent.ClientTickEvent and LevelEvent.Unload, and drops the Relogger integration
 * (not ported), treating a disconnect as always non-relog.
 */
public class TaskManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Dynamic2CommandExceptionType CONFLICTING_TASK_EXCEPTION =
        new Dynamic2CommandExceptionType((conflictingTask, cancel) -> Component.translatable("commands.ctask.conflicting", conflictingTask, cancel));

    static {
        TickEvent.ClientTickEvent.Pre.BUS.addListener(event -> tick());
        LevelEvent.Unload.BUS.addListener(event -> {
            if (event.getLevel() instanceof ClientLevel) {
                onLevelUnload(true);
            }
        });
    }

    /**
     * The mutex for all intensive tasks, to make sure only one can run at a time
     */
    public static final Object INTENSIVE_TASK_MUTEX = new Object();

    private static final Map<String, LongTask> tasks = new LinkedHashMap<>();
    private static long nextTaskId = 1;
    private static String forceAddedTaskName = null;

    private static void tick() {
        if (tasks.isEmpty()) {
            return;
        }

        int iterationCount = 0;
        var iteratingTasks = new ArrayList<>(tasks.entrySet());
        while (!iteratingTasks.isEmpty()) {
            var itr = iteratingTasks.iterator();
            boolean tickedAnyTask = false;
            while (itr.hasNext()) {
                var taskEntry = itr.next();
                LongTask task = taskEntry.getValue();
                tickedAnyTask = true;
                if (!task.isInitialized) {
                    task.initialize();
                    task.isInitialized = true;
                }
                if (task.isCompleted()) {
                    forceAddedTaskName = null;
                    task.onCompleted();
                    if (!taskEntry.getKey().equals(forceAddedTaskName)) {
                        tasks.remove(taskEntry.getKey());
                    }
                    itr.remove();
                } else {
                    task.body();
                    if (!task.isCompleted()) {
                        task.increment();
                    }
                    if (task.isDelayScheduled()) {
                        task.unscheduleDelay();
                        itr.remove();
                    }
                }
            }

            if (!tickedAnyTask) {
                break;
            }

            if (++iterationCount == 1000) {
                LOGGER.warn("A LongTask is taking an exceptionally long time. Task list: {}", tasks);
            }
        }
    }

    private static void onLevelUnload(boolean isDisconnect) {
        var oldTasks = new ArrayList<Map.Entry<String, LongTask>>();
        {
            var itr = tasks.entrySet().iterator();
            while (itr.hasNext()) {
                var entry = itr.next();
                if (entry.getValue().stopOnLevelUnload(isDisconnect)) {
                    itr.remove();
                    oldTasks.add(entry);
                }
            }
        }

        for (var taskEntry : oldTasks) {
            taskEntry.getValue().onCompleted();
        }
    }

    public static String addTask(String name, LongTask task) throws CommandSyntaxException {
        for (var otherTask : tasks.entrySet()) {
            if (task.conflictsWith(otherTask.getValue())) {
                throw CONFLICTING_TASK_EXCEPTION.create(
                    otherTask.getKey(),
                    CComponentUtil.getCommandTextComponent("commands.client.cancel", "/ctask stop " + otherTask.getKey())
                );
            }
        }

        String actualName = (nextTaskId++) + "." + name;
        tasks.put(actualName, task);
        return actualName;
    }

    public static void forceAddTask(String fullName, LongTask task) {
        tasks.put(fullName, task);
        forceAddedTaskName = fullName;
    }

    public static int getTaskCount() {
        return tasks.size();
    }

    public static Iterable<String> getTaskNames() {
        return tasks.keySet();
    }

    public static void removeTask(String name) {
        LongTask task = tasks.get(name);
        if (task != null) {
            task._break();
        }
    }

}
