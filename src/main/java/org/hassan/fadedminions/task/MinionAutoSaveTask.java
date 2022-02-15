package org.hassan.fadedminions.task;

import org.bukkit.scheduler.BukkitRunnable;
import org.hassan.fadedminions.FadedMinions;

public class MinionAutoSaveTask extends BukkitRunnable {

    private FadedMinions plugin;

    public MinionAutoSaveTask(FadedMinions plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getMinionPlayerDataManager().save();
    }
}
