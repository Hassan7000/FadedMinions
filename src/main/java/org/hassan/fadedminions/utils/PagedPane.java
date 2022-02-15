package org.hassan.fadedminions.utils;

import com.vk2gpz.tokenenchant.Y.F.B;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hassan.fadedminions.FadedMinions;
import org.hassan.fadedminions.data.MinionData;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * A paged pane. Credits @ I Al Ianstaan
 */
public class PagedPane implements InventoryHolder {

    private Inventory inventory;

    private SortedMap<Integer, Page> pages = new TreeMap<>();
    private int currentIndex;
    private int pageSize;
    private FadedMinions plugin;

    private Player player;

    private int level;
    private List<MinionData> minionDataList;

    @SuppressWarnings("WeakerAccess")
    protected Button controlBack;
    @SuppressWarnings("WeakerAccess")
    protected Button controlNext;

    protected Button upgradeButton;

    /**
     * @param pageSize The page size. inventory rows - 2
     */
    public PagedPane(FadedMinions plugin, int pageSize, int rows, String title, Player player, int level, List<MinionData> minionDataList) {
        Objects.requireNonNull(title, "title can not be null!");
        if (rows > 6) {
            throw new IllegalArgumentException("Rows must be <= 6, got " + rows);
        }
        if (pageSize > 6) {
            throw new IllegalArgumentException("Page size must be <= 6, got" + pageSize);
        }

        this.pageSize = pageSize;
        this.plugin = plugin;
        this.player = player;
        this.level = level;
        this.minionDataList = minionDataList;
        inventory = Bukkit.createInventory(this, rows * 9, color(title));

        pages.put(0, new Page(pageSize));
    }

    /**
     * @param button The button to add
     */
    public void addButton(Button button) {
        for (Entry<Integer, Page> entry : pages.entrySet()) {
            if (entry.getValue().addButton(button)) {
                if (entry.getKey() == currentIndex) {
                    reRender();
                }
                return;
            }
        }
        Page page = new Page(pageSize);
        page.addButton(button);
        pages.put(pages.lastKey() + 1, page);

        reRender();
    }

    /**
     * @param button The Button to remove
     */
    @SuppressWarnings("unused")
    public void removeButton(Button button) {
        for (Iterator<Entry<Integer, Page>> iterator = pages.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<Integer, Page> entry = iterator.next();
            if (entry.getValue().removeButton(button)) {

                // we may need to delete the page
                if (entry.getValue().isEmpty()) {
                    // we have more than one page, so delete it
                    if (pages.size() > 1) {
                        iterator.remove();
                    }
                    // the currentIndex now points to a page that does not exist. Correct it.
                    if (currentIndex >= pages.size()) {
                        currentIndex--;
                    }
                }
                // if we modified the current one, re-render
                // if we deleted the current page, re-render too
                if (entry.getKey() >= currentIndex) {
                    reRender();
                }
                return;
            }
        }
    }

    /**
     * @return The amount of pages
     */
    @SuppressWarnings("WeakerAccess")
    public int getPageAmount() {
        return pages.size();
    }

    /**
     * @return The number of the current page (1 based)
     */
    @SuppressWarnings("WeakerAccess")
    public int getCurrentPage() {
        return currentIndex + 1;
    }

    /**
     * @param index The index of the new page
     */
    @SuppressWarnings("WeakerAccess")
    public void selectPage(int index) {
        if (index < 0 || index >= getPageAmount()) {
            throw new IllegalArgumentException(
                    "Index out of bounds s: " + index + " [" + 0 + " " + getPageAmount() + ")"
            );
        }
        if (index == currentIndex) {
            return;
        }

        currentIndex = index;
        reRender();
    }

    /**
     * Renders the inventory again
     */
    @SuppressWarnings("WeakerAccess")
    public void reRender() {
        inventory.clear();
        pages.get(currentIndex).render(inventory);

        controlBack = null;
        controlNext = null;
        upgradeButton = null;
        createControls(inventory);
    }

    /**
     * @param event The {@link InventoryClickEvent}
     */
    @SuppressWarnings("WeakerAccess")
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(true);




