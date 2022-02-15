package org.hassan.fadedminions.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import java.text.DecimalFormat;
import java.util.*;


public class Common {

    public static void sendMessage(Player player, String message) {

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendConsoleMessage(String message){
        Bukkit.getLogger().info(message);
    }



    public static void executePlayerCommand(Player player, String command) {
        player.performCommand(command);
    }

    public static String colorMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Location locFromString(String string) {
        String[] loc = string.split(":");

        return new Location(Bukkit.getWorld(loc[0]), Double.parseDouble(loc[1]), Double.parseDouble(loc[2]), Double.parseDouble(loc[3]), (float) Double.parseDouble(loc[4]), (float) Double.parseDouble(loc[5]));
    }

    public static String stringFromLoc(Location loc) {
        return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch();
    }

    public static String formatValue(float value) {
        String arr[] = {"", "K", "M", "B", "T", "P", "E"};
        int index = 0;
        while ((value / 1000) >= 1) {
            value = value / 1000;
            index++;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return String.format("%s%s", decimalFormat.format(value), arr[index]);
    }

    public static String checkTime(int seconds) {
        int h = seconds/ 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        String sh = (h > 0 ? String.valueOf(h) + "h" : "");
        String sm = (m < 10 && m > 0 && h > 0 ? "0" : "") + (m > 0 ? (h > 0 && s == 0 ? String.valueOf(m) : String.valueOf(m) + "m") : "");
        String ss = (s == 0 && (h > 0 || m > 0) ? "" : (s < 10 && (h > 0 || m > 0) ? "0" : "") + String.valueOf(s) + "s");
        return sh + (h > 0 ? " " : "") + sm + (m > 0 ? " " : "") + ss;
    }

    public static ArrayList<String> convertList(List<String> lore){
        ArrayList<String> formatted = new ArrayList<>();
        for(String l : lore){
            formatted.add(colorMessage(l));
        }
        return formatted;
    }

    public static Map<Integer, ItemStack> addItems(final Inventory inventory, final int oversizedStacks, final ItemStack... items) {
        if (isCombinedInv(inventory)) {
            final Inventory fakeInventory = makeTruncatedInv((PlayerInventory) inventory);
            final Map<Integer, ItemStack> overflow = addItems(fakeInventory, oversizedStacks, items);
            for (int i = 0; i < fakeInventory.getContents().length; i++)
                inventory.setItem(i, fakeInventory.getContents()[i]);
            return overflow;
        }

        final Map<Integer, ItemStack> left = new HashMap<>();

        // combine items
        final ItemStack[] combined = new ItemStack[items.length];
        for (final ItemStack item : items) {
            if (item == null || item.getAmount() < 1)
                continue;
            for (int j = 0; j < combined.length; j++) {
                if (combined[j] == null) {
                    combined[j] = item.clone();
                    break;
                }
                if (combined[j].isSimilar(item)) {
                    combined[j].setAmount(combined[j].getAmount() + item.getAmount());
                    break;
                }
            }
        }

        for (int i = 0; i < combined.length; i++) {
            final ItemStack item = combined[i];
            if (item == null || item.getType() == Material.AIR)
                continue;

            while (true) {
                // Do we already have a stack of it?
                final int maxAmount = oversizedStacks > item.getType().getMaxStackSize() ? oversizedStacks : item.getType().getMaxStackSize();
                final int firstPartial = firstPartial(inventory, item, maxAmount);

                // Drat! no partial stack
                if (firstPartial == -1) {
                    // Find a free spot!
                    final int firstFree = inventory.firstEmpty();

                    if (firstFree == -1) {
                        // No space at all!
                        left.put(i, item);
                        break;
                    }

                    // More than a single stack!
                    if (item.getAmount() > maxAmount) {
                        final ItemStack stack = item.clone();
                        stack.setAmount(maxAmount);
                        inventory.setItem(firstFree, stack);
                        item.setAmount(item.getAmount() - maxAmount);
                    } else {
                        // Just store it
                        inventory.setItem(firstFree, item);
                        break;
                    }

                } else {
                    // So, apparently it might only partially fit, well lets do just that
                    final ItemStack partialItem = inventory.getItem(firstPartial);

                    final int amount = item.getAmount();
                    final int partialAmount = partialItem.getAmount();

                    // Check if it fully fits
                    if (amount + partialAmount <= maxAmount) {
                        partialItem.setAmount(amount + partialAmount);
                        break;
                    }

                    // It fits partially
                    partialItem.setAmount(maxAmount);
                    item.setAmount(amount + partialAmount - maxAmount);
                }
            }
        }
        return left;
    }

    private static int firstPartial(final Inventory inventory, final ItemStack item, final int maxAmount) {
        if (item == null)
            return -1;
        final ItemStack[] stacks = inventory.getContents();
        for (int i = 0; i < stacks.length; i++) {
            final ItemStack cItem = stacks[i];
            if (cItem != null && cItem.getAmount() < maxAmount && cItem.isSimilar(item))
                return i;
        }
        return -1;
    }

    /**
     *
     * @param playerInventory
     * @return
     */
    private static Inventory makeTruncatedInv(final PlayerInventory playerInventory) {
        final Inventory fake = Bukkit.createInventory(null, 36);
        fake.setContents(Arrays.copyOf(playerInventory.getContents(), fake.getSize()));

        return fake;
    }

    /**
     * Return true if the inventory is combined player inventory
     *
     * @param inventory
     * @return
     */
    private static boolean isCombinedInv(final Inventory inventory) {
        return inventory instanceof PlayerInventory && inventory.getContents().length > 36;
    }


}
