package org.hassan.fadedminions.data;

import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.menus.enums.Message;
import org.hassan.fadedminions.utils.Common;
import org.hassan.fadedminions.utils.ItemBuilder;

import java.util.*;

public class MinionPlayerDataManager {

    private FadedMinions plugin;
    private HashMap<String, MinionItem> minionItemMap = new HashMap<>();


    public HashMap<String, MinionItem> getMinionItemMap(){
        return minionItemMap;
    }


    private HashMap<UUID, MinionPlayerData> minionPlayerDataMap = new HashMap<>();

    public HashMap<UUID, MinionPlayerData> getMinionPlayerDataMap(){
        return  minionPlayerDataMap;
    }

    public MinionPlayerDataManager(FadedMinions plugin){
        this.plugin = plugin;
        minionPlayerDataMap = (HashMap<UUID, MinionPlayerData>) plugin.load(plugin.file);

        if(minionPlayerDataMap == null) {
            minionPlayerDataMap = new HashMap<UUID, MinionPlayerData>();
        }
    }

    public void save(){
        if(plugin.file.exists()){
            plugin.saveData(plugin.file);
        }
    }




    public boolean hasMinionPlayerData(Player player){
        return getMinionPlayerDataMap().containsKey(player.getUniqueId());
    }

    public void saveMinionPlayerData(Player player){
        MinionPlayerData minionPlayerData = new MinionPlayerData();
        getMinionPlayerDataMap().put(player.getUniqueId(), minionPlayerData);

    }

    public MinionPlayerData getMinionPlayerData(Player player){
        MinionPlayerData minionPlayerData = getMinionPlayerDataMap().get(player.getUniqueId());
        return minionPlayerData != null ? minionPlayerData : null;
    }

    public void addMinion(Player player, MinionData minionData){
        UUID uuid = UUID.randomUUID();
        minionData.setUuid(uuid);
        MinionPlayerData minionPlayerData = this.getMinionPlayerData(player);

        minionPlayerData.addMinion(minionData);

    }

    public MinionData getMinionDataByUUID(Player player, UUID uuid){
        MinionPlayerData minionPlayerData = this.getMinionPlayerData(player);

        if(minionPlayerData == null) return null;

        for(MinionData data : minionPlayerData.getMinionDataList()){
            if(data.getUuid().equals(uuid) || data.getUuid() == uuid){
                return data;
            }
        }

        return null;

    }

    public long getOverAllTokens(Player player){
        long tokens = 0;
        MinionPlayerData minionPlayerData = this.getMinionPlayerData(player);
        if(minionPlayerData == null) return 0;
        for(MinionData data : minionPlayerData.getMinionDataList()){
            tokens += data.getTokens();
        }
        return tokens;
    }

    public long getOverAllTokensFromType(Player player, String type){
        long tokens = 0;
        MinionPlayerData minionPlayerData = this.getMinionPlayerData(player);
        if(minionPlayerData == null) return 0;
        for(MinionData data : minionPlayerData.getMinionDataList()){
            if(type.equalsIgnoreCase(data.getMinionType())){
                tokens += data.getTokens();
            }
        }
        return tokens;
    }

    public void removeAllTokens(Player player){
        MinionPlayerData minionPlayerData = this.getMinionPlayerData(player);
        if(minionPlayerData == null) return;
        for(MinionData data : minionPlayerData.getMinionDataList()){
            data.setTokens(0);
        }
    }

    public void removeTokensFromType(Player player, String type){
        MinionPlayerData minionPlayerData = this.getMinionPlayerData(player);
        if(minionPlayerData == null) return;
        for(MinionData data : minionPlayerData.getMinionDataList()){
            if(type.equalsIgnoreCase(data.getMinionType())){
                data.setTokens(0);
            }
        }
    }


