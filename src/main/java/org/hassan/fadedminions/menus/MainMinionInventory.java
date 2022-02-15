package org.hassan.fadedminions.menus;

import com.vk2gpz.tokenenchant.api.TokenEnchantAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.data.MinionData;
import org.hassan.fadedminions.data.MinionPlayerData;
import org.hassan.fadedminions.menus.enums.MenuItemType;
import org.hassan.fadedminions.menus.enums.Message;
import org.hassan.fadedminions.menus.menu.MinionMenu;
import org.hassan.fadedminions.utils.Common;
import org.hassan.fadedminions.utils.ItemBuilder;
import org.hassan.fadedminions.utils.SafeNBT;

import java.util.ArrayList;
import java.util.List;

public class MainMinionInventory extends MinionMenu {


    public MainMinionInventory(FadedMinions plugin, Player player) {
        super(plugin, player);
    }

    @Override
    public String getMenuName() {
        return Common.colorMessage(plugin.getMenuConfig().getConfiguration().getString("Settings.Main-Minion-Menu-Name"));
    }

    @Override
    public int getSlots() {
        return plugin.getMenuConfig().getConfiguration().getInt("Settings.Main-Minion-Menu-Size");
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();

        if(item == null ||item.getType() == Material.AIR) return;

        SafeNBT safeNBT = SafeNBT.get(item);

        if(safeNBT.hasKey("minion")){

            String type = safeNBT.getString("type");
            if(MenuItemType.valueOf(type.toUpperCase()) == MenuItemType.STATIC_ITEM) return;

            if(MenuItemType.valueOf(type.toUpperCase()) == MenuItemType.MINION_ITEM){
                String minion = safeNBT.getString("minion");

                if(plugin.getMinionPlayerDataManager().hasMinionPlayerData(player)){
                    MinionPlayerData minionPlayerData = plugin.getMinionPlayerDataManager().getMinionPlayerData(player);
                    List<MinionData> minionPlayerDataList = minionPlayerData.sortMinionsByType(minion);
                    new SubMinionInventory(plugin,player,minion,minionPlayerDataList).open();
                }
            }


            if(MenuItemType.valueOf(type.toUpperCase()) == MenuItemType.COLLECT_ALL){
                //Collect all here
                long tokens = plugin.getMinionPlayerDataManager().getOverAllTokens(player);

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
        for(String menu : plugin.getMenuConfig().getConfiguration().getConfigurationSection("Main-Minion-Menu").getKeys(false)){
            String path = "Main-Minion-Menu." + menu;

            Material material = Material.matchMaterial(plugin.getMenuConfig().getConfiguration().getString(path + ".Material"));
            if(material == null){
                Bukkit.getLogger().info("MATERIAL: " + plugin.getMinionsConfig().getConfiguration().getString(path + ".Material") + " doesn't exist");
                continue;
            }

            String name = plugin.getMenuConfig().getConfiguration().getString(path + ".Name");
            String type = plugin.getMenuConfig().getConfiguration().getString(path + ".Type");
            ArrayList<String> formattedLore = new ArrayList<>();

            for(String lore : plugin.getMenuConfig().getConfiguration().getStringList(path + ".Lore")){
                lore = lore.replace("{collect}", Common.formatValue(plugin.getMinionPlayerDataManager().getOverAllTokens(player)));
                formattedLore.add(Common.colorMessage(lore));
            }

            String minion = plugin.getMenuConfig().getConfiguration().getString(path + ".Minion-Inventory");

            int slot = plugin.getMenuConfig().getConfiguration().getInt(path + ".Slot");

            ItemStack item = new ItemBuilder(material)
                    .setDisplayName(Common.colorMessage(name))
                    .setLore(formattedLore)
                    .setKey("minion", minion != null ? minion : "")
                    .setKey("type", type)
                    .build();

            inventory.setItem(slot, item);

        }
    }
}
