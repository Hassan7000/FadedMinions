package org.hassan.fadedminions;

import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.hassan.fadedminions.commands.FadedMinionCommand;
import org.hassan.fadedminions.data.MinionData;
import org.hassan.fadedminions.data.MinionItem;
import org.hassan.fadedminions.data.MinionPlayerData;
import org.hassan.fadedminions.data.MinionPlayerDataManager;
import org.hassan.fadedminions.files.MenuConfig;
import org.hassan.fadedminions.files.MessageConfig;
import org.hassan.fadedminions.files.MinionsConfig;
import org.hassan.fadedminions.listeners.PlayerListeners;
import org.hassan.fadedminions.task.MinionAutoSaveTask;
import org.hassan.fadedminions.task.MinionTokenTask;
import org.hassan.fadedminions.utils.Common;
import org.hassan.fadedminions.utils.ItemBuilder;

import javax.swing.text.html.parser.Entity;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FadedMinions extends JavaPlugin {


    private MinionsConfig minionsConfig;

    private MenuConfig menuConfig;
    private MessageConfig messageConfig;

    private MinionPlayerDataManager minionPlayerDataManager;


    private CommandManager commandManager;

    private BukkitTask minionTokenTask;
    private BukkitTask minionAutoSaveTask;

    public File file = new File(getDataFolder(), "Data.dat");
    @Override
    public void onEnable() {
        minionsConfig = new MinionsConfig(this);
        menuConfig = new MenuConfig(this);
        messageConfig = new MessageConfig(this);
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        minionPlayerDataManager = new MinionPlayerDataManager(this);
        minionPlayerDataManager.loadMinions();





        commandManager = new CommandManager(this);
        commandManager.register(new FadedMinionCommand(this));

        minionTokenTask = new MinionTokenTask(this).runTaskTimer(this,20,20);
        minionAutoSaveTask = new MinionAutoSaveTask(this).runTaskTimer(this,20,1200*20);

        getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
        updateMinions();
    }

    @Override
    public void onDisable() {
        minionPlayerDataManager.save();
        if(minionTokenTask != null){
            minionTokenTask.cancel();
        }
    }

    public MinionsConfig getMinionsConfig(){
        return minionsConfig;
    }
    public MenuConfig getMenuConfig(){
        return menuConfig;
    }
    public MessageConfig getMessageConfig(){
        return messageConfig;
    }
    public MinionPlayerDataManager getMinionPlayerDataManager(){
        return minionPlayerDataManager;
    }

    public void saveData(File f) {
        if(f.exists()) {
            try {
                ObjectOutputStream dataa = new ObjectOutputStream(new FileOutputStream(f));
                dataa.writeObject(getMinionPlayerDataManager().getMinionPlayerDataMap());
                dataa.flush();
                dataa.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public Object load(File f) {
        try {
            ObjectInputStream data = new ObjectInputStream(new FileInputStream(f));
            Object result = data.readObject();
            data.close();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateMinions(){
        for(Map.Entry<UUID, MinionPlayerData> entry : getMinionPlayerDataManager().getMinionPlayerDataMap().entrySet()){
            MinionPlayerData minionPlayerData = entry.getValue();

            for(MinionData minion : minionPlayerData.getMinionDataList()){

                String path = "Minions." + minion.getMinionType()+ ".Tiers." + String.valueOf(minion.getLevel());
                MinionData minionData = new MinionData();
                minionData.setMinionType(minion.getMinionType());
                minionData.setLevel(minion.getLevel());
                int timer = getMinionsConfig().getConfiguration().getInt(path + ".Timer");
                minionData.setTimer(timer);
                minionData.setCurrentTimer(timer);
                minionData.setTokens(0);
                int tokensToGenerate = getMinionsConfig().getConfiguration().getInt(path + ".Tokens-To-Generate");
                minionData.setTokensToGenerate(tokensToGenerate);

            }
        }
    }
}