        // back item
        if (event.getSlot() == inventory.getSize() - 8) {
            if (controlBack != null) {
                controlBack.onClick(event);
            }
            return;
        }
        // next item
        else if (event.getSlot() == inventory.getSize() - 2) {
            if (controlNext != null) {
                controlNext.onClick(event);
            }
            return;
        }else if(event.getSlot() == inventory.getSize() - 3){
            if(upgradeButton != null){
                upgradeButton.onClick(event);
            }
        }

        pages.get(currentIndex).handleClick(event);

    }

    /**
     * Get the object's inventory.
     *
     * @return The inventory.
     */
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Creates the controls
     *
     * @param inventory The inventory
     */
    @SuppressWarnings("WeakerAccess")
    protected void createControls(Inventory inventory) {
        // create separator
        fillRow(
                inventory.getSize() / 9 - 2,
                getItemStack(XMaterial.GRAY_STAINED_GLASS_PANE.parseItem(), "&f"),
                inventory
        );

        if (getCurrentPage() > 1) {

            String name = plugin.getMenuConfig().getConfiguration().getString("Control-Button.Back-Page-Item.Name")
                    .replace("{current_page}", String.valueOf(getCurrentPage()))
                    .replace("{previous_page}", String.valueOf(getCurrentPage() - 1));


            ArrayList<String> formattedLore = new ArrayList<>();
            for(String lore : plugin.getMenuConfig().getConfiguration().getStringList("Control-Button.Back-Page-Item.Lore")){
                lore = lore.replace("{current_page}", String.valueOf(getCurrentPage()));
                lore = lore.replace("{previous_page}", String.valueOf(getCurrentPage() - 1));
                formattedLore.add(Common.colorMessage(lore));
            }

            ItemStack itemStack = new ItemBuilder(Material.valueOf(plugin.getMenuConfig().getConfiguration().getString("Control-Button.Back-Page-Item.Material")))
                    .setDisplayName(Common.colorMessage(name))
                    .setLore(formattedLore)
                    .build();

            controlBack = new Button(itemStack, event -> selectPage(currentIndex - 1));
            inventory.setItem(inventory.getSize() - 8, itemStack);
        }

        if (getCurrentPage() < getPageAmount()) {

            String name = plugin.getMenuConfig().getConfiguration().getString("Control-Button.Next-Page-Item.Name")
                    .replace("{current_page}", String.valueOf(getCurrentPage()))
                    .replace("{next_page}", String.valueOf(getCurrentPage() + 1));


            ArrayList<String> formattedLore = new ArrayList<>();
            for(String lore : plugin.getMenuConfig().getConfiguration().getStringList("Control-Button.Next-Page-Item.Lore")){
                lore = lore.replace("{current_page}", String.valueOf(getCurrentPage()));
                lore = lore.replace("{next_page}", String.valueOf(getCurrentPage() + 1));
                formattedLore.add(Common.colorMessage(lore));
            }

            ItemStack itemStack = new ItemBuilder(Material.valueOf(plugin.getMenuConfig().getConfiguration().getString("Control-Button.Next-Page-Item.Material")))
                    .setDisplayName(Common.colorMessage(name))
                    .setLore(formattedLore)
                    .build();
            controlNext = new Button(itemStack, event -> selectPage(getCurrentPage()));
            inventory.setItem(inventory.getSize() - 2, itemStack);
        }

        {


            String name = plugin.getMenuConfig().getConfiguration().getString("Control-Button.Information-Item.Name")
                    .replace("{current_page}", String.valueOf(getCurrentPage() ))
                    .replace("{next_page}", String.valueOf(getPageAmount()));


            ArrayList<String> formattedLore = new ArrayList<>();
            for(String lore : plugin.getMenuConfig().getConfiguration().getStringList("Control-Button.Information-Item.Lore")){
                lore = lore.replace("{current_page}", String.valueOf(getCurrentPage()));
                lore = lore.replace("{next_page}", String.valueOf(getPageAmount()));
                formattedLore.add(Common.colorMessage(lore));
            }

            ItemStack itemStack = new ItemBuilder(Material.valueOf(plugin.getMenuConfig().getConfiguration().getString("Control-Button.Next-Page-Item.Material")))
                    .setDisplayName(Common.colorMessage(name))
                    .setLore(formattedLore)
                    .build();

            inventory.setItem(inventory.getSize() - 5, itemStack);


            String upgradeName = plugin.getMenuConfig().getConfiguration().getString("Control-Button.Upgrade-Item.Name")
                    .replace("{current_page}", String.valueOf(getCurrentPage() ))
                    .replace("{next_page}", String.valueOf(getPageAmount()));


            ArrayList<String> upgradeFormattedLore = new ArrayList<>();
            for(String lore : plugin.getMenuConfig().getConfiguration().getStringList("Control-Button.Upgrade-Item.Lore")){
                lore = lore.replace("{current_page}", String.valueOf(getCurrentPage()));
                lore = lore.replace("{next_page}", String.valueOf(getPageAmount()));
                upgradeFormattedLore.add(Common.colorMessage(lore));
            }
            ItemStack upgrade = new ItemBuilder(Material.BARRIER)
                    .setDisplayName(Common.colorMessage(upgradeName))
                    .setLore(upgradeFormattedLore)
                    .build();

            upgradeButton = new Button(upgrade, event -> plugin.getMinionPlayerDataManager().upgradeAll(player, minionDataList, level));
            inventory.setItem(inventory.getSize() - 3, upgrade);
        }
    }

    private void fillRow(int rowIndex, ItemStack itemStack, Inventory inventory) {
        int yMod = rowIndex * 9;
        for (int i = 0; i < 9; i++) {
            int slot = yMod + i;
            inventory.setItem(slot, itemStack);
        }
    }

    /**
     * @param type The {@link Material} of the {@link ItemStack}
     * @param durability The durability
     * @param name The name. May be null.
     * @param lore The lore. May be null.
     *
     * @return The item
     */
    @SuppressWarnings("WeakerAccess")
    protected ItemStack getItemStack(Material type, int durability, String name, String... lore) {
        ItemStack itemStack = new ItemStack(type, 1, (short) durability);

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (name != null) {
            itemMeta.setDisplayName(color(name));
        }
        if (lore != null && lore.length != 0) {
            itemMeta.setLore(Arrays.stream(lore).map(this::color).collect(Collectors.toList()));
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    protected ItemStack getItemStack(ItemStack itemStack, String name, String... lore) {

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (name != null) {
            itemMeta.setDisplayName(color(name));
        }
        if (lore != null && lore.length != 0) {
            itemMeta.setLore(Arrays.stream(lore).map(this::color).collect(Collectors.toList()));
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @SuppressWarnings("WeakerAccess")
    protected String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * @param player The {@link Player} to open it for
     */
    public void open(Player player) {
        reRender();
        player.openInventory(getInventory());
    }


    private static class Page {
        private List<Button> buttons = new ArrayList<>();
        private int maxSize;

        Page(int maxSize) {
            this.maxSize = maxSize;
        }

        /**
         * @param event The click event
         */
        void handleClick(InventoryClickEvent event) {
            // user clicked in his own inventory. Silently drop it
            if (event.getRawSlot() > event.getInventory().getSize()) {
                return;
            }
            // user clicked outside of the inventory
            if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) {
                return;
            }
            if (event.getSlot() >= buttons.size()) {
                return;
            }
            Button button = buttons.get(event.getSlot());
            button.onClick(event);
        }

        /**
         * @return True if there is space left
         */
        boolean hasSpace() {
            return buttons.size() < maxSize * 9;
        }

        /**
         * @param button The {@link Button} to add
         *
         * @return True if the button was added, false if there was no space
         */
        boolean addButton(Button button) {
            if (!hasSpace()) {
                return false;
            }
            buttons.add(button);

            return true;
        }

        /**
         * @param button The {@link Button} to remove
         *
         * @return True if the button was removed
         */
        boolean removeButton(Button button) {
            return buttons.remove(button);
        }

        /**
         * @param inventory The inventory to render in
         */
        void render(Inventory inventory) {
            for (int i = 0; i < buttons.size(); i++) {
                Button button = buttons.get(i);

                inventory.setItem(i, button.getItemStack());
            }
        }

        /**
         * @return True if this page is empty
         */
        boolean isEmpty() {
            return buttons.isEmpty();
        }
    }

}
