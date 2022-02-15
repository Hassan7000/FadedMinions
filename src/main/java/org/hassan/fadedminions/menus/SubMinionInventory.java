package org.hassan.fadedminions.menus;

import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.data.MinionData;
import org.hassan.fadedminions.data.MinionItem;
import org.hassan.fadedminions.data.MinionPlayerData;
import org.hassan.fadedminions.menus.enums.MenuItemType;
import org.hassan.fadedminions.menus.enums.Message;
import org.hassan.fadedminions.menus.menu.MinionMenu;
import org.hassan.fadedminions.menus.menu.SubMinionMenu;
import org.hassan.fadedminions.utils.*;

import java.util.*;
import java.util.function.Consumer;

public class SubMinionInventory extends SubMinionMenu {


    public SubMinionInventory(FadedMinions plugin, Player player, String minionType, List<MinionData> minionDataList) {
        super(plugin, player, minionType, minionDataList);
    }

    @Override
    public String getMenuName() {
        return Common.colorMessage(plugin.getMenuConfig().getConfiguration().getString("Settings.Minion-Tier-Menu-Name")
        .replace("{minion}",minionType));
    }

    @Override
    public int getSlots() {
        return plugin.getMenuConfig().getConfiguration().getInt("Settings.Minion-Tier-Menu-Size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();

        SafeNBT safeNBT = SafeNBT.get(item);

        if(safeNBT.hasKey("tier")){
            String type = safeNBT.getString("type");


            int tier = Integer.valueOf(safeNBT.getString("tier"));
            if(MenuItemType.valueOf(type.toUpperCase()) == MenuItemType.STATIC_ITEM) return;

            if(MenuItemType.valueOf(type.toUpperCase()) == MenuItemType.TIER_ITEM){
                List<MinionData> minionList = plugin.getMinionPlayerDataManager().sortMinionByTier(minionDataList,tier);

                if(minionList.isEmpty()){
                    Message.NO_MINIONS_IN_THAT_TIER.getMessage(plugin).forEach(s -> Common.sendMessage(player,s));
                    return;
                }
                PagedPane pagedPane = new PagedPane(plugin,6 - 2, 6, "&6Minion Inventory", player, tier,minionList);
                Set<ItemStack> shopItems = new HashSet<ItemStack>();

                for(MinionData minionData : minionList){
                    MinionItem minionItem = plugin.getMinionPlayerDataManager().getMinionItemMap().get(minionData.getMinionType());

                    ArrayList<String> formattedLore = new ArrayList<>();

                    long nextUpgradeCost = 0;


                    String path = "Minions." + minionType+ ".Tiers." + String.valueOf(minionData.getLevel());
                    boolean lastRank = plugin.getMinionsConfig().getConfiguration().getBoolean(path + ".Last-Rank");
                    int timer = 0;
                    int tokensToGenerate = 0;

                    if(!lastRank){
                        int nextRank = minionData.getLevel() + 1;
                        String nextPath = "Minions." + minionType+ ".Tiers." + String.valueOf(nextRank);
                        timer = plugin.getMinionsConfig().getConfiguration().getInt(nextPath + ".Timer");
                        nextUpgradeCost = plugin.getMinionsConfig().getConfiguration().getLong(nextPath + ".Price");
                        tokensToGenerate = plugin.getMinionsConfig().getConfiguration().getInt(nextPath + ".Tokens-To-Generate");
                    }else{
                        nextUpgradeCost = plugin.getMinionsConfig().getConfiguration().getInt(path + ".Price");
                    }


                    for(String lore : plugin.getMinionsConfig().getConfiguration().getStringList("Minions." + minionType + ".Lore")){
                        lore = lore.replace("{tokens}", Common.formatValue((float) minionData.getTokensToGenerate()));
                        lore = lore.replace("{timer}", String.valueOf(minionData.getTimer()));
                        lore = lore.replace("{next_upgrade_cost}", Common.formatValue(nextUpgradeCost));
                        lore = lore.replace("{tier}", String.valueOf(minionData.getLevel()));
                        lore = lore.replace("{current_tokens}", Common.formatValue(minionData.getTokens()));
                        formattedLore.add(Common.colorMessage(lore));

                    }

                    ItemStack guiItem = new ItemBuilder(minionItem.getMaterial())
                            .setDisplayName(minionItem.getMaterialName())
                            .setLore(formattedLore)
                            .setKey("UUID", minionData.getUuid().toString())
                            .setKey("upgradecost", String.valueOf(nextUpgradeCost))
                            .setKey("lastRank", String.valueOf(lastRank))
                            .setKey("currenttier", String.valueOf(minionData.getLevel()))
                            .setKey("timer", String.valueOf(timer))
                            .setKey("TTG", String.valueOf(tokensToGenerate))
                            .build();
                    shopItems.add(guiItem);
                }

                for (ItemStack shopItem : shopItems) {
                    // Use Lambda for Java 8 :)
                    pagedPane.addButton(new Button(shopItem, new Consumer<InventoryClickEvent>() {
                        @Override
                        public void accept(InventoryClickEvent e) {
                            // The Human Entity that clicked this Button/Item
                            HumanEntity whoClicked = e.getWhoClicked();

                            // Making sure the Human Entity is a Player
                            if (whoClicked instanceof Player) {
                                // Now we have the player
                                Player player = (Player) whoClicked;
                                SafeNBT safeNBT = SafeNBT.get(e.getCurrentItem());
                                if(safeNBT.hasKey("UUID")){
                                    UUID uuid = UUID.fromString(safeNBT.getString("UUID"));
                                    long upgradeCost = Long.valueOf(safeNBT.getString("upgradecost"));
                                    boolean lastRank = Boolean.valueOf(safeNBT.getString("lastRank"));
                                    int currentTier = Integer.valueOf(safeNBT.getString("currenttier"));
                                    int timer = Integer.valueOf(safeNBT.getString("timer"));
                                    int tokensToGenerate = Integer.valueOf(safeNBT.getString("TTG"));
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


                                            data.setTimer(timer > 0 ? timer : 20);
                                            data.setCurrentTimer(timer > 0 ? timer : 20);
                                            data.setTokensToGenerate(tokensToGenerate > 0 ? tokensToGenerate : 20000);
                                            List<MinionData> minionList = plugin.getMinionPlayerDataManager().sortMinionByTier(minionPlayerData.getMinionDataList(), currentTier);
                                            player.closeInventory();
                                            if(minionList.isEmpty()){
                                                List<MinionData> minionPlayerDataList = minionPlayerData.sortMinionsByType(minionType);
                                                new SubMinionInventory(plugin,player,minionType,minionPlayerDataList).open();
                                                return;
                                            }

                                            List<MinionData> minionPlayerDataList = minionPlayerData.sortMinionsByType(minionType);
                                            new SubMinionInventory(plugin,player,minionType,minionPlayerDataList).open();

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
                        }
                    }));
                }

                pagedPane.open(player);

                //new MinionInventory(plugin,player,minionType,minionList,tier).open();
            }
            if(MenuItemType.valueOf(type.toUpperCase()) == MenuItemType.COLLECT_SINGLE){
                long tokens = plugin.getMinionPlayerDataManager().getOverAllTokensFromType(player, minionType);

                if(tokens <= 0){
                    Message.NO_TOKENS_TO_CLAIM.getMessage(plugin).forEach(s -> Common.sendMessage(player,s));
                    return;
                }

                TokenEnchantAPI.getInstance().addTokens(player, tokens);
                Message.TOKENS_CLAIM.getMessage(plugin).forEach(s -> Common.sendMessage(player,s
                        .replace("{tokens}", Common.formatValue(tokens))));
                plugin.getMinionPlayerDataManager().removeAllTokens(player);



            }


        }
    }

    @Override
    public void setMenuItems() {
        for(String menu : plugin.getMenuConfig().getConfiguration().getConfigurationSection("Minion-Menu." + minionType).getKeys(false)){
            String path = "Minion-Menu." + minionType + "." + menu;

            Material material = Material.matchMaterial(plugin.getMenuConfig().getConfiguration().getString(path + ".Material"));

            if(material == null) {
                Bukkit.getLogger().info("MATERIAL: " + plugin.getMinionsConfig().getConfiguration().getString(path + ".Material") + " doesn't exist");
                continue;
            }
            int tier = plugin.getMenuConfig().getConfiguration().getInt(path + ".Tier");
            String name = plugin.getMenuConfig().getConfiguration().getString(path + ".Name");
            String type = plugin.getMenuConfig().getConfiguration().getString(path + ".Type");
            ArrayList<String> formattedLore = new ArrayList<>();

            for(String lore : plugin.getMenuConfig().getConfiguration().getStringList(path + ".Lore")){
                lore = lore.replace("{collect}", Common.formatValue(plugin.getMinionPlayerDataManager().getOverAllTokensFromType(player,minionType)));
                lore = lore.replace("{amount}", String.valueOf(plugin.getMinionPlayerDataManager().amountOfMinionsInTier(minionDataList, tier)));
                formattedLore.add(Common.colorMessage(lore));
            }





            int slot = plugin.getMenuConfig().getConfiguration().getInt(path + ".Slot");

            ItemStack item = new ItemBuilder(material)
                    .setDisplayName(Common.colorMessage(name))
                    .setLore(formattedLore)
                    .setKey("tier", String.valueOf(tier))
                    .setKey("type", type)
                    .build();

            inventory.setItem(slot, item);
        }
    }
}