    public void upgradeAll(Player player, List<MinionData> minionDataList, int level){
        int minionUpgraded = 0;
        long minionCost = 0;
       // TokenEnchantAPI.getInstance().removeTokens()
        for(MinionData minionData : minionDataList){
            int nextRank = minionData.getLevel() + 1;
            String nextPath = "Minions." + minionData.getMinionType()+ ".Tiers." + String.valueOf(nextRank);

            String path = "Minions." + minionData.getMinionType()+ ".Tiers." + String.valueOf(minionData.getLevel());
            boolean lastRank = plugin.getMinionsConfig().getConfiguration().getBoolean(path + ".Last-Rank");

            if(lastRank) continue;


            long nextUpgradeCost = plugin.getMinionsConfig().getConfiguration().getLong(nextPath + ".Price");
            int timer = plugin.getMinionsConfig().getConfiguration().getInt(nextPath + ".Timer");
            int tokensToGenerate = plugin.getMinionsConfig().getConfiguration().getInt(nextPath + ".Tokens-To-Generate");


            if(minionData.getLevel() == level){
                double tokens = TokenEnchantAPI.getInstance().getTokens(player);

                if(tokens >= nextUpgradeCost){
                    int nextLevel = minionData.getLevel() + 1;
                    minionUpgraded++;
                    minionCost += nextUpgradeCost;


                    minionData.setLevel(nextLevel);
                    minionData.setTimer(timer);
                    minionData.setCurrentTimer(timer);
                    minionData.setTokensToGenerate(tokensToGenerate);

                    TokenEnchantAPI.getInstance().removeTokens(player, nextUpgradeCost);
                }

            }

        }
        player.closeInventory();

        if(minionUpgraded == 0 && minionCost == 0){
            Common.sendMessage(player, "&7You haven't upgraded any minions");
            return;
        }

        for(String message : Message.UPGRADE_ALL.getMessage(plugin)){
            message = message.replace("{amount}", String.valueOf(minionUpgraded));
            message = message.replace("{tokens}", Common.formatValue(minionCost));
            Common.sendMessage(player, message);
        }

    }

    public int amountOfMinionsInTier(List<MinionData> minionDataList, int level){
        List<MinionData> minionDataTier = sortMinionByTier(minionDataList, level);

        return minionDataTier.size();
    }

    public List<MinionData> sortMinionByTier(List<MinionData> minionData, int tier){

        List<MinionData> minionDataList = new ArrayList<>();

        for(MinionData data : minionData){
            if(data.getLevel() == tier){
                minionDataList.add(data);
            }
        }

        return minionDataList;
    }

    public void loadMinions(){
        int minions = 0;
        for(String minion : plugin.getMinionsConfig().getConfiguration().getConfigurationSection("Minions").getKeys(false)){
            String path = "Minions." + minion;

            Material material = Material.matchMaterial(plugin.getMinionsConfig().getConfiguration().getString(path + ".Material"));

            if(material == null){
                Bukkit.getLogger().info("MATERIAL: " + plugin.getMinionsConfig().getConfiguration().getString(path + ".Material") + " doesn't exist");
                continue;
            }

            String name = plugin.getMinionsConfig().getConfiguration().getString(path + ".Name");
            ArrayList<String> formattedLore = new ArrayList<>();


            int timer = 0;
            int tokens = 0;

            for(String tier : plugin.getMinionsConfig().getConfiguration().getConfigurationSection(path + ".Tiers").getKeys(false)){
                String tierPath = path + ".Tiers." + tier;

                boolean firstRank = plugin.getMinionsConfig().getConfiguration().getBoolean(tierPath + ".First-Rank");

                if(firstRank){
                    timer = plugin.getMinionsConfig().getConfiguration().getInt(tierPath + ".Timer");
                    tokens = plugin.getMinionsConfig().getConfiguration().getInt(tierPath + ".Tokens-To-Generate");
                }
            }

            for(String lore : plugin.getMinionsConfig().getConfiguration().getStringList(path + ".Lore")){
                lore = lore.replace("{tokens}", Common.formatValue(tokens));
                lore = lore.replace("{timer}", Common.checkTime(timer));
                formattedLore.add(Common.colorMessage(lore));
            }


            MinionItem minionItem = new MinionItem();

            minionItem.setMinionName(minion);
            minionItem.setLore(formattedLore);
            minionItem.setMaterial(material);
            minionItem.setTiers(plugin.getMinionsConfig().getConfiguration().getConfigurationSection(path + ".Tiers"));
            minionItem.setMaterialName(Common.colorMessage(name));


            ItemStack item = new ItemBuilder(material)
                    .setDisplayName(Common.colorMessage(name))
                    .setLore(formattedLore)
                    .setKey("minion", minion)
                    .build();
            minionItem.setItem(item);

            getMinionItemMap().put(minion, minionItem);
            minions++;

        }

        Bukkit.getLogger().info(minions + " MINIONS LOADED!");
    }

}
