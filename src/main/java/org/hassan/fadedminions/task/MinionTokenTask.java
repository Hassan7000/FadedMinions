package org.hassan.fadedminions.task;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.data.MinionData;
import org.hassan.fadedminions.data.MinionPlayerData;

import java.util.Map;
import java.util.UUID;

public class MinionTokenTask extends BukkitRunnable {

    private FadedMinions plugin;

    public MinionTokenTask(FadedMinions plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for(Map.Entry<UUID, MinionPlayerData> entry : plugin.getMinionPlayerDataManager().getMinionPlayerDataMap().entrySet()){
            MinionPlayerData minionPlayerData = entry.getValue();

            for(MinionData minionData : minionPlayerData.getMinionDataList()){

                int currentTimer = minionData.getCurrentTimer() - 1;

                if(currentTimer <= 0){
                    minionData.setCurrentTimer(minionData.getTimer());
                    minionData.setTokens((float) (minionData.getTokens() + minionData.getTokensToGenerate()));
                }else{
                    minionData.setCurrentTimer(currentTimer);
                }


            }
        }
    }
}
