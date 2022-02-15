package org.hassan.fadedminions.data;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class MinionItem {

    private String minionName;
    private Material material;

    private String materialName;

    private ArrayList<String> lore;

    private ConfigurationSection tiers;

    private ItemStack item;


    public void setMinionName(String minionName){
        this.minionName = minionName;
    }

    public void setMaterial(Material material){
        this.material = material;
    }

    public void setMaterialName(String materialName){
        this.materialName = materialName;
    }

    public void setLore(ArrayList<String> lore){
        this.lore = lore;
    }

    public void setTiers(ConfigurationSection tiers){
        this.tiers = tiers;
    }

    public void setItem(ItemStack item){
        this.item = item;
    }

    public String getMinionName(){
        return minionName;
    }

    public Material getMaterial(){
        return material;
    }

    public String getMaterialName(){
        return materialName;
    }

    public ArrayList<String> getLore(){
        return lore;
    }

    public ConfigurationSection getTiers(){
        return tiers;
    }

    public ItemStack getItem(){
        return item;
    }

}
