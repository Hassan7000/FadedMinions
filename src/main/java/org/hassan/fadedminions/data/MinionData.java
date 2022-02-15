package org.hassan.fadedminions.data;

import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class MinionData implements Serializable {

    private static final long serialVersionUID = 1L;
    public UUID uuid;

    private String minionType;

    private int level;

    private int timer;

    private float tokens;

    private double tokensToGenerate;

    private int currentTimer;

    public String getMinionType(){
        return minionType;
    }

    public int getTimer(){

        return timer;
    }

    public int getCurrentTimer(){
        return currentTimer;
    }

    public UUID getUuid(){
        return uuid;
    }

    public int getLevel(){
        return level;
    }

    public float getTokens(){
        return tokens;
    }

    public double getTokensToGenerate(){
        return tokensToGenerate;
    }

    public void setMinionType(String minionType){
        this.minionType = minionType;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public void setTimer(int timer){
        this.timer = timer;
    }

    public void setTokens(float tokens){
        this.tokens = tokens;
    }

    public void setCurrentTimer(int currentTimer){
        this.currentTimer = currentTimer;
    }


    public void setTokensToGenerate(double tokensToGenerate){
        this.tokensToGenerate = tokensToGenerate;
    }

    public void setUuid(UUID uuid){
        this.uuid = uuid;
    }



}
