package org.hassan.fadedminions.menus;

import com.vk2gpz.tokenenchant.Y.H.C;
import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.data.MinionData;
import org.hassan.fadedminions.data.MinionItem;
import org.hassan.fadedminions.data.MinionPlayerData;
import org.hassan.fadedminions.menus.enums.Message;
import org.hassan.fadedminions.menus.menu.MinionMenu;
import org.hassan.fadedminions.menus.menu.TierMinionMenu;
import org.hassan.fadedminions.utils.Common;
import org.hassan.fadedminions.utils.ItemBuilder;
import org.hassan.fadedminions.utils.SafeNBT;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinionInventory extends TierMinionMenu {


    public MinionInventory(FadedMinions plugin, Player player, String minionType, List<MinionData> minionDataList, int tier) {
        super(plugin, player, minionType, minionDataList, tier);
    }

    @Override
    public String getMenuName() {
        return Common.colorMessage(plugin.getMenuConfig().getConfiguration().getString("Settings.Minion-Menu-Name")
                .replace("{minion}",minionType)
                .replace("{tier}", String.valueOf(tier)));
    }

    @Override
    public int getSlots() {
        return plugin.getMenuConfig().getConfiguration().getInt("Settings.Minion-Menu-Size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        InventoryAction action = e.getAction();
        SafeNBT safeNBT = SafeNBT.get(item);

        if(safeNBT.hasKey("UUID")){
            UUID uuid = UUID.fromString(safeNBT.getString("UUID"));
            int upgradeCost = Integer.valueOf(safeNBT.getString("upgradecost"));
            boolean lastRank = Boolean.valueOf(safeNBT.getString("lastRank"));
            int currentTier = Integer.valueOf(safeNBT.getString("currenttier"));


            MinionData data = plugin.getMinionPlayerDataManager().getMinionDataByUUID(player, uuid);
            double tokens = TokenEnchantAPI.getInstance().getTokens(player);

            if(e.getClick() == ClickType.LEFT){
                if(lastRank){
                    Message.MINION_IS_LAST_RANK.getMessage(plugin).forEach(s -> Common.sendMessage(player,s));
                    return;
                }

                if(tokens >= upgradeCost){
                    int nextLevel = data.getLevel() + 1;
                    TokenEnchantAPI.getInstance().removeTokens(player, upgradeCost);
                    Message.BOUGHT_UPGRADE.getMessage(plugin).forEach(s -> Common.sendMessage(player,s
                    .replace("{level}", String.valueOf(nextLevel))
                    .replace("{tokens}", Common.formatValue(upgradeCost))));


                    //Update the inventory
                    MinionPlayerData minionPlayerData = plugin.getMinionPlayerDataManager().getMinionPlayerData(player);

                    if(minionPlayerData == null){
                        Common.sendMessage(player, "&cERROR PLEASE CONTACT AN ADMIN");
                        return;
                    }
                    data.setLevel(nextLevel);
                    List<MinionData> minionList = plugin.getMinionPlayerDataManager().sortMinionByTier(minionPlayerData.getMinionDataList(), currentTier);
                    player.closeInventory();

                    if(minionList.isEmpty()){
                        List<MinionData> minionPlayerDataList = minionPlayerData.sortMinionsByType(minionType);
                        new SubMinionInventory(plugin,player,minionType,minionPlayerDataList).open();
                        return;
                    }

                    new MinionInventory(plugin,player,minionType,minionList, tier).open();
                    //HERE WE SET THE NEW VALUES LIKE TIMER, TOKENS TO GENERATE ETC


                }else{
                    Message.NOT_ENOUGH_TOKENS.getMessage(plugin).forEach(s -> Common.sendMessage(player,s));
                }
            }
            if(e.getClick() == ClickType.RIGHT){
                if(data.getTokens() == 0){
                    Message.NO_TOKENS_TO_CLAIM.getMessage(plugin).forEach(s -> Common.sendMessage(player,s));
                    return;
                }
                TokenEnchantAPI.getInstance().addTokens(player, data.getTokens());
                Message.TOKENS_CLAIM.getMessage(plugin).forEach(s -> Common.sendMessage(player,s
                        .replace("{tokens}", Common.formatValue(data.getTokens()))));
                data.setTokens(data.getTokens() - data.getTokens());
            }



        }

    }

    @Override
    public void setMenuItems() {
        int slot = 0;
        for(MinionData minionData : minionDataList){
            MinionItem minionItem = plugin.getMinionPlayerDataManager().getMinionItemMap().get(minionData.getMinionType());

            ArrayList<String> formattedLore = new ArrayList<>();

            int nextUpgradeCost = 0;


                String path = "Minions." + minionType+ ".Tiers." + String.valueOf(minionData.getLevel());
                boolean lastRank = plugin.getMinionsConfig().getConfiguration().getBoolean(path + ".Last-Rank");

                if(!lastRank){
                    int nextRank = minionData.getLevel() + 1;
                    String nextPath = "Minions." + minionType+ ".Tiers." + String.valueOf(nextRank);

                    nextUpgradeCost = plugin.getMinionsConfig().getConfiguration().getInt(nextPath + ".Price");
                }else{
                    nextUpgradeCost = plugin.getMinionsConfig().getConfiguration().getInt(path + ".Price");
                }


            for(String lore : plugin.getMinionsConfig().getConfiguration().getStringList("Minions." + minionType + ".Lore")){
                lore = lore.replace("{tokens}", String.valueOf(minionData.getTokensToGenerate()));
                lore = lore.replace("{timer}", String.valueOf(minionData.getTimer()));
                lore = lore.replace("{next_upgrade_cost}", String.valueOf(nextUpgradeCost));
                lore = lore.replace("{tier}", String.valueOf(minionData.getLevel()));
                lore = lore.replace("{current_tokens}", Common.formatValue(minionData.getTokens()));
                formattedLore.add(Common.colorMessage(lore));

            }

            ItemStack item = new ItemBuilder(minionItem.getMaterial())
                    .setDisplayName(minionItem.getMaterialName())
                    .setLore(formattedLore)
                    .setKey("UUID", minionData.getUuid().toString())
                    .setKey("upgradecost", String.valueOf(nextUpgradeCost))
                    .setKey("lastRank", String.valueOf(lastRank))
                    .setKey("currenttier", String.valueOf(minionData.getLevel()))
                    .build();

            inventory.setItem(slot, item);
            slot++;
        }
    }

    public void updateInventory(Inventory inventory, List<MinionData> minionDataList){
        int slot = 0;

        for(MinionData minionData : minionDataList){
            MinionItem minionItem = plugin.getMinionPlayerDataManager().getMinionItemMap().get(minionData.getMinionType());

            ArrayList<String> formattedLore = new ArrayList<>();

            int nextUpgradeCost = 0;


            String path = "Minions." + minionType+ ".Tiers." + String.valueOf(minionData.getLevel());
            boolean lastRank = plugin.getMinionsConfig().getConfiguration().getBoolean(path + ".Last-Rank");

            if(!lastRank){
                int nextRank = minionData.getLevel() + 1;
                String nextPath = "Minions." + minionType+ ".Tiers." + String.valueOf(nextRank);

                nextUpgradeCost = plugin.getMinionsConfig().getConfiguration().getInt(nextPath + ".Price");
            }else{
                nextUpgradeCost = plugin.getMinionsConfig().getConfiguration().getInt(path + ".Price");
            }


            for(String lore : plugin.getMinionsConfig().getConfiguration().getStringList("Minions." + minionType + ".Lore")){
                lore = lore.replace("{tokens}", String.valueOf(minionData.getTokensToGenerate()));
                lore = lore.replace("{timer}", String.valueOf(minionData.getTimer()));
                lore = lore.replace("{next_upgrade_cost}", String.valueOf(nextUpgradeCost));
                lore = lore.replace("{tier}", String.valueOf(minionData.getLevel()));
                lore = lore.replace("{current_tokens}", Common.formatValue(minionData.getTokens()));
                formattedLore.add(Common.colorMessage(lore));

            }

            ItemStack item = new ItemBuilder(minionItem.getMaterial())
                    .setDisplayName(minionItem.getMaterialName())
                    .setLore(formattedLore)
                    .setKey("UUID", minionData.getUuid().toString())
                    .setKey("upgradecost", String.valueOf(nextUpgradeCost))
                    .setKey("lastRank", String.valueOf(lastRank))
                    .build();

            inventory.setItem(slot, item);
            slot++;


        }
    }
}
